package com.vauff.maunzdiscord.timers;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.threads.ServerProcessThread;
import com.vauff.maunzdiscord.threads.ServerRequestThread;
import org.bson.Document;

import java.util.HashMap;

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
	 * Iterate the server tracking storage and start threads for each server and service
	 */
	public static Runnable timer = new Runnable()
	{
		public void run()
		{
			try
			{
				if (Main.gateway.getGatewayClient(0).get().isConnected())
				{
					Logger.log.debug("Starting a server timer run...");

					for (Document doc : Main.mongoDatabase.getCollection("services").find(eq("enabled", true)))
					{
						String id = doc.getObjectId("_id").toString();

						if (!threadRunning.containsKey(id))
							threadRunning.put(id, false);

						if (!threadRunning.get(id))
						{
							ServerProcessThread thread = new ServerProcessThread(doc, id);

							threadRunning.put(id, true);
							thread.start();
						}
					}

					for (Document doc : Main.mongoDatabase.getCollection("servers").find(eq("enabled", true)))
					{
						String id = doc.getString("ip") + ":" + doc.getInteger("port");

						if (!threadRunning.containsKey(id))
							threadRunning.put(id, false);

						if (!threadRunning.get(id))
						{
							ServerRequestThread thread = new ServerRequestThread(doc, id);

							threadRunning.put(id, true);
							thread.start();
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