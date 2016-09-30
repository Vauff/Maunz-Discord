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
	public static String version = "1.0";
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
			log.info("Starting Maunz-Discord v" + version);
			client = new ClientBuilder().withToken(Passwords.discordToken).login();
			client.getDispatcher().registerListener(new ReadyEventListener());
		}
		catch (Exception e)
		{
			log.error(e);
		}
	}
}
