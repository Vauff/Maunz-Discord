package com.vauff.maunzdiscord.features;

import com.vauff.maunzdiscord.core.Main;

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
					Main.log.info("Maunz is restarting...");
					Main.client.logout();
					Main.bot.disconnect();
					System.exit(1);
				}
			}
			catch (Exception e)
			{
				Main.log.error("", e);
			}
		}
	};
}
