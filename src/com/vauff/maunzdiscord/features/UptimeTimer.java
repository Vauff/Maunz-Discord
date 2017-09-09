package com.vauff.maunzdiscord.features;

import java.util.ArrayList;

import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

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
				if (!Main.bot.isConnected() || !Main.client.isLoggedIn())
				{
					final ArrayList<String> command = new ArrayList<String>();

					command.add("java");
					command.add("-jar");
					command.add("Maunz-Discord.jar");

					if (Util.devMode)
					{
						command.add("-dev");
					}
					
					Main.log.info("Maunz is restarting...");
					new ProcessBuilder(command).start();
					Main.client.logout();
					Main.bot.disconnect();
					System.exit(0);
				}
			}
			catch (Exception e)
			{
				Main.log.error("", e);
			}
		}
	};
}
