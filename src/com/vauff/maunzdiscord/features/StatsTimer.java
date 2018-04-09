package com.vauff.maunzdiscord.features;

import com.vauff.maunzdiscord.core.Main;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

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
					Main.client.changePresence(StatusType.ONLINE, ActivityType.PLAYING, Main.client.getGuilds().size() + " guilds");
					showingGuilds = true;
				}
				else
				{
					Main.client.changePresence(StatusType.ONLINE, ActivityType.PLAYING, "discord.gg/v55fW9b");
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
