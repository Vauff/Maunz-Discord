package com.vauff.maunzdiscord.servertracking;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import discord4j.common.util.Snowflake;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
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
	public static ConcurrentHashMap<ObjectId, Boolean> threadRunning = new ConcurrentHashMap<>();

	/**
	 * How many active services a server ID has attached
	 */
	public static ConcurrentHashMap<ObjectId, Integer> serverActiveServices = new ConcurrentHashMap<>();

	/**
	 * Last time serverActiveServices cache was invalidated
	 */
	public static Instant lastInvalidatedCache = Instant.ofEpochMilli(0L);

	private Thread thread;
	private final long LOOP_TIME = 60000;

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
					// Back off for a full loop duration and try again
					Thread.sleep(LOOP_TIME);
					continue;
				}

				// Check if a shutdown is queued
				if (Main.shutdownState == Main.ShutdownState.SHUTDOWN_QUEUED)
				{
					while (true)
					{
						boolean noThreadsRunning = true;

						// Wait until no threads are running
						for (Boolean value : threadRunning.values())
						{
							if (value)
							{
								noThreadsRunning = false;
								break;
							}
						}

						if (noThreadsRunning)
							break;
						else
							Thread.sleep(1000);
					}

					// Stop, and mark the bot safe to shutdown
					Main.shutdownState = Main.ShutdownState.SHUTDOWN_SAFE;
					break;
				}

				List<Document> serverDocs = Main.mongoDatabase.getCollection("servers").find(eq("enabled", true)).projection(new Document("ip", 1).append("port", 1)).into(new ArrayList<>());
				List<Document> serviceDocs = null;
				long startTime = System.currentTimeMillis();
				long targetSleepTime = LOOP_TIME / Math.max(serverDocs.size(), 1);
				int iterCount = 0;

				for (Document doc : serverDocs)
				{
					ObjectId id = doc.getObjectId("_id");
					String ipPort = doc.getString("ip") + ":" + doc.getInteger("port");

					iterCount++;

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

					// Determine how long we would need to sleep to stay on schedule for this iteration
					long sleepTime = (System.currentTimeMillis() - (startTime + (targetSleepTime * iterCount))) * -1;

					// Sleep only if we're ahead of schedule
					if (sleepTime > 0)
						Thread.sleep(sleepTime);
				}

				// Might still be ahead of schedule if last servers had no active services
				long elapsedTime = System.currentTimeMillis() - startTime;

				if (elapsedTime < LOOP_TIME)
					Thread.sleep(LOOP_TIME - elapsedTime);

				Logger.log.debug("Finished server tracking loop in " + (System.currentTimeMillis() - startTime) + " ms");
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
			}
		}
	}
}