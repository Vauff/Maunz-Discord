package com.vauff.maunzdiscord.threads;

import com.github.koraktor.steamcondenser.servers.GameServer;
import com.github.koraktor.steamcondenser.servers.SourceServer;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.timers.ServerTimer;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.mongodb.client.model.Filters.eq;

public class ServerRequestThread implements Runnable
{
	private GameServer server;
	private Thread thread;
	private ObjectId id;
	private String ipPort;

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

			while (true)
			{
				try
				{
					server = new SourceServer(InetAddress.getByName(doc.getString("ip")), doc.getInteger("port"));
					server.initialize();
					server.updatePlayers();
					Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("players", server.getPlayers().keySet())));

					break;
				}
				catch (Exception e)
				{
					attempts++;

					if (attempts >= 5 || doc.getInteger("downtimeTimer") >= doc.getInteger("failedConnectionsThreshold"))
					{
						Logger.log.warn("Failed to connect to the server " + ipPort + ", automatically retrying in 1 minute");
						int downtimeTimer = doc.getInteger("downtimeTimer") + 1;
						Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("downtimeTimer", downtimeTimer)));

						if (downtimeTimer >= 10080)
						{
							Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("enabled", false)));
						}

						cleanup(true);
						return;
					}
					else
					{
						Thread.sleep(1000);
					}
				}
			}

			HashMap<String, Object> serverInfo = server.getServerInfo();
			long timestamp = 0;
			int appId = 0;
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
				cleanup(false);
				return;
			}

			// 24-bit app id within 64-bit game id, may not be available
			if (serverInfo.containsKey("gameId") && !Objects.isNull(serverInfo.get("gameId")))
				appId = (int) (((long) serverInfo.get("gameId")) & (1L << 24) - 1L);

			// 16-bit app id, possibly truncated but (theoretically) always available
			else if (serverInfo.containsKey("appId") && !Objects.isNull(serverInfo.get("appId")))
				appId = (short) serverInfo.get("appId");

			if (serverInfo.containsKey("serverName") && !Objects.isNull(serverInfo.get("serverName")))
				name = serverInfo.get("serverName").toString();

			if (serverInfo.containsKey("numberOfPlayers") && !Objects.isNull(serverInfo.get("numberOfPlayers")))
				currentPlayers = Integer.parseInt(serverInfo.get("numberOfPlayers").toString());

			if (serverInfo.containsKey("maxPlayers") && !Objects.isNull(serverInfo.get("maxPlayers")))
				maxPlayers = Integer.parseInt(serverInfo.get("maxPlayers").toString());

			if (currentPlayers > maxPlayers && maxPlayers >= 0)
				currentPlayers = maxPlayers;

			String playerCount = currentPlayers + "/" + maxPlayers;

			if (!map.equals("") && !map.equalsIgnoreCase(doc.getString("map")))
			{
				Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("map", map)));
				timestamp = System.currentTimeMillis();

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

			if (appId != 0 && appId != doc.getInteger("appId"))
				Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("appId", appId)));

			if (!playerCount.equals("") && !playerCount.equals(doc.getString("playerCount")))
				Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("playerCount", playerCount)));

			if (timestamp != 0)
				Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("timestamp", timestamp)));

			if (!name.equals("") && !name.equals(doc.getString("name")))
				Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("name", name)));

			if (doc.getInteger("downtimeTimer") != 0)
				Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("downtimeTimer", 0)));

			cleanup(true);
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
			cleanup(false);
		}
		finally
		{
			ServerTimer.threadRunning.put(id.toString(), false);
		}
	}

	private void cleanup(boolean success)
	{
		if (!Objects.isNull(server))
			server.disconnect();

		List<ServiceProcessThread> processThreads = new ArrayList<>(ServerTimer.waitingProcessThreads.get(id.toString()));

		if (success)
		{
			for (ServiceProcessThread processThread : processThreads)
			{
				processThread.start();

				// TODO: replace this awful workaround with a new scheduler
				// only start ~10 threads per second
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					Logger.log.error("", e);
				}
			}
		}
		else
		{
			for (ServiceProcessThread processThread : processThreads)
				ServerTimer.threadRunning.put(processThread.id.toString(), false);
		}

		ServerTimer.waitingProcessThreads.get(id.toString()).clear();
	}
}
