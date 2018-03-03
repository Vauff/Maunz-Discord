package com.vauff.maunzdiscord.core;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import java.io.File;

public class Main
{
	public static IDiscordClient client;
	public static String version = "2.4.1";
	public static Logger log;

	public static void main(String[] args) throws DiscordException
	{
		try
		{
			File file = new File(Util.getJarLocation() + "config.json");
			File logFile = new File("maunzdiscord.log");
			File oldLogFile = new File("maunzdiscord-old.log");
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
				json.put("botOwnerID", 0L);
				json.put("cleverbotAPIKey", "");
				json.put("database", new JSONObject());
				json.getJSONObject("database").put("hostname", "");
				json.getJSONObject("database").put("username", "");
				json.getJSONObject("database").put("password", "");
				FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
			}

			Thread.sleep(3000);
			oldLogFile.delete();
			logFile.renameTo(oldLogFile);
			log = LogManager.getLogger(Main.class);

			if (!json.getString("discordToken").equals(""))
			{
				log.info("Starting Maunz-Discord v" + version);
				Util.token = json.getString("discordToken");
			}
			else
			{
				log.fatal("You need to provide a bot token to run Maunz, please add one obtained from https://discordapp.com/developers/applications/me to the discordToken option in config.json");
				System.exit(1);
			}

			client = new ClientBuilder().withToken(Util.token).login();
			client.getDispatcher().registerListener(new ReadyEventListener());
		}
		catch (Exception e)
		{
			log.error("", e);
		}
	}
}
