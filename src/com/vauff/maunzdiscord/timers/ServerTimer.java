package com.vauff.maunzdiscord.timers;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.threads.ServerRequestThread;
import com.vauff.maunzdiscord.threads.ServiceProcessThread;
import discord4j.common.util.Snowflake;
import discord4j.rest.http.client.ClientException;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * A timer to refresh the state of tracked servers
 */
public class ServerTimer
{
	/**
	 * Holds the boolean status of whether each server/service currently has a thread running or not
	 */
	public static HashMap<String, Boolean> threadRunning = new HashMap<>();

	/**
	 * Holds lists of which ServerProcessThreads are waiting for which server requests to finish
	 */
	public static HashMap<String, List<ServiceProcessThread>> waitingProcessThreads = new HashMap<>();

	/**
	 * Iterate the server tracking storage and start threads for each server and service
	 */
	public static Runnable timer = new Runnable()
	{
		public void run()
		{
			try
			{
				if (Main.gateway.getGatewayClient(0).get().isConnected().block())
				{
					Logger.log.debug("Starting a server timer run...");

					for (Document doc : Main.mongoDatabase.getCollection("services").find(eq("enabled", true)))
					{
						String id = doc.getObjectId("_id").toString();
						String serverID = doc.getObjectId("serverID").toString();

						try
						{
							Main.gateway.getGuildById(Snowflake.of(doc.getLong("guildID"))).block();
						}
						catch (ClientException e)
						{
							// bot is no longer in this guild
							continue;
						}

						threadRunning.putIfAbsent(id, false);

						if (!threadRunning.get(id))
						{
							ServiceProcessThread thread = new ServiceProcessThread(doc, id);

							threadRunning.put(id, true);

							waitingProcessThreads.putIfAbsent(serverID, new ArrayList<>());
							waitingProcessThreads.get(serverID).add(thread);
						}
					}

					/**
					 * Holds a list of which servers already have a thread started in the current timer run
					 * This is different from {@link threadRunning}, because that can be asynchronously set to false before the timer stops running
					 */
					List<String> startedThreads = new ArrayList<>();

					for (Document doc : Main.mongoDatabase.getCollection("servers").find(eq("enabled", true)))
					{
						if (!ServerTimer.waitingProcessThreads.containsKey(doc.getObjectId("_id").toString()))
							continue;

						String id = doc.getString("ip") + ":" + doc.getInteger("port");

						threadRunning.putIfAbsent(id, false);

						if (!threadRunning.get(id) && !startedThreads.contains(id))
						{
							ServerRequestThread thread = new ServerRequestThread(doc, id);

							threadRunning.put(id, true);
							thread.start();
							startedThreads.add(id);
						}
					}
				}
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
			}
		}
	};
}