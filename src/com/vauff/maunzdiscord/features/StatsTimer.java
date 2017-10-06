package com.vauff.maunzdiscord.features;

import com.vauff.maunzdiscord.core.Main;

/**
 * Holds a timer to set the playing text in Discord
 */
public class StatsTimer
{
	/** Updates the bot's playing text to show the amount of guilds the bot is on and how many users those guilds have */
	public static Runnable timer = new Runnable()
	{
		public void run()
		{
			try
			{
				Main.client.changePlayingText(Main.client.getGuilds().size() + " guilds");
			}
			catch (Exception e)
			{
				Main.log.error("", e);
			}
		}
	};
}
