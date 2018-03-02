package com.vauff.maunzdiscord.core;

import com.vauff.maunzdiscord.commands.Help;
import com.vauff.maunzdiscord.features.ServerTimer;
import com.vauff.maunzdiscord.features.StatsTimer;
import com.vauff.maunzdiscord.features.UptimeTimer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ReadyEventListener
{
	/**
	 * A watch to keep track of the uptime of the bot
	 */
	public static StopWatch uptime = new StopWatch();

	@EventSubscriber
	public void onReady(ReadyEvent event)
	{
		try
		{
			uptime.start();

			List<File> folderList = new ArrayList<File>();

			folderList.add(new File(Util.getJarLocation() + "data/"));
			folderList.add(new File(Util.getJarLocation() + "data/services/"));
			folderList.add(new File(Util.getJarLocation() + "data/guilds/"));
			folderList.add(new File(Util.getJarLocation() + "data/services/server-tracking/"));

			for (File folder : folderList)
			{
				if (!folder.isDirectory())
				{
					folder.mkdir();
				}
			}

			for (IGuild guild : Main.client.getGuilds())
			{
				File file = new File(Util.getJarLocation() + "data/guilds/" + guild.getStringID() + ".json");

				if (!file.exists())
				{
					JSONObject json = new JSONObject();

					file.createNewFile();
					json.put("enabled", true);
					json.put("lastGuildName", guild.getName());
					json.put("blacklist", new JSONArray());
					FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
				}
			}

			Help.setupCmdHelp();

			Main.client.getDispatcher().registerListener(new MainListener());
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(ServerTimer.timer, 0, 60, TimeUnit.SECONDS);
			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(UptimeTimer.timer, 600, 60, TimeUnit.SECONDS);
			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(StatsTimer.timer, 0, 300, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}
}
