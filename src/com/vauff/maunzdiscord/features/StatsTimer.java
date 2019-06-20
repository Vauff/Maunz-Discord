package com.vauff.maunzdiscord.features;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;

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
					if (Main.client.getGuilds().count().block() == 1L)
					{
						Main.client.updatePresence(Presence.online(Activity.playing(Main.client.getGuilds().count().block() + " guild"))).block();
					}
					else
					{
						Main.client.updatePresence(Presence.online(Activity.playing(Main.client.getGuilds().count().block() + " guilds"))).block();
					}
					showingGuilds = true;
				}
				else
				{
					Main.client.updatePresence(Presence.online(Activity.playing("discord.gg/v55fW9b"))).block();
					showingGuilds = false;
				}
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
			}
		}
	};
}
