package com.vauff.maunzdiscord.core;

import com.mongodb.client.MongoCollection;
import com.vauff.maunzdiscord.threads.InteractionCreateThread;
import com.vauff.maunzdiscord.threads.MessageCreateThread;
import com.vauff.maunzdiscord.threads.ReactionAddThread;
import com.vauff.maunzdiscord.timers.ServerTimer;
import com.vauff.maunzdiscord.timers.StatsTimer;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.http.client.ClientException;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

public class MainListener
{
	/**
	 * Holds the timestamp of the last time a user used a command
	 */
	public static HashMap<Snowflake, Long> cooldownTimestamps = new HashMap<>();

	/**
	 * Holds the timestamp of the last time a user was given the command cooldown message
	 */
	public static HashMap<Snowflake, Long> cooldownMessageTimestamps = new HashMap<>();

	/**
	 * Whether onReady has been fired during this bot session
	 */
	private static boolean onReadyFired = false;

	public static void onReady()
	{
		try
		{
			if (onReadyFired)
				return;

			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(ServerTimer.timer, 0, 60, TimeUnit.SECONDS);
			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(StatsTimer.timer, 0, 300, TimeUnit.SECONDS);
			onReadyFired = true;
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onGuildCreate(GuildCreateEvent event)
	{
		long guildId = event.getGuild().getId().asLong();
		String guildName = event.getGuild().getName();
		MongoCollection<Document> col = Main.mongoDatabase.getCollection("guilds");

		if (col.countDocuments(eq("guildId", guildId)) == 0L)
		{
			Document doc = new Document("guildId", guildId).append("enabled", true).append("lastGuildName", guildName).append("blacklist", new ArrayList());
			col.insertOne(doc);
		}
		else if (!col.find(eq("guildId", guildId)).first().getString("lastGuildName").equals(guildName))
		{
			col.updateOne(eq("guildId", guildId), new Document("$set", new Document("lastGuildName", guildName)));
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

	public static void onInteractionCreate(InteractionCreateEvent event)
	{
		try
		{
			new InteractionCreateThread(event, "interactioncreate-" + event.getInteraction().getId().asString()).start();
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
