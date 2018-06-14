package com.vauff.maunzdiscord.features;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.SteamPlayer;
import com.github.koraktor.steamcondenser.steam.servers.SourceServer;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.threads.ServerTimerThread;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.File;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * A timer to send notifications of maps currently being played on given servers
 */
public class ServerTimer
{
	/**
	 * Holds extended information about servers (for instance online players)
	 */
	public static HashMap<String, Set<String>> serverPlayers = new HashMap<>();

	/**
	 * Checks the servers in {@link Util#getJarLocation()}/services/map-tracking for new maps being played and sends them to a channel
	 * as well as notifying users that set up a notification for that map
	 */
	public static Runnable timer = new Runnable()
	{
		public void run()
		{
			try
			{
				for (File file : new File(Util.getJarLocation() + "data/services/server-tracking").listFiles())
				{
					ServerTimerThread thread = new ServerTimerThread(file, "servertracking-" + file.getName().replace(".json", ""));
					thread.start();
				}
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
			}
		}
	};
}