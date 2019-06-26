package com.vauff.maunzdiscord.core;

import com.vauff.maunzdiscord.commands.*;
import com.vauff.maunzdiscord.features.*;
import com.vauff.maunzdiscord.threads.MessageCreateThread;
import com.vauff.maunzdiscord.threads.ReactionAddThread;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;
import discord4j.rest.http.client.ClientException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainListener
{
	/**
	 * Holds all commands
	 */
	public static LinkedList<AbstractCommand<MessageCreateEvent>> commands = new LinkedList<>();

	/**
	 * Holds the timestamp of the last time a user used a command
	 */
	public static HashMap<Snowflake, Long> cooldownTimestamps = new HashMap<>();

	/**
	 * Holds the timestamp of the last time a user was given the command cooldown message
	 */
	public static HashMap<Snowflake, Long> cooldownMessageTimestamps = new HashMap<>();

	/**
	 * A watch to keep track of the uptime of the bot
	 */
	public static StopWatch uptime = new StopWatch();

	public static void onReady()
	{
		try
		{
			List<File> folderList = new ArrayList<>();
			JSONObject configJson = new JSONObject(Util.getFileContents("config.json"));

			uptime.start();
			Help.setupCmdHelp();
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
				File file = new File(Util.getJarLocation() + "data/guilds/" + guild.getId().asString() + ".json");

				if (!file.exists())
				{
					JSONObject json = new JSONObject();

					file.createNewFile();
					json.put("enabled", true);
					json.put("lastGuildName", guild.getName());
					json.put("language", "english");
					json.put("blacklist", new JSONArray());
					FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
				}
			}

			Main.client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(Logger::onMessageCreate);
			Main.client.getEventDispatcher().on(MessageUpdateEvent.class).subscribe(Logger::onMessageUpdate);
			Main.client.getEventDispatcher().on(MessageDeleteEvent.class).subscribe(Logger::onMessageDelete);
			Main.client.getEventDispatcher().on(ReactionAddEvent.class).subscribe(Logger::onReactionAdd);
			Main.client.getEventDispatcher().on(ReactionRemoveEvent.class).subscribe(Logger::onReactionRemove);
			Main.client.getEventDispatcher().on(GuildCreateEvent.class).subscribe(Logger::onGuildCreate);
			Main.client.getEventDispatcher().on(GuildDeleteEvent.class).subscribe(Logger::onGuildDelete);
			commands.add(new About());
			commands.add(new Benchmark());
			commands.add(new Blacklist());
			commands.add(new Changelog());
			commands.add(new Colour());
			commands.add(new Disable());
			commands.add(new Discord());
			commands.add(new Enable());
			commands.add(new Help());
			commands.add(new IsItDown());
			commands.add(new Map());
			commands.add(new Minecraft());
			commands.add(new Notify());
			commands.add(new Ping());
			commands.add(new Players());
			commands.add(new Reddit());
			commands.add(new Restart());
			commands.add(new Say());
			commands.add(new Services());
			commands.add(new Source());
			commands.add(new Steam());
			commands.add(new Stop());

			if (configJson.getJSONObject("database").getString("hostname").equals("") || configJson.getJSONObject("database").getString("username").equals("") || configJson.getJSONObject("database").getString("password").equals(""))
			{
				Logger.log.warn("The quote command is disabled due to 1 or more values in the database section of config.json not being supplied");
			}
			else
			{
				commands.add(new Quote());
			}

			Main.client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(MainListener::onMessageCreate);
			Main.client.getEventDispatcher().on(ReactionAddEvent.class).subscribe(MainListener::onReactionAdd);
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(ServerTimer.timer, 0, 60, TimeUnit.SECONDS);
			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(StatsTimer.timer, 0, 300, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onMessageCreate(MessageCreateEvent event)
	{
		try
		{
			new MessageCreateThread(event, "messagereceived-" + event.getMessage().getId().asString()).start();
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onReactionAdd(ReactionAddEvent event)
	{
		try
		{
			Message message;

			try
			{
				message = event.getMessage().block();
			}
			catch (ClientException e)
			{
				//message was deleted too quick for us to get anything about it, never ours when this happens anyways
				return;
			}

			new ReactionAddThread(event, message, "reactionadd-" + message.getId().asString()).start();
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
