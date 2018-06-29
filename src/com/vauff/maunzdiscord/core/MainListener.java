package com.vauff.maunzdiscord.core;

import com.vauff.maunzdiscord.commands.About;
import com.vauff.maunzdiscord.commands.Benchmark;
import com.vauff.maunzdiscord.commands.Blacklist;
import com.vauff.maunzdiscord.commands.Changelog;
import com.vauff.maunzdiscord.commands.Colour;
import com.vauff.maunzdiscord.commands.Disable;
import com.vauff.maunzdiscord.commands.Discord;
import com.vauff.maunzdiscord.commands.Enable;
import com.vauff.maunzdiscord.commands.Help;
import com.vauff.maunzdiscord.commands.IsItDown;
import com.vauff.maunzdiscord.commands.Map;
import com.vauff.maunzdiscord.commands.Minecraft;
import com.vauff.maunzdiscord.commands.Notify;
import com.vauff.maunzdiscord.commands.Ping;
import com.vauff.maunzdiscord.commands.Players;
import com.vauff.maunzdiscord.commands.Quote;
import com.vauff.maunzdiscord.commands.Reddit;
import com.vauff.maunzdiscord.commands.Restart;
import com.vauff.maunzdiscord.commands.Say;
import com.vauff.maunzdiscord.commands.Source;
import com.vauff.maunzdiscord.commands.Steam;
import com.vauff.maunzdiscord.commands.Stop;
import com.vauff.maunzdiscord.commands.servicesmenu.Services;
import com.vauff.maunzdiscord.threads.MessageReceivedThread;
import com.vauff.maunzdiscord.threads.ReactionAddThread;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;

import java.io.File;
import java.util.LinkedList;

public class MainListener
{
	/**
	 * Holds all commands
	 */
	public static LinkedList<AbstractCommand<MessageReceivedEvent>> commands = new LinkedList<AbstractCommand<MessageReceivedEvent>>();

	/**
	 * Sets up all commands
	 */
	public MainListener()
	{
		try
		{
			JSONObject json = new JSONObject(Util.getFileContents("config.json"));

			commands.add(new About());
			commands.add(new Benchmark());
			commands.add(new Blacklist());
			commands.add(new Changelog());
			commands.add(new Colour());
			commands.add(new Disable());
			commands.add(new Discord());
			commands.add(new Enable());
			commands.add(new Help());
			commands.add(new IsItDown());
			commands.add(new Map());
			commands.add(new Minecraft());
			commands.add(new Notify());
			commands.add(new Ping());
			commands.add(new Players());
			commands.add(new Reddit());
			commands.add(new Restart());
			commands.add(new Say());
			commands.add(new Services());
			commands.add(new Source());
			commands.add(new Steam());
			commands.add(new Stop());

			if (json.getJSONObject("database").getString("hostname").equals("") || json.getJSONObject("database").getString("username").equals("") || json.getJSONObject("database").getString("password").equals(""))
			{
				Logger.log.warn("The quote command is disabled due to 1 or more values in the database section of config.json not being supplied");
			}
			else
			{
				commands.add(new Quote());
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event)
	{
		MessageReceivedThread thread = new MessageReceivedThread(event, "messagereceived-" + event.getMessage().getStringID());
		thread.start();
	}

	@EventSubscriber
	public void onReactionAdd(ReactionAddEvent event)
	{
		ReactionAddThread thread = new ReactionAddThread(event, "reactionadd-" + event.getMessage().getStringID());
		thread.start();
	}

	@EventSubscriber
	public void onGuildCreate(GuildCreateEvent event)
	{
		try
		{
			File file = new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().getStringID() + ".json");

			if (!file.exists())
			{
				JSONObject json = new JSONObject();

				file.createNewFile();
				json.put("enabled", true);
				json.put("lastGuildName", event.getGuild().getName());
				json.put("blacklist", new JSONArray());
				FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
