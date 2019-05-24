package com.vauff.maunzdiscord.core;

import com.vauff.maunzdiscord.commands.Help;
import com.vauff.maunzdiscord.features.ServerTimer;
import com.vauff.maunzdiscord.features.StatsTimer;
import com.vauff.maunzdiscord.features.UptimeTimer;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Guild;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONArray;
import org.json.JSONObject;

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

	public static void onReady()
	{
		try
		{
			uptime.start();

			List<File> folderList = new ArrayList<>();

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

			for (Guild guild : Main.client.getGuilds().toIterable())
			{
				File file = new File(Util.getJarLocation() + "data/guilds/" + guild.getId() + ".json");

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

			Main.client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(Logger::onMessageCreate);
			Main.client.getEventDispatcher().on(MessageUpdateEvent.class).subscribe(Logger::onMessageUpdate);
			Main.client.getEventDispatcher().on(MessageDeleteEvent.class).subscribe(Logger::onMessageDelete);
			Main.client.getEventDispatcher().on(ReactionAddEvent.class).subscribe(Logger::onReactionAdd);
			Main.client.getEventDispatcher().on(ReactionRemoveEvent.class).subscribe(Logger::onReactionRemove);
			Main.client.getEventDispatcher().on(GuildCreateEvent.class).subscribe(Logger::onGuildCreate);
			Main.client.getEventDispatcher().on(GuildDeleteEvent.class).subscribe(Logger::onGuildDelete);
			new MainListener();
			Main.client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(MainListener::onMessageCreate);
			Main.client.getEventDispatcher().on(ReactionAddEvent.class).subscribe(MainListener::onReactionAdd);
			Main.client.getEventDispatcher().on(GuildCreateEvent.class).subscribe(MainListener::onGuildCreate);
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(ServerTimer.timer, 0, 60, TimeUnit.SECONDS);
			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(UptimeTimer.timer, 600, 60, TimeUnit.SECONDS);
			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(StatsTimer.timer, 0, 300, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
