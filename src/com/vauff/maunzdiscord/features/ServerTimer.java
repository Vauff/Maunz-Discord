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
	 * Holds extended information about servers (for instance online players)
	 */
	public static HashMap<String, Set<String>> serverPlayers = new HashMap<>();

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
						ServerTimerThread thread = new ServerTimerThread(file, "servertracking-" + file.getName().replace(".json", ""));
						thread.start();
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