package com.vauff.maunzdiscord.features;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import org.json.JSONObject;

import java.io.File;

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
				if (Main.gateway.getGatewayClient(0).get().isConnected())
				{
					if (!showingGuilds)
					{
						if (Main.gateway.getGuilds().count().block() == 1L)
						{
							Main.gateway.updatePresence(Presence.online(Activity.playing(Main.gateway.getGuilds().count().block() + " guild"))).block();
						}
						else
						{
							Main.gateway.updatePresence(Presence.online(Activity.playing(Main.gateway.getGuilds().count().block() + " guilds"))).block();
						}
						showingGuilds = true;
					}
					else
					{
						JSONObject json = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "config.json")));

						Main.gateway.updatePresence(Presence.online(Activity.playing(json.getString("altPlayingText")))).block();
						showingGuilds = false;
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
