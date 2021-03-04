package com.vauff.maunzdiscord.timers;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.threads.ServerRequestThread;
import com.vauff.maunzdiscord.threads.ServiceProcessThread;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.rest.http.client.ClientException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Duration;
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

					List<Document> serviceDocs = Main.mongoDatabase.getCollection("services").find(eq("enabled", true)).projection(new Document("serverID", 1).append("guildID", 1)).into(new ArrayList<>());
					List<Document> serverDocs = Main.mongoDatabase.getCollection("servers").find(eq("enabled", true)).projection(new Document("ip", 1).append("port", 1)).into(new ArrayList<>());

					for (Document doc : serviceDocs)
					{
						ObjectId id = doc.getObjectId("_id");
						String idString = id.toString();
						String serverID = doc.getObjectId("serverID").toString();
						Guild guild;

						try
						{
							guild = Main.gateway.getGuildById(Snowflake.of(doc.getLong("guildID"))).block(Duration.ofSeconds(10));
						}
						catch (Exception e)
						{
							// likely bot is no longer in the guild, this will sometimes also error due to general API/network issues, but we can just safely skip over a service in that instance
							continue;
						}

						threadRunning.putIfAbsent(idString, false);

						if (!threadRunning.get(idString))
						{
							ServiceProcessThread thread = new ServiceProcessThread(id, guild);

							threadRunning.put(idString, true);

							waitingProcessThreads.putIfAbsent(serverID, new ArrayList<>());
							waitingProcessThreads.get(serverID).add(thread);
						}
					}

					/**
					 * Holds a list of which servers already have a thread started in the current timer run
					 * This is different from {@link threadRunning}, because that can be asynchronously set to false before the timer stops running
					 */
					List<String> startedThreads = new ArrayList<>();

					for (Document doc : serverDocs)
					{
						if (!ServerTimer.waitingProcessThreads.containsKey(doc.getObjectId("_id").toString()))
							continue;

						ObjectId id = doc.getObjectId("_id");
						String idString = id.toString();
						String ipPort = doc.getString("ip") + ":" + doc.getInteger("port");

						threadRunning.putIfAbsent(idString, false);

						if (!threadRunning.get(idString) && !startedThreads.contains(idString))
						{
							ServerRequestThread thread = new ServerRequestThread(id, ipPort);

							threadRunning.put(idString, true);
							thread.start();
							startedThreads.add(idString);
						}
					}
				}
			}
			catch (Exception e)
			{
				Logger.log.error(ExceptionUtils.getStackTrace(e));
			}
		}
	};
}