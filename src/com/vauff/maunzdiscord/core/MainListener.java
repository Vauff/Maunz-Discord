package com.vauff.maunzdiscord.core;

import com.mongodb.client.MongoCollection;
import com.vauff.maunzdiscord.commands.legacy.*;
import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.threads.InteractionCreateThread;
import com.vauff.maunzdiscord.threads.MessageCreateThread;
import com.vauff.maunzdiscord.threads.ReactionAddThread;
import com.vauff.maunzdiscord.timers.*;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.rest.RestClient;
import discord4j.rest.http.client.ClientException;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

public class MainListener
{
	/**
	 * Lists that hold all legacy/slash commands
	 */
	public static LinkedList<AbstractLegacyCommand<MessageCreateEvent>> legacyCommands = new LinkedList<>();
	public static LinkedList<AbstractSlashCommand<ApplicationCommandInteraction>> slashCommands = new LinkedList<>();
	public static LinkedList<AbstractCommand> commands = new LinkedList<>();


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

			uptime.start();

			for (Guild guild : Main.gateway.getGuilds().toIterable())
			{
				setupGuild(guild);
			}

			legacyCommands.add(new About());
			legacyCommands.add(new Benchmark());
			legacyCommands.add(new Blacklist());
			legacyCommands.add(new Changelog());
			legacyCommands.add(new Colour());
			legacyCommands.add(new Disable());
			legacyCommands.add(new Discord());
			legacyCommands.add(new Enable());
			legacyCommands.add(new Help());
			legacyCommands.add(new IsItDown());
			legacyCommands.add(new Map());
			legacyCommands.add(new Minecraft());
			legacyCommands.add(new Notify());
			legacyCommands.add(new Ping());
			slashCommands.add(new com.vauff.maunzdiscord.commands.slash.Ping());
			legacyCommands.add(new Players());
			legacyCommands.add(new Reddit());
			legacyCommands.add(new Restart());
			legacyCommands.add(new Say());
			legacyCommands.add(new Services());
			slashCommands.add(new com.vauff.maunzdiscord.commands.slash.Services());
			legacyCommands.add(new Source());
			legacyCommands.add(new Steam());
			legacyCommands.add(new Stop());

			commands.addAll(legacyCommands);
			commands.addAll(slashCommands);
			commands.sort(Comparator.comparing(AbstractCommand::getFirstAlias));

			Main.gateway.getEventDispatcher().on(MessageCreateEvent.class).subscribe(Logger::onMessageCreate);
			Main.gateway.getEventDispatcher().on(MessageUpdateEvent.class).subscribe(Logger::onMessageUpdate);
			Main.gateway.getEventDispatcher().on(MessageDeleteEvent.class).subscribe(Logger::onMessageDelete);
			Main.gateway.getEventDispatcher().on(ReactionAddEvent.class).subscribe(Logger::onReactionAdd);
			Main.gateway.getEventDispatcher().on(ReactionRemoveEvent.class).subscribe(Logger::onReactionRemove);
			Main.gateway.getEventDispatcher().on(GuildCreateEvent.class).subscribe(Logger::onGuildCreate);
			Main.gateway.getEventDispatcher().on(GuildDeleteEvent.class).subscribe(Logger::onGuildDelete);
			Main.gateway.getEventDispatcher().on(MessageCreateEvent.class).subscribe(MainListener::onMessageCreate);
			Main.gateway.getEventDispatcher().on(InteractionCreateEvent.class).subscribe(MainListener::onInteractionCreate);
			Main.gateway.getEventDispatcher().on(ReactionAddEvent.class).subscribe(MainListener::onReactionAdd);
			Main.gateway.getEventDispatcher().on(GuildCreateEvent.class).subscribe(MainListener::onGuildCreate);

			JSONArray devGuilds = new JSONObject(Util.getFileContents("config.json")).getJSONArray("devGuilds");
			RestClient restClient = Main.gateway.getRestClient();

			if (devGuilds.length() > 0)
			{
				for (int i = 0; i < devGuilds.length(); i++)
				{
					for (AbstractSlashCommand<ApplicationCommandInteraction> cmd : slashCommands)
						restClient.getApplicationService().createGuildApplicationCommand(restClient.getApplicationId().block(), devGuilds.getLong(i), cmd.getCommand()).block();
				}
			}
			else
			{
				for (AbstractSlashCommand<ApplicationCommandInteraction> cmd : slashCommands)
					restClient.getApplicationService().createGlobalApplicationCommand(restClient.getApplicationId().block(), cmd.getCommand()).block();
			}

			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(ServerTimer.timer, 0, 60, TimeUnit.SECONDS);
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

	public static void onInteractionCreate(InteractionCreateEvent event)
	{
		try
		{
			event.acknowledge();
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
