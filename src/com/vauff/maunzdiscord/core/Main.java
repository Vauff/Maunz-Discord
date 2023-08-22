package com.vauff.maunzdiscord.core;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.vauff.maunzdiscord.commands.*;
import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.servertracking.MapImageTimer;
import com.vauff.maunzdiscord.servertracking.ServerTrackingLoop;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main
{
	public static GatewayDiscordClient gateway;
	public static MongoDatabase mongoDatabase;
	public static String version = "r55";
	public static Config cfg;

	/**
	 * A watch to keep track of the uptime of the bot
	 */
	public static StopWatch uptime = new StopWatch();

	/**
	 * List that holds all commands
	 */
	public static HashMap<String, AbstractCommand<ChatInputInteractionEvent>> commands = new HashMap<>();

	/**
	 * Cached Guild objects, to avoid constant getGuildById calls to Discord API
	 */
	public static HashMap<Snowflake, Guild> guildCache = new HashMap<>();

	/**
	 * Current shutdown state of the bot
	 *
	 * NONE - Bot is not shutting down
	 * SHUTDOWN_QUEUED - A bot shutdown is queued, waiting for server tracking loop & active threads to finish
	 * SHUTDOWN_SAFE - Server tracking has fully finished, bot is now shutting down
	 */
	public enum ShutdownState
	{
		NONE,
		SHUTDOWN_QUEUED,
		SHUTDOWN_SAFE
	}

	public static ShutdownState shutdownState = ShutdownState.NONE;

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

			if (cfg.getMongoConnectionString().equals("") || cfg.getMongoDatabase().equals(""))
			{
				Logger.log.fatal("You need to fill in all values of the mongoDatabase section of config.json to run Maunz");
				exit = true;
			}

			if (exit)
			{
				System.exit(1);
			}

			Logger.log.info("Starting Maunz-Discord " + version + "...");
			Logger.log.info("Connecting to MongoDB (" + cfg.getMongoConnectionString() + ")");

			try
			{
				mongoDatabase = MongoClients.create(cfg.getMongoConnectionString()).getDatabase(cfg.getMongoDatabase());
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
				Logger.log.fatal("An error occured while connecting to the MongoDB database");
				System.exit(1);
			}

			gateway = DiscordClient.create(cfg.getToken()).gateway()
				.withEventDispatcher(eventDispatcher ->
				{
					var event1 = eventDispatcher.on(GuildCreateEvent.class).doOnNext(MainListener::onGuildCreate);
					var event2 = eventDispatcher.on(GuildCreateEvent.class).doOnNext(Logger::onGuildCreate);
					return Mono.when(event1, event2);
				})
				.login().block();

			uptime.start();
			setupCommands();

			gateway.on(ChatInputInteractionEvent.class, event -> Mono.fromRunnable(() -> Logger.onChatInputInteraction(event))).subscribe();
			gateway.on(ButtonInteractionEvent.class, event -> Mono.fromRunnable(() -> Logger.onButtonInteraction(event))).subscribe();
			gateway.on(GuildDeleteEvent.class, event -> Mono.fromRunnable(() -> Logger.onGuildDelete(event))).subscribe();
			gateway.on(ChatInputInteractionEvent.class, event -> Mono.fromRunnable(() -> MainListener.onChatInputInteraction(event))).subscribe();
			gateway.on(ButtonInteractionEvent.class, event -> Mono.fromRunnable(() -> MainListener.onButtonInteraction(event))).subscribe();
			gateway.on(GuildDeleteEvent.class, event -> Mono.fromRunnable(() -> MainListener.onGuildDelete(event))).subscribe();

			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(MapImageTimer.timer, 0, 1, TimeUnit.HOURS);
			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(PresenceTimer.timer, 2, 5, TimeUnit.MINUTES);

			// Initialize the server tracking main loop
			new ServerTrackingLoop().start();

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
	private static void setupCommands()
	{
		LinkedList<AbstractCommand<ChatInputInteractionEvent>> commandsArray = new LinkedList<>();

		commandsArray.add(new About());
		commandsArray.add(new Benchmark());
		commandsArray.add(new Changelog());
		commandsArray.add(new Colour());
		commandsArray.add(new Help());
		commandsArray.add(new Invite());
		commandsArray.add(new IsItDown());
		commandsArray.add(new Map());
		commandsArray.add(new Minecraft());
		commandsArray.add(new Notify());
		commandsArray.add(new Ping());
		commandsArray.add(new Players());
		commandsArray.add(new Reddit());
		commandsArray.add(new Say());
		commandsArray.add(new Servers());
		commandsArray.add(new Steam());
		commandsArray.add(new Stop());
		commandsArray.sort(Comparator.comparing(AbstractCommand::getName));

		for (AbstractCommand<ChatInputInteractionEvent> cmd : commandsArray)
			commands.put(cmd.getName(), cmd);

		RestClient restClient = gateway.getRestClient();
		long appID = restClient.getApplicationId().block();
		ArrayList<ApplicationCommandRequest> cmdRequests = new ArrayList<>();

		for (AbstractCommand<ChatInputInteractionEvent> cmd : commands.values())
			cmdRequests.add(cmd.getCommandRequest());

		if (cfg.getDevGuilds().length > 0)
		{
			for (int i = 0; i < cfg.getDevGuilds().length; i++)
			{
				long guildId = cfg.getDevGuilds()[i];
				List<ApplicationCommandData> cmdDatas = restClient.getApplicationService().bulkOverwriteGuildApplicationCommand(appID, guildId, cmdRequests).collectList().block();

				mapCommandDataToCommands(cmdDatas, guildId);
			}

			// Remove all global commands while in dev mode
			cmdRequests = new ArrayList<>();
		}


		List<ApplicationCommandData> cmdDatas = restClient.getApplicationService().bulkOverwriteGlobalApplicationCommand(appID, cmdRequests).collectList().block();

		if (cmdRequests.size() > 0)
			mapCommandDataToCommands(cmdDatas, 0L);
	}

	private static void mapCommandDataToCommands(List<ApplicationCommandData> cmdDatas, long guildId)
	{
		parentLoop:
		for (AbstractCommand<ChatInputInteractionEvent> cmd : commands.values())
		{
			for (ApplicationCommandData cmdData : cmdDatas)
			{
				if (cmd.getName().equals(cmdData.name()))
				{
					cmd.cmdDatas.put(guildId, cmdData);
					continue parentLoop;
				}
			}
		}
	}
}
