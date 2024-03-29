package com.vauff.maunzdiscord.core;

import com.vauff.maunzdiscord.servertracking.ServerTrackingLoop;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Holds a timer to set the presence activity text in Discord
 */
public class PresenceTimer
{
	/**
	 * Whether alt text from config.json is currently being shown as the activity text
	 */
	private static boolean showingAltText = false;

	/**
	 * How many services are actively running, referred to as servers for simplicitly
	 */
	public static int serverCount = 0;

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
			long guildCount = Main.gateway.getGuilds().count().block();

			// Update server count if cache has not been recently invalidated
			if (ServerTrackingLoop.lastInvalidatedCache.plus(2, ChronoUnit.MINUTES).isBefore(Instant.now()))
			{
				int newServerCount = 0;

				for (int i : ServerTrackingLoop.serverActiveServices.values())
					newServerCount += i;

				serverCount = newServerCount;
			}

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
