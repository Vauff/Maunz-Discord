package com.vauff.maunzdiscord.core;

import com.vauff.maunzdiscord.servertracking.ServerTimer;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;

/**
 * Holds a timer to set the presence activity text in Discord
 */
public class PresenceTimer
{
	private static boolean showingAltText = false;

	/**
	 * Updates the bot's activity text to switch between showing the amount of servers the bot is tracking and the alt text configured in config.json
	 */
	public static Runnable timer = () ->
	{
		try
		{
			if (!Main.gateway.getGatewayClient(0).get().isConnected().block())
				return;

			String altPlayingText = Main.cfg.getPlayingText();
			int serverCount = ServerTimer.serverCount;
			long guildCount = Main.gateway.getGuilds().count().block();

			if (showingAltText || altPlayingText.equals(""))
			{
				if (serverCount == 0)
					return;

				String text = serverCount + " server" + (serverCount == 1 ? "" : "s") + " on " + guildCount + " guild" + (guildCount == 1 ? "" : "s");

				Main.gateway.updatePresence(ClientPresence.online(ClientActivity.watching(text))).block();
				showingAltText = false;
			}
			else
			{
				Main.gateway.updatePresence(ClientPresence.online(ClientActivity.playing(altPlayingText))).block();
				showingAltText = true;
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	};
}
