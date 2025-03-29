package com.vauff.maunzdiscord.servertracking;

import com.github.koraktor.steamcondenser.servers.GameServer;
import com.github.koraktor.steamcondenser.servers.SourceServer;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Objects;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class ServerRequestThread implements Runnable
{
	/**
	 * Cached InetAddress objects, because it's *very* expensive to constantly run InetAddress.getByName
	 */
	private static ConcurrentHashMap<String, InetAddress> serverAddresses = new ConcurrentHashMap<>();
	private Thread thread;
	private ObjectId id;
	private String ipPort;
	private GameServer server;

	public ServerRequestThread(ObjectId id, String ipPort)
	{
		this.id = id;
		this.ipPort = ipPort;
	}

	public void start()
	{
		if (thread == null)
		{
			thread = new Thread(this, "servertracking-" + ipPort);
			thread.start();
		}
	}

	public void run()
	{
		try
		{
			Document doc = Main.mongoDatabase.getCollection("servers").find(eq("_id", id)).first();
			int attempts = 0;
			boolean serverInfoSuccess = false;

			while (true)
			{
				try
				{
					if (!serverInfoSuccess)
					{
						InetAddress address;

						if (serverAddresses.containsKey(ipPort))
						{
							address = serverAddresses.get(ipPort);
						}
						else
						{
							address = InetAddress.getByName(doc.getString("ip"));
							serverAddresses.put(ipPort, address);
						}

						server = new SourceServer(address, doc.getInteger("port"));
						server.updateServerInfo();
						serverInfoSuccess = true;
					}

					server.updatePlayers();
					Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("players", server.getPlayers().keySet())));
					break;
				}
				catch (Exception e)
				{
					attempts++;

					if (attempts >= 5 || (!serverInfoSuccess && doc.getInteger("downtimeTimer") >= doc.getInteger("failedConnectionsThreshold")))
					{
						if (!serverInfoSuccess)
						{
							int downtimeTimer = doc.getInteger("downtimeTimer") + 1;

							Logger.log.warn("Failed to connect to the server " + ipPort + ", automatically retrying in 1 minute");
							Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("downtimeTimer", downtimeTimer)));

							if (downtimeTimer >= 10080)
							{
								Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("enabled", false)));
								ServerTrackingLoop.lastInvalidatedCache = Instant.now();
								ServerTrackingLoop.serverActiveServices.remove(id);
							}

							runServiceThreads();
							return;
						}
						else
						{
							Logger.log.warn("Failed to retrieve player information from " + ipPort + ", automatically retrying in 1 minute");
							Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("players", List.of("SERVER_UPDATEPLAYERS_FAILED"))));
							break;
						}
					}
					else
					{
						Thread.sleep(1000);
					}
				}
			}

			HashMap<String, Object> serverInfo = server.getServerInfo();
			long timestamp = 0;
			String appId = "0";
			String map = "";
			String name = "N/A";
			int currentPlayers = 0;
			int maxPlayers = 0;

			if (serverInfo.containsKey("mapName") && !Objects.isNull(serverInfo.get("mapName")))
			{
				map = serverInfo.get("mapName").toString();
			}
			else
			{
				Logger.log.warn("Null mapname received for server " + ipPort + ", automatically retrying in 1 minute");
				return;
			}

			// 24-bit app id within 64-bit game id, may not be available
			if (serverInfo.containsKey("gameId") && !Objects.isNull(serverInfo.get("gameId")))
				appId = String.valueOf((int) (((long) serverInfo.get("gameId")) & (1L << 24) - 1L));
			// 16-bit app id, possibly truncated but (theoretically) always available
			else if (serverInfo.containsKey("appId") && !Objects.isNull(serverInfo.get("appId")))
				appId = String.valueOf((short) serverInfo.get("appId"));

			// Special handling for CS:GO/CS2, thanks for two games on the same app id, Valve!
			if (appId.equals("730"))
			{
				if (serverInfo.containsKey("gameVersion") && !Objects.isNull(serverInfo.get("gameVersion")))
				{
					String version = serverInfo.get("gameVersion").toString();

					// Strip periods, take first three numbers only e.g. "1.38.8.1" > 138
					int majorVersion = Integer.parseInt(version.replace(".", "").substring(0, 3));

					if (majorVersion >= 139)
						appId = "730_cs2";
					else if (majorVersion <= 138)
						appId = "730_csgo";
				}
				else
				{
					appId = "0";
				}
			}

			if (serverInfo.containsKey("serverName") && !Objects.isNull(serverInfo.get("serverName")))
				name = serverInfo.get("serverName").toString();

			if (serverInfo.containsKey("numberOfPlayers") && !Objects.isNull(serverInfo.get("numberOfPlayers")))
				currentPlayers = ((Byte) serverInfo.get("numberOfPlayers")).intValue();

			if (serverInfo.containsKey("maxPlayers") && !Objects.isNull(serverInfo.get("maxPlayers")))
				maxPlayers = ((Byte) serverInfo.get("maxPlayers")).intValue();

			if (currentPlayers > maxPlayers && maxPlayers >= 0)
				currentPlayers = maxPlayers;

			String playerCount = currentPlayers + "/" + maxPlayers;

			if (!map.equals("") && !map.equalsIgnoreCase(doc.getString("map")))
			{
				Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("map", map)));
				timestamp = System.currentTimeMillis();

				// Precache the map image colour for service process threads, because multiple threads can run getMapImageColour at the same time, wasting resources if not cached yet
				MapImages.getMapImageColour(MapImages.getMapImageURL(map, appId));

				boolean mapFound = false;

				for (int i = 0; i < doc.getList("mapDatabase", Document.class).size(); i++)
				{
					String dbMap = doc.getList("mapDatabase", Document.class).get(i).getString("map");

					if (dbMap.equalsIgnoreCase(map))
					{
						mapFound = true;
						Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("mapDatabase." + i + ".lastPlayed", timestamp)));
						break;
					}
				}

				if (!mapFound)
				{
					Document mapDoc = new Document();
					mapDoc.put("map", map);
					mapDoc.put("firstPlayed", timestamp);
					mapDoc.put("lastPlayed", timestamp);
					Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$push", new Document("mapDatabase", mapDoc)));
				}
			}

			if (!appId.equals("0") && !appId.equals(doc.getString("appId")))
				Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("appId", appId)));

			if (!playerCount.equals("") && !playerCount.equals(doc.getString("playerCount")))
				Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("playerCount", playerCount)));

			if (timestamp != 0)
				Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("timestamp", timestamp)));

			if (!name.equals("") && !name.equals(doc.getString("name")))
				Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("name", name)));

			if (doc.getInteger("downtimeTimer") != 0)
				Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("downtimeTimer", 0)));

			runServiceThreads();
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
		finally
		{
			ServerTrackingLoop.threadRunning.put(id, false);
		}
	}

	private void runServiceThreads() throws Exception
	{
		Document doc = Main.mongoDatabase.getCollection("servers").find(eq("_id", id)).first();
		List<Document> serviceDocs = Main.mongoDatabase.getCollection("services").find(and(eq("enabled", true), eq("serverID", id))).into(new ArrayList<>());

		for (Document serviceDoc : serviceDocs)
		{
			ObjectId serviceId = serviceDoc.getObjectId("_id");
			Snowflake guildId = Snowflake.of(serviceDoc.getLong("guildID"));
			Guild guild;

			if (Main.guildCache.containsKey(guildId))
				guild = Main.guildCache.get(guildId);
			else
				continue;

			ServerTrackingLoop.threadRunning.putIfAbsent(serviceId, false);

			if (!ServerTrackingLoop.threadRunning.get(serviceId))
			{
				ServiceProcessThread thread = new ServiceProcessThread(serviceDoc, doc, guild);

				ServerTrackingLoop.threadRunning.put(serviceId, true);
				thread.start();

				// only start ~20 threads per second
				Thread.sleep(50);
			}
		}
	}
}
