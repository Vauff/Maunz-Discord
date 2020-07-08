package com.vauff.maunzdiscord.threads;

import com.github.koraktor.steamcondenser.servers.SourceServer;
import com.github.koraktor.steamcondenser.servers.SteamPlayer;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.timers.ServerTimer;
import org.bson.Document;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.mongodb.client.model.Filters.eq;

public class ServerRequestThread implements Runnable
{
	private Document doc;
	private SourceServer server;
	private Thread thread;
	private String id;

	public ServerRequestThread(Document doc, String id)
	{
		this.doc = doc;
		this.id = id;
	}

	public void start()
	{
		if (thread == null)
		{
			thread = new Thread(this, "servertracking-" + id);
			thread.start();
		}
	}

	public void run()
	{
		try
		{
			int attempts = 0;

			while (true)
			{
				try
				{
					server = new SourceServer(InetAddress.getByName(doc.getString("ip")), doc.getInteger("port"));
					server.initialize();

					try
					{
						doc.put("players", server.getPlayers().keySet());
					}
					catch (NullPointerException e)
					{
						Set<String> keySet = new HashSet<>();

						for (SteamPlayer player : new ArrayList<>(server.getPlayers().values()))
						{
							keySet.add(player.getName());
						}

						doc.put("players", keySet);
					}

					break;
				}
				catch (Exception e)
				{
					attempts++;

					if (attempts >= 5 || doc.getInteger("downtimeTimer") >= doc.getInteger("failedConnectionsThreshold"))
					{
						Logger.log.warn("Failed to connect to the server " + doc.getString("ip") + ":" + doc.getInteger("port") + ", automatically retrying in 1 minute");
						doc.put("downtimeTimer", doc.getInteger("downtimeTimer") + 1);

						if (doc.getInteger("downtimeTimer") >= 10080)
						{
							doc.put("enabled", false);
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
			String map = serverInfo.get("mapName").toString();
			String name = serverInfo.get("serverName").toString();
			int currentPlayers = Integer.parseInt(serverInfo.get("numberOfPlayers").toString());
			int maxPlayers = Integer.parseInt(serverInfo.get("maxPlayers").toString());

			if (currentPlayers > maxPlayers)
			{
				currentPlayers = maxPlayers;
			}

			String playerCount = currentPlayers + "/" + maxPlayers;

			if (!map.equals("") && !doc.getString("map").equalsIgnoreCase(map))
			{
				timestamp = System.currentTimeMillis();

				boolean mapFound = false;

				for (int i = 0; i < doc.getList("mapDatabase", Document.class).size(); i++)
				{
					String dbMap = doc.getList("mapDatabase", Document.class).get(i).getString("map");

					if (dbMap.equalsIgnoreCase(map))
					{
						mapFound = true;
						doc.getList("mapDatabase", Document.class).get(i).put("lastPlayed", timestamp);
						break;
					}
				}

				if (!mapFound)
				{
					Document mapDoc = new Document();
					mapDoc.put("map", map);
					mapDoc.put("firstPlayed", timestamp);
					mapDoc.put("lastPlayed", timestamp);
					doc.getList("mapDatabase", Document.class).add(mapDoc);
				}
			}

			if (!map.equals(""))
			{
				doc.put("map", map);
			}

			if (!playerCount.equals(""))
			{
				doc.put("playerCount", playerCount);
			}

			if (timestamp != 0)
			{
				doc.put("timestamp", timestamp);
			}

			if (!name.equals(""))
			{
				doc.put("name", name);
			}

			doc.put("downtimeTimer", 0);
			cleanup(true);
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
			cleanup(false);
		}
		finally
		{
			ServerTimer.threadRunning.put(id, false);
		}
	}

	private void cleanup(boolean success)
	{
		server.disconnect();

		if (!success)
			return;

		Main.mongoDatabase.getCollection("servers").replaceOne(eq("_id", doc.getObjectId("_id")), doc);

		if (!ServerTimer.waitingProcessThreads.containsKey(doc.getObjectId("_id").toString()))
			return;

		for (ServiceProcessThread processThread : ServerTimer.waitingProcessThreads.get(doc.getObjectId("_id").toString()))
				processThread.start();
	}
}
