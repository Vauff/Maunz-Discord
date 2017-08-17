package com.vauff.maunzdiscord.core;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

public class Main
{
	public static IDiscordClient client;
	public static String version = "2.0";
	public static Logger log;

	public static void main(String[] args) throws DiscordException
	{
		try
		{
			File logFile = new File("maunzdiscord.log");
			File oldLogFile = new File("maunzdiscord-old.log");

			Thread.sleep(3000);
			oldLogFile.delete();
			logFile.renameTo(oldLogFile);
			log = LogManager.getLogger(Main.class);

			if (args.length >= 1 && args[0].equals("-dev"))
			{
				log.info("Starting Maunz-Discord v" + version + " in dev mode");
				Util.token = Passwords.discordDevToken;
				Util.devMode = true;
			}
			else
			{
				log.info("Starting Maunz-Discord v" + version);
				Util.token = Passwords.discordToken;
				Util.devMode = false;
			}

			client = new ClientBuilder().withToken(Util.token).login();
			client.getDispatcher().registerListener(new MainListener());
		}
		catch (Exception e)
		{
			log.error("", e);
		}
	}
}
