package com.vauff.maunzdiscord.features;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.threads.ServerTimerThread;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

/**
 * A timer to send notifications of maps currently being played on given servers
 */
public class ServerTimer
{
	/**
	 * Holds online player lists for each server
	 */
	public static HashMap<String, Set<String>> serverPlayers = new HashMap<>();

	/**
	 * Holds the boolean status of whether each guilds server tracking services currently have a thread running or not
	 */
	public static HashMap<String, Boolean> trackingThreadRunning = new HashMap<>();

	/**
	 * Checks the servers in {@link Util#getJarLocation()}/data/services/server-tracking for new maps being played and sends them to a channel
	 * as well as notifying users that set up a notification for that map
	 */
	public static Runnable timer = new Runnable()
	{
		public void run()
		{
			try
			{
				if (Main.client.isConnected())
				{
					for (File file : new File(Util.getJarLocation() + "data/services/server-tracking").listFiles())
					{
						String id = file.getName();

						if (!trackingThreadRunning.containsKey(id))
						{
							trackingThreadRunning.put(id, false);
						}

						if (!trackingThreadRunning.get(id))
						{
							ServerTimerThread thread = new ServerTimerThread(file, "servertracking-" + id);

							trackingThreadRunning.put(id, true);
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