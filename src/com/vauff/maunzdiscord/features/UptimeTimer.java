package com.vauff.maunzdiscord.features;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;

import java.util.ArrayList;

/**
 * Holds a timer to restart the bot if the IRC or Discord connection is lost
 */
public class UptimeTimer
{
	public static Runnable timer = new Runnable()
	{
		public void run()
		{
			try
			{
				if (!Main.client.isLoggedIn())
				{
					final ArrayList<String> command = new ArrayList<String>();

					command.add("java");
					command.add("-jar");
					command.add("Maunz-Discord.jar");

					Logger.log.info("Maunz is restarting...");
					new ProcessBuilder(command).start();
					System.exit(0);
				}
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
			}
		}
	};
}
