package com.vauff.maunzdiscord.core;

import com.vauff.maunzdiscord.commands.*;
import com.vauff.maunzdiscord.threads.MessageCreateThread;
import com.vauff.maunzdiscord.threads.ReactionAddThread;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.util.Snowflake;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

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
	 * Sets up all commands
	 */
	public MainListener()
	{
		try
		{
			JSONObject json = new JSONObject(Util.getFileContents("config.json"));

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

			if (json.getJSONObject("database").getString("hostname").equals("") || json.getJSONObject("database").getString("username").equals("") || json.getJSONObject("database").getString("password").equals(""))
			{
				Logger.log.warn("The quote command is disabled due to 1 or more values in the database section of config.json not being supplied");
			}
			else
			{
				commands.add(new Quote());
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onMessageCreate(MessageCreateEvent event)
	{
		MessageCreateThread thread = new MessageCreateThread(event, "messagereceived-" + event.getMessage().getId().asString());
		thread.start();
	}

	public static void onReactionAdd(ReactionAddEvent event)
	{
		ReactionAddThread thread = new ReactionAddThread(event, "reactionadd-" + event.getMessage().block().getId().asString());
		thread.start();
	}

	public static void onGuildCreate(GuildCreateEvent event)
	{
		try
		{
			File file = new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().getId().asString() + ".json");

			if (!file.exists())
			{
				JSONObject json = new JSONObject();

				file.createNewFile();
				json.put("enabled", true);
				json.put("lastGuildName", event.getGuild().getName());
				json.put("blacklist", new JSONArray());
				FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
