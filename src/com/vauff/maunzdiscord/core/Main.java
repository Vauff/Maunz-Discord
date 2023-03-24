package com.vauff.maunzdiscord.core;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.vauff.maunzdiscord.commands.*;
import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.servertracking.MapImageTimer;
import com.vauff.maunzdiscord.servertracking.ServerTimer;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.gateway.GatewayReactorResources;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.RestClient;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

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
	public static String version = "r54";
	public static Config cfg;

	/**
	 * A watch to keep track of the uptime of the bot
	 */
	public static StopWatch uptime = new StopWatch();

	/**
	 * List that holds all commands
	 */
	public static LinkedList<AbstractCommand<ChatInputInteractionEvent>> commands = new LinkedList<>();

	/**
	 * Cached Guild objects, to avoid constant getGuildById calls to Discord API
	 */
	public static HashMap<Snowflake, Guild> guildCache = new HashMap<>();

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
				.setEnabledIntents(IntentSet.nonPrivileged().andNot(IntentSet.of(Intent.MESSAGE_CONTENT)))
				.setGatewayReactorResources(resources -> GatewayReactorResources.builder(resources)
					.httpClient(HttpClient.create(ConnectionProvider.newConnection())
						.compress(true)
						.followRedirect(true)
						.secure())
					.build())
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
			gateway.on(MessageCreateEvent.class, event -> Mono.fromRunnable(() -> Logger.onMessageCreate(event))).subscribe();
			gateway.on(MessageUpdateEvent.class, event -> Mono.fromRunnable(() -> Logger.onMessageUpdate(event))).subscribe();
			gateway.on(ButtonInteractionEvent.class, event -> Mono.fromRunnable(() -> Logger.onButtonInteraction(event))).subscribe();
			gateway.on(GuildDeleteEvent.class, event -> Mono.fromRunnable(() -> Logger.onGuildDelete(event))).subscribe();
			gateway.on(ChatInputInteractionEvent.class, event -> Mono.fromRunnable(() -> MainListener.onChatInputInteraction(event))).subscribe();
			gateway.on(ButtonInteractionEvent.class, event -> Mono.fromRunnable(() -> MainListener.onButtonInteraction(event))).subscribe();
			gateway.on(GuildDeleteEvent.class, event -> Mono.fromRunnable(() -> MainListener.onGuildDelete(event))).subscribe();

			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(MapImageTimer.timer, 0, 1, TimeUnit.HOURS);
			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(StatsTimer.timer, 0, 5, TimeUnit.MINUTES);
			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(ServerTimer.timer, 10, 60, TimeUnit.SECONDS);

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
		commands.add(new About());
		commands.add(new Benchmark());
		commands.add(new Changelog());
		commands.add(new Colour());
		commands.add(new Help());
		commands.add(new Invite());
		commands.add(new IsItDown());
		commands.add(new Map());
		commands.add(new Minecraft());
		commands.add(new Notify());
		commands.add(new Ping());
		commands.add(new Players());
		commands.add(new Reddit());
		commands.add(new Say());
		commands.add(new Servers());
		commands.add(new Steam());
		commands.add(new Stop());
		commands.sort(Comparator.comparing(AbstractCommand::getName));

		RestClient restClient = gateway.getRestClient();
		long appID = restClient.getApplicationId().block();
		ArrayList<ApplicationCommandRequest> cmdRequests = new ArrayList<>();


		for (AbstractCommand<ChatInputInteractionEvent> cmd : commands)
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
		for (AbstractCommand<ChatInputInteractionEvent> cmd : commands)
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
