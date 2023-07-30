package com.vauff.maunzdiscord.servertracking;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import discord4j.common.util.Snowflake;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.mongodb.client.model.Filters.eq;

/**
 * Main class for running server tracking
 */
public class ServerTrackingLoop implements Runnable
{
	/**
	 * Holds the boolean status of whether each server/service currently has a thread running or not
	 */
	public static HashMap<ObjectId, Boolean> threadRunning = new HashMap<>();

	/**
	 * How many active services a server ID has attached
	 */
	public static HashMap<ObjectId, Integer> serverActiveServices = new HashMap<>();

	/**
	 * Last time serverActiveServices cache was invalidated
	 */
	public static Instant lastInvalidatedCache = Instant.ofEpochMilli(0L);

	private Thread thread;

	public void start() throws Exception
	{
		if (thread == null)
		{
			// Initial 10 second delay to allow map images to fully initialize
			Thread.sleep(10000);

			thread = new Thread(this, "servertracking-mainloop");
			thread.start();
		}
	}

	/**
	 * The server tracking main loop
	 */
	public void run()
	{
		while (true)
		{
			try
			{
				if (!Main.gateway.getGatewayClient(0).get().isConnected().block())
				{
					// Back off for 60 seconds and try again
					Thread.sleep(60000);
					continue;
				}

				List<Document> serverDocs = Main.mongoDatabase.getCollection("servers").find(eq("enabled", true)).projection(new Document("ip", 1).append("port", 1)).into(new ArrayList<>());
				List<Document> serviceDocs = null;

				for (Document doc : serverDocs)
				{
					ObjectId id = doc.getObjectId("_id");
					String ipPort = doc.getString("ip") + ":" + doc.getInteger("port");

					// Store how many services are actively tracking this server
					if (!serverActiveServices.containsKey(id))
					{
						int serviceCount = 0;

						if (Objects.isNull(serviceDocs))
							serviceDocs = Main.mongoDatabase.getCollection("services").find(eq("enabled", true)).projection(new Document("serverID", 1).append("guildID", 1)).into(new ArrayList<>());

						for (Document serviceDoc : serviceDocs)
						{
							ObjectId serverID = serviceDoc.getObjectId("serverID");
							Snowflake guildId = Snowflake.of(serviceDoc.getLong("guildID"));

							if (id.equals(serverID) && Main.guildCache.containsKey(guildId))
								serviceCount++;

						}

						serverActiveServices.put(id, serviceCount);
					}

					// Check if this server needs to be tracked
					if (serverActiveServices.get(id) == 0)
						continue;

					threadRunning.putIfAbsent(id, false);

					if (!threadRunning.get(id))
					{
						ServerRequestThread thread = new ServerRequestThread(id, ipPort);

						threadRunning.put(id, true);
						thread.start();
					}

					// TODO: dynamic sleep
				}
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
			}
		}
	}
}