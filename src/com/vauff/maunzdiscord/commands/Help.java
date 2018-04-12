package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.lang3.math.NumberUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class Help extends AbstractCommand<MessageReceivedEvent>
{
	private static HashMap<String, String> cmdHelp = new HashMap<>();
	private static HashMap<String, Integer> listPages = new HashMap<>();
	private static HashMap<String, String> listMessages = new HashMap<>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length == 1)
		{
			ArrayList<String> helpEntries = new ArrayList<String>();
			SortedSet<String> sortedHelpEntries = new TreeSet<String>(cmdHelp.keySet());

			for (String key : sortedHelpEntries)
			{
				helpEntries.add(cmdHelp.get(key));
			}

			IMessage m = Util.buildPage(helpEntries, 10, 1, false, false, event.getChannel(), event.getAuthor());

			listMessages.put(event.getAuthor().getStringID(), m.getStringID());
			waitForReaction(m.getStringID(), event.getAuthor().getStringID());
			listPages.put(event.getAuthor().getStringID(), 1);
		}

		else if (args.length == 2 && NumberUtils.isCreatable(args[1]))
		{
			int page = Integer.parseInt(args[1]);
			ArrayList<String> helpEntries = new ArrayList<String>();
			SortedSet<String> sortedHelpEntries = new TreeSet<String>(cmdHelp.keySet());

			for (String key : sortedHelpEntries)
			{
				helpEntries.add(cmdHelp.get(key));
			}

			IMessage m = Util.buildPage(helpEntries, 10, page, false, false, event.getChannel(), event.getAuthor());

			listMessages.put(event.getAuthor().getStringID(), m.getStringID());
			waitForReaction(m.getStringID(), event.getAuthor().getStringID());
			listPages.put(event.getAuthor().getStringID(), page);
		}
		else
		{
			String arg = args[1].toLowerCase().replace("*", "");

			if (cmdHelp.containsKey(arg))
			{
				Util.msg(event.getChannel(), event.getAuthor(), cmdHelp.get(arg));
			}
			else
			{
				Util.msg(event.getChannel(), event.getAuthor(), "I don't recognize the command " + args[1] + "!");
			}
		}
	}

	public static void setupCmdHelp()
	{
		cmdHelp.put("about", "**\\*about** - Gives information about Maunz such as version and uptime.");
		cmdHelp.put("benchmark", "**\\*benchmark <gpu/cpu>** - Provides complete benchmark information on a GPU or CPU powered by PassMark.");
		cmdHelp.put("blacklist", "**\\*blacklist [all/channel]/<list> <all/command>/[page]** - Allows you to blacklist the usage of either all commands or specific commands in a channel, only usable by guild administrators and the bot owner.");
		cmdHelp.put("changelog", "**\\*changelog [version]** - Shows you the changelog of the Maunz version you specify.");
		cmdHelp.put("disable", "**\\*disable** - Disables Maunz either in a specific guild or globally, only usable by guild administrators and the bot owner.");
		cmdHelp.put("discord", "**\\*discord** - Sends an invite link to add the bot to your own server and an invite link to the Maunz Hub server.");
		cmdHelp.put("enable", "**\\*enable** - Enables Maunz either in a specific guild or globally, only usable by guild administrators and the bot owner.");
		cmdHelp.put("help", "**\\*help [command/page]** - Links you to the README or gives command help if a command is given.");
		cmdHelp.put("isitdown", "**\\*isitdown <hostname>** - Tells you if the given hostname is down or not.");
		cmdHelp.put("map", "**\\*map** - Tells you which map a server is playing outside of its standard map tracking channel.");
		cmdHelp.put("minecraft", "**\\*minecraft <uuid/username>** - Gives you full information about any Minecraft account.");
		cmdHelp.put("notify", "**\\*notify <list/wipe/mapname> [page]** - Lets you list, wipe, add or remove your server map notifications.");
		cmdHelp.put("ping", "**\\*ping** - Makes Maunz respond to you with pong. Very useful for testing your connection!");
		cmdHelp.put("players", "**\\*players** - Lists the current players online on a server.");
		cmdHelp.put("quote", "**\\*quote <view/list/add> <quoteid>/[page]** - Allows you to view chat quotes.");
		cmdHelp.put("reddit", "**\\*reddit <subreddit>** - Links you to a subreddit that you provide.");
		cmdHelp.put("restart", "**\\*restart** - Restarts Maunz, only usable by the bot owner.");
		cmdHelp.put("say", "**\\*say [channel] <message>** - Makes Maunz say whatever you want her to, only usable by guild administrators and the bot owner.");
		cmdHelp.put("services", "**\\*services** - Opens an interface for enabling specific services on a guild, only usable by guild administrators and the bot owner.");
		cmdHelp.put("source", "**\\*source** - Links you to the GitHub page of Maunz, you can submit issues/pull requests here.");
		cmdHelp.put("steam", "**\\*steam <steamid>** - Gives full information on a Steam account for the given input.");
		cmdHelp.put("stop", "**\\*stop** - Stops Maunz, only usable by the bot owner.");
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*help" };
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event)
	{
		if (listMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(listMessages.get(event.getUser().getStringID())))
			{
				if (event.getReaction().getEmoji().toString().equals("▶"))
				{
					ArrayList<String> helpEntries = new ArrayList<String>();
					SortedSet<String> sortedHelpEntries = new TreeSet<String>(cmdHelp.keySet());

					for (String key : sortedHelpEntries)
					{
						helpEntries.add(cmdHelp.get(key));
					}

					IMessage m = Util.buildPage(helpEntries, 10, listPages.get(event.getUser().getStringID()) + 1, false, false, event.getChannel(), event.getUser());

					listMessages.put(event.getUser().getStringID(), m.getStringID());
					waitForReaction(m.getStringID(), event.getUser().getStringID());
					listPages.put(event.getUser().getStringID(), listPages.get(event.getUser().getStringID()) + 1);
				}

				else if (event.getReaction().getEmoji().toString().equals("◀"))
				{
					ArrayList<String> helpEntries = new ArrayList<String>();
					SortedSet<String> sortedHelpEntries = new TreeSet<String>(cmdHelp.keySet());

					for (String key : sortedHelpEntries)
					{
						helpEntries.add(cmdHelp.get(key));
					}

					IMessage m = Util.buildPage(helpEntries, 10, listPages.get(event.getUser().getStringID()) - 1, false, false, event.getChannel(), event.getUser());

					listMessages.put(event.getUser().getStringID(), m.getStringID());
					waitForReaction(m.getStringID(), event.getUser().getStringID());
					listPages.put(event.getUser().getStringID(), listPages.get(event.getUser().getStringID()) - 1);
				}
			}
		}
	}
}