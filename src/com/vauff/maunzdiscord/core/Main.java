package com.vauff.maunzdiscord.core;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vauff.maunzdiscord.features.CsgoUpdateBot;

import org.json.JSONObject;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

public class Main
{
	public static IDiscordClient client;
	public static CsgoUpdateBot bot;
	public static String version = "2.2";
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
				json.put("discordToken", "");
				json.put("discordDevToken", "");
				json.put("databasePassword", "");
				json.put("cleverbotAPIKey", "");
				FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
			}

			Thread.sleep(3000);
			oldLogFile.delete();
			logFile.renameTo(oldLogFile);
			log = LogManager.getLogger(Main.class);

			if (args.length >= 1 && args[0].equals("-dev"))
			{
				if (!json.getString("discordDevToken").equals(""))
				{
					log.info("Starting Maunz-Discord v" + version + " in dev mode");
					Util.token = json.getString("discordDevToken");
					Util.devMode = true;
					CsgoUpdateBot.listeningNick = "Vauff";
				}
				else
				{
					log.fatal("You need to provide a bot token to run Maunz, please add one obtained from https://discordapp.com/developers/applications/me to the discordDevToken option in config.json");
					System.exit(1);
				}
			}
			else
			{
				if (!json.getString("discordToken").equals(""))
				{
					log.info("Starting Maunz-Discord v" + version);
					Util.token = json.getString("discordToken");
					Util.devMode = false;
					CsgoUpdateBot.listeningNick = "SteamDB";
				}
				else
				{
					log.fatal("You need to provide a bot token to run Maunz, please add one obtained from https://discordapp.com/developers/applications/me to the discordToken option in config.json");
					System.exit(1);
				}
			}

			client = new ClientBuilder().withToken(Util.token).login();
			client.getDispatcher().registerListener(new MainListener());

			bot = new CsgoUpdateBot();
			bot.connect("irc.freenode.net");

			if (Util.devMode)
			{
				bot.joinChannel("#maunztesting");
			}
			else
			{
				bot.joinChannel("#steamdb-announce");
			}
		}
		catch (Exception e)
		{
			log.error("", e);
		}
	}
}
