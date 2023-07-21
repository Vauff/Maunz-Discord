package com.vauff.maunzdiscord.servertracking;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	 * Whether a server ID has any active services attached
	 */
	public static HashMap<ObjectId, Boolean> serverHasActiveServices = new HashMap<>();

	/**
	 * How many services are actively running, referred to as servers for simplicitly
	 */
	public static int serverCount = 0;

	private Thread thread;

	public void start() throws Exception
	{
		if (thread == null)
		{
			// Initial 10 second delay to allow map images to fully initialize
			Thread.sleep(10000);

			thread = new Thread(this, "servertrackingmainloop");
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

				List<Document> serviceDocs = Main.mongoDatabase.getCollection("services").find(eq("enabled", true)).projection(new Document("serverID", 1).append("guildID", 1)).into(new ArrayList<>());
				List<Document> serverDocs = Main.mongoDatabase.getCollection("servers").find(eq("enabled", true)).projection(new Document("ip", 1).append("port", 1)).into(new ArrayList<>());

				for (Document doc : serverDocs)
				{
					ObjectId id = doc.getObjectId("_id");
					String ipPort = doc.getString("ip") + ":" + doc.getInteger("port");

					if (!serverHasActiveServices.containsKey(id))
					{
						// TODO: precache here
					}

					if (!serverHasActiveServices.get(id))
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