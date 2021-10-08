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
import discord4j.rest.RestClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main
{
	public static GatewayDiscordClient gateway;
	public static MongoDatabase mongoDatabase;
	public static String version = "r40";
	public static String prefix;

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
			File file = new File(Util.getJarLocation() + "config.json");
			boolean exit = false;
			JSONObject json;

			if (file.exists())
			{
				json = new JSONObject(Util.getFileContents(file));
			}
			else
			{
				json = new JSONObject();
				file.createNewFile();
				json.put("enabled", true);
				json.put("discordToken", "");
				json.put("altPlayingText", "discord.gg/v55fW9b");
				json.put("userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.61 Safari/537.36");
				json.put("botOwners", new JSONArray());
				json.put("devGuilds", new JSONArray());
				json.put("prefix", "*");
				json.put("mongoDatabase", new JSONObject());
				json.getJSONObject("mongoDatabase").put("connectionString", "");
				json.getJSONObject("mongoDatabase").put("database", "");
				FileUtils.writeStringToFile(file, json.toString(4), "UTF-8");
			}

			Logger.log = LogManager.getLogger(Main.class);
			prefix = json.getString("prefix");

			if (json.getString("discordToken").equals(""))
			{
				Logger.log.fatal("You need to provide a bot token to run Maunz, please add one obtained from https://discord.com/developers/applications to the discordToken option in config.json");
				exit = true;
			}

			if (json.getJSONObject("mongoDatabase").getString("connectionString").equals("") || json.getJSONObject("mongoDatabase").getString("database").equals(""))
			{
				Logger.log.fatal("You need to fill in all values of the mongoDatabase section of config.json to run Maunz");
				exit = true;
			}

			if (exit)
			{
				System.exit(1);
			}

			Logger.log.info("Starting Maunz-Discord " + version + "...");
			Logger.log.info("Connecting to MongoDB (" + json.getJSONObject("mongoDatabase").getString("connectionString") + ")");

			try
			{
				mongoDatabase = MongoClients.create(json.getJSONObject("mongoDatabase").getString("connectionString")).getDatabase(json.getJSONObject("mongoDatabase").getString("database"));
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
				Logger.log.fatal("An error occured while connecting to the MongoDB database");
				System.exit(1);
			}

			buildBot(json.getString("discordToken"));
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	/**
	 * Builds and sets up the primary Discord4J bot object
	 */
	public static void buildBot(String token) throws Exception
	{
		gateway = DiscordClient.builder(token).build().gateway().withEventDispatcher(eventDispatcher ->
			{
				var event1 = eventDispatcher.on(GuildCreateEvent.class).doOnNext(MainListener::onGuildCreate);
				var event2 = eventDispatcher.on(GuildCreateEvent.class).doOnNext(Logger::onGuildCreate);
				return Mono.when(event1, event2);
			})
			.login().block();

		uptime.start();

		JSONArray devGuilds = new JSONObject(Util.getFileContents("config.json")).getJSONArray("devGuilds");
		RestClient restClient = gateway.getRestClient();

		if (devGuilds.length() > 0)
		{
			for (int i = 0; i < devGuilds.length(); i++)
			{
				for (AbstractSlashCommand<ChatInputInteractionEvent> cmd : slashCommands)
					restClient.getApplicationService().createGuildApplicationCommand(restClient.getApplicationId().block(), devGuilds.getLong(i), cmd.getCommand()).block();
			}
		}
		else
		{
			for (AbstractSlashCommand<ChatInputInteractionEvent> cmd : slashCommands)
				restClient.getApplicationService().createGlobalApplicationCommand(restClient.getApplicationId().block(), cmd.getCommand()).block();
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
}
