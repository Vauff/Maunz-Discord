package com.vauff.maunzdiscord.features;

import com.vauff.maunzdiscord.core.Main;

/**
 * Holds a timer to set the playing text in Discord
 */
public class StatsTimer
{
	private static boolean showingGuilds = false;
	/**
	 * Updates the bot's playing text to show the amount of guilds the bot is on and the invite link to the official Maunz Discord server
	 */
	public static Runnable timer = new Runnable()
	{
		public void run()
		{
			try
			{
				if (!showingGuilds)
				{
					Main.client.changePlayingText(Main.client.getGuilds().size() + " guilds");
					showingGuilds = true;
				}
				else
				{
					Main.client.changePlayingText("discord.gg/v55fW9b");
					showingGuilds = false;
				}
			}
			catch (Exception e)
			{
				Main.log.error("", e);
			}
		}
	};
}
