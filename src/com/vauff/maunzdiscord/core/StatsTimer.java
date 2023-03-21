package com.vauff.maunzdiscord.core;

import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;

/**
 * Holds a timer to set the playing text in Discord
 */
public class StatsTimer
{
	private static boolean showingGuilds = true;

	/**
	 * Updates the bot's playing text to switch between showing the amount of guilds the bot is on and the alt text configured in config.json
	 */
	public static Runnable timer = () ->
	{
		try
		{
			if (Main.gateway.getGatewayClient(0).get().isConnected().block())
			{
				if (!showingGuilds)
				{
					if (Main.gateway.getGuilds().count().block() == 1L)
					{
						Main.gateway.updatePresence(ClientPresence.online(ClientActivity.playing(Main.gateway.getGuilds().count().block() + " guild"))).block();
					}
					else
					{
						Main.gateway.updatePresence(ClientPresence.online(ClientActivity.playing(Main.gateway.getGuilds().count().block() + " guilds"))).block();
					}
					showingGuilds = true;
				}
				else
				{
					Main.gateway.updatePresence(ClientPresence.online(ClientActivity.playing(Main.cfg.getPlayingText()))).block();
					showingGuilds = false;
				}
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	};
}
