package com.vauff.maunzdiscord.core;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.vauff.maunzdiscord.commands.legacy.*;
import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.timers.ServerTimer;
import com.vauff.maunzdiscord.timers.StatsTimer;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main
{
	public static GatewayDiscordClient gateway;
	public static MongoDatabase mongoDatabase;
	public static String version = "r43";
	public static Config cfg;

	/**
	 * A watch to keep track of the uptime of the bot
	 */
	public static StopWatch uptime = new StopWatch();

	/**
	 * Lists that hold all legacy/slash commands
	 */
	public static LinkedList<AbstractLegacyCommand<MessageCreateEvent>> legacyCommands = new LinkedList<>();
	public static LinkedList<AbstractSlashCommand<ChatInputInteractionEvent>> slashCommands = new LinkedList<>();
	public static LinkedList<AbstractCommand> commands = new LinkedList<>();

	public static void main(String[] args)
	{
		try
		{
			boolean exit = false;

			Logger.log = LogManager.getLogger(Main.class);
			cfg = new Config();

			if (cfg.getToken().equals(""))
			{
				Logger.log.fatal("You need to provide a bot token to run Maunz, please add one obtained from https://discord.com/developers/applications to the discordToken option in config.json");
				exit = true;
			}

			if (cfg.getConnectionString().equals("") || cfg.getDatabase().equals(""))
			{
				Logger.log.fatal("You need to fill in all values of the mongoDatabase section of config.json to run Maunz");
				exit = true;
			}

			if (exit)
			{
				System.exit(1);
			}

			Logger.log.info("Starting Maunz-Discord " + version + "...");
			Logger.log.info("Connecting to MongoDB (" + cfg.getConnectionString() + ")");

			try
			{
				mongoDatabase = MongoClients.create(cfg.getConnectionString()).getDatabase(cfg.getDatabase());
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
				Logger.log.fatal("An error occured while connecting to the MongoDB database");
				System.exit(1);
			}

			gateway = DiscordClient.builder(cfg.getToken()).build().gateway().withEventDispatcher(eventDispatcher ->
				{
					var event1 = eventDispatcher.on(GuildCreateEvent.class).doOnNext(MainListener::onGuildCreate);
					var event2 = eventDispatcher.on(GuildCreateEvent.class).doOnNext(Logger::onGuildCreate);
					return Mono.when(event1, event2);
				})
				.login().block();

			uptime.start();
			setupCommands();

			gateway.on(ChatInputInteractionEvent.class).subscribe(Logger::onChatInputInteraction);
			gateway.on(MessageCreateEvent.class).subscribe(Logger::onMessageCreate);
			gateway.on(MessageUpdateEvent.class).subscribe(Logger::onMessageUpdate);
			gateway.on(ReactionAddEvent.class).subscribe(Logger::onReactionAdd);
			gateway.on(ReactionRemoveEvent.class).subscribe(Logger::onReactionRemove);
			gateway.on(GuildDeleteEvent.class).subscribe(Logger::onGuildDelete);
			gateway.on(MessageCreateEvent.class).subscribe(MainListener::onMessageCreate);
			gateway.on(ChatInputInteractionEvent.class).subscribe(MainListener::onChatInputInteraction);
			gateway.on(ReactionAddEvent.class).subscribe(MainListener::onReactionAdd);

			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(ServerTimer.timer, 0, 60, TimeUnit.SECONDS);
			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(StatsTimer.timer, 0, 300, TimeUnit.SECONDS);

			// Keep app alive by waiting for disconnect
			gateway.onDisconnect().block();
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	/**
	 * Fills command arrays and registers appropriate slash commands with Discord
	 */
	public static void setupCommands()
	{
		legacyCommands.add(new About());
		legacyCommands.add(new Benchmark());
		legacyCommands.add(new Blacklist());
		legacyCommands.add(new Changelog());
		legacyCommands.add(new Colour());
		legacyCommands.add(new Discord());
		legacyCommands.add(new Help());
		legacyCommands.add(new IsItDown());
		legacyCommands.add(new Map());
		slashCommands.add(new com.vauff.maunzdiscord.commands.slash.Minecraft());
		legacyCommands.add(new Notify());
		slashCommands.add(new com.vauff.maunzdiscord.commands.slash.Ping());
		legacyCommands.add(new Players());
		slashCommands.add(new com.vauff.maunzdiscord.commands.slash.Reddit());
		legacyCommands.add(new Say());
		slashCommands.add(new com.vauff.maunzdiscord.commands.slash.Services());
		legacyCommands.add(new Steam());
		slashCommands.add(new com.vauff.maunzdiscord.commands.slash.Stop());

		commands.addAll(legacyCommands);
		commands.addAll(slashCommands);
		commands.sort(Comparator.comparing(AbstractCommand::getFirstAlias));

		RestClient restClient = gateway.getRestClient();
		long appID = restClient.getApplicationId().block();
		ArrayList<ApplicationCommandRequest> cmdRequests = new ArrayList<>();

		for (AbstractSlashCommand<ChatInputInteractionEvent> cmd : slashCommands)
			cmdRequests.add(cmd.getCommand());

		if (cfg.getDevGuilds().length > 0)
		{
			for (int i = 0; i < cfg.getDevGuilds().length; i++)
				restClient.getApplicationService().bulkOverwriteGuildApplicationCommand(appID, cfg.getDevGuilds()[i], cmdRequests).blockLast();

			// Remove all global commands while in dev mode
			cmdRequests = new ArrayList<>();
		}

		restClient.getApplicationService().bulkOverwriteGlobalApplicationCommand(appID, cmdRequests).blockLast();
	}
}
