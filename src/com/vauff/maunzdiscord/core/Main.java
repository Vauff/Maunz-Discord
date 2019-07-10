package com.vauff.maunzdiscord.core;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class Main
{
	public static DiscordClient client;
	public static MongoDatabase mongoDatabase;
	public static String version = "r9";

	public static void main(String[] args)
	{
		try
		{
			File file = new File(Util.getJarLocation() + "config.json");
			File logFile = new File("maunz.log");
			File oldLogFile = new File("maunz-old.log");
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
				json.put("botOwners", new JSONArray());
				json.put("mongoDatabase", new JSONObject());
				json.getJSONObject("mongoDatabase").put("connectionString", "");
				json.getJSONObject("mongoDatabase").put("database", "");
				json.put("quotesDatabase", new JSONObject());
				json.getJSONObject("quotesDatabase").put("hostname", "");
				json.getJSONObject("quotesDatabase").put("username", "");
				json.getJSONObject("quotesDatabase").put("password", "");
				json.getJSONObject("quotesDatabase").put("database", "");
				FileUtils.writeStringToFile(file, json.toString(4), "UTF-8");
			}

			Thread.sleep(2000);
			oldLogFile.delete();
			logFile.renameTo(oldLogFile);
			Logger.log = LogManager.getLogger(Main.class);

			if (json.getString("discordToken").equals(""))
			{
				Logger.log.fatal("You need to provide a bot token to run Maunz, please add one obtained from https://discordapp.com/developers/applications/me to the discordToken option in config.json");
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
			Util.token = json.getString("discordToken");
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

			client = new DiscordClientBuilder(Util.token).build();
			client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> MainListener.onReady());
			client.login().block();
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
