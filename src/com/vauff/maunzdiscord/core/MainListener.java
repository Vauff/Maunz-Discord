package com.vauff.maunzdiscord.core;

import com.mongodb.client.MongoCollection;
import com.vauff.maunzdiscord.commands.*;
import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.threads.MessageCreateThread;
import com.vauff.maunzdiscord.threads.ReactionAddThread;
import com.vauff.maunzdiscord.timers.*;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.rest.http.client.ClientException;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

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
			if (uptime.isStarted())
				return;

			JSONObject configJson = new JSONObject(Util.getFileContents("config.json"));

			uptime.start();

			for (Guild guild : Main.gateway.getGuilds().toIterable())
			{
				setupGuild(guild);
			}


			Main.gateway.getEventDispatcher().on(MessageCreateEvent.class).subscribe(Logger::onMessageCreate);
			Main.gateway.getEventDispatcher().on(MessageUpdateEvent.class).subscribe(Logger::onMessageUpdate);
			Main.gateway.getEventDispatcher().on(MessageDeleteEvent.class).subscribe(Logger::onMessageDelete);
			Main.gateway.getEventDispatcher().on(ReactionAddEvent.class).subscribe(Logger::onReactionAdd);
			Main.gateway.getEventDispatcher().on(ReactionRemoveEvent.class).subscribe(Logger::onReactionRemove);
			Main.gateway.getEventDispatcher().on(GuildCreateEvent.class).subscribe(Logger::onGuildCreate);
			Main.gateway.getEventDispatcher().on(GuildDeleteEvent.class).subscribe(Logger::onGuildDelete);
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

			Main.gateway.getEventDispatcher().on(MessageCreateEvent.class).subscribe(MainListener::onMessageCreate);
			Main.gateway.getEventDispatcher().on(ReactionAddEvent.class).subscribe(MainListener::onReactionAdd);
			Main.gateway.getEventDispatcher().on(GuildCreateEvent.class).subscribe(MainListener::onGuildCreate);
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(ServerTimer.timer, 0, 60, TimeUnit.SECONDS);
			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(StatsTimer.timer, 0, 300, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onGuildCreate(GuildCreateEvent event)
	{
		setupGuild(event.getGuild());
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

	public static void setupGuild(Guild guild)
	{
		MongoCollection<Document> col = Main.mongoDatabase.getCollection("guilds");

		if (col.countDocuments(eq("guildId", guild.getId().asLong())) == 0L)
		{
			Document doc = new Document("guildId", guild.getId().asLong()).append("enabled", true).append("lastGuildName", guild.getName()).append("blacklist", new ArrayList());
			col.insertOne(doc);
		}
		else if (!col.find(eq("guildId", guild.getId().asLong())).first().getString("lastGuildName").equals(guild.getName()))
		{
			col.updateOne(eq("guildId", guild.getId().asLong()), new Document("$set", new Document("lastGuildName", guild.getName())));
		}
	}
}
