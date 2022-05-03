package com.vauff.maunzdiscord.timers;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;

import java.time.Instant;

/**
 * Shitty workaround for Discord4J hanging when reconnecting to Discord, force restarts the bot if no progress is being made after 15 minutes
 *
 * Note: This requires some sort of external restarter (e.g. being registered as a systemd service) to take care of starting the bot back up again, so this is disabled by default (see "restartWhenHung" in config.json)
 */
public class HangTimer
{
	private static Long disconnectedAt = 0L;

	public static Runnable timer = () ->
	{
		try
		{
			if (Main.gateway.getGatewayClient(0).get().isConnected().block())
			{
				disconnectedAt = 0L;
			}
			else
			{
				long now = Instant.now().getEpochSecond();

				if (disconnectedAt == 0L)
					disconnectedAt = now;

				if ((disconnectedAt + 900L) < now)
				{
					Logger.log.fatal("Detected bot in hung state, attempting to force an external restart");
					System.exit(1);
				}
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	};
}
