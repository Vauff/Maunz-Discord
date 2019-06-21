package com.vauff.maunzdiscord.core;

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
	public static String version = "r2";

	public static void main(String[] args)
	{
		try
		{
			File file = new File(Util.getJarLocation() + "config.json");
			File logFile = new File("maunz.log");
			File oldLogFile = new File("maunz-old.log");
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
				json.put("botOwners", new JSONArray());
				json.put("database", new JSONObject());
				json.getJSONObject("database").put("hostname", "");
				json.getJSONObject("database").put("username", "");
				json.getJSONObject("database").put("password", "");
				FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
			}

			Thread.sleep(2000);
			oldLogFile.delete();
			logFile.renameTo(oldLogFile);
			Logger.log = LogManager.getLogger(Main.class);

			if (!json.getString("discordToken").equals(""))
			{
				Logger.log.info("Starting Maunz-Discord " + version);
				Util.token = json.getString("discordToken");
			}
			else
			{
				Logger.log.fatal("You need to provide a bot token to run Maunz, please add one obtained from https://discordapp.com/developers/applications/me to the discordToken option in config.json");
				System.exit(1);
			}

			client = new DiscordClientBuilder(Util.token).build();
			client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> ReadyEventListener.onReady());
			client.login().block();
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
