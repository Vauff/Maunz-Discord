package com.vauff.maunzdiscord.core;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import java.io.File;

public class Main
{
	public static IDiscordClient client;
	public static String version = "3.0";

	public static void main(String[] args) throws DiscordException
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
				Logger.log.info("Starting Maunz-Discord v" + version);
				Util.token = json.getString("discordToken");
			}
			else
			{
				Logger.log.fatal("You need to provide a bot token to run Maunz, please add one obtained from https://discordapp.com/developers/applications/me to the discordToken option in config.json");
				System.exit(1);
			}

			client = new ClientBuilder().withToken(Util.token).login();
			client.getDispatcher().registerListener(new ReadyEventListener());
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
