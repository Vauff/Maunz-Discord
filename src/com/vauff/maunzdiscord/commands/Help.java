package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class Help extends AbstractCommand<MessageCreateEvent>
{
	private static HashMap<String, String> cmdHelp = new HashMap<>();
	private static HashMap<String, String> cmdAliases = new HashMap<>();
	private static HashMap<Snowflake, Integer> listPages = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> listMessages = new HashMap<>();

	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().get().split(" ");

		if (args.length == 1)
		{
			ArrayList<String> helpEntries = new ArrayList<>();
			SortedSet<String> sortedHelpEntries = new TreeSet<>(cmdHelp.keySet());

			for (String key : sortedHelpEntries)
			{
				helpEntries.add(cmdHelp.get(key));
			}

			Message m = Util.buildPage(helpEntries, "Command List", 10, 1, false, false, channel, author);

			listMessages.put(author.getId(), m.getId());
			waitForReaction(m.getId(), author.getId());
			listPages.put(author.getId(), 1);
		}

		else if (args.length == 2 && NumberUtils.isCreatable(args[1]))
		{
			int page = Integer.parseInt(args[1]);
			ArrayList<String> helpEntries = new ArrayList<>();
			SortedSet<String> sortedHelpEntries = new TreeSet<>(cmdHelp.keySet());

			for (String key : sortedHelpEntries)
			{
				helpEntries.add(cmdHelp.get(key));
			}

			Message m = Util.buildPage(helpEntries, "Command List", 10, page, false, false, channel, author);

			listMessages.put(author.getId(), m.getId());
			waitForReaction(m.getId(), author.getId());
			listPages.put(author.getId(), page);
		}
		else
		{
			String arg = args[1].toLowerCase().replace("*", "");

			if (cmdHelp.containsKey(arg))
			{
				String list = cmdHelp.get(arg);
				int i = 2;

				while (true)
				{
					if (cmdHelp.containsKey(arg + i))
					{
						list += System.lineSeparator() + cmdHelp.get(arg + i);
					}
					else
					{
						break;
					}

					i++;
				}

				Util.msg(channel, author, list);
			}
			else if (cmdAliases.containsKey(arg))
			{
				String list = cmdAliases.get(arg);
				int i = 2;

				while (true)
				{
					if (cmdAliases.containsKey(arg + i))
					{
						list += System.lineSeparator() + cmdAliases.get(arg + i);
					}
					else
					{
						break;
					}

					i++;
				}

				Util.msg(channel, author, list);
			}
			else
			{
				Util.msg(channel, author, "I don't recognize the command " + args[1] + "!");
			}
		}
	}

	public static void setupCmdHelp()
	{
		cmdHelp.put("about", "**\\*about** - Gives information about Maunz such as version and uptime.");
		cmdHelp.put("benchmark", "**\\*benchmark <gpu/cpu>** - Provides complete benchmark information on a GPU or CPU powered by PassMark.");
		cmdHelp.put("blacklist", "**\\*blacklist [all/channel] <all/command>** - Allows you to blacklist the usage of different command/channel combinations (or all), only usable by guild administrators and the bot owner.");
		cmdHelp.put("blacklist2", "**\\*blacklist list [page]** - Lists the currently blacklisted commands/channels, only usable by guild administrators and the bot owner.");
		cmdHelp.put("changelog", "**\\*changelog [version]** - Shows you the changelog of the Maunz version you specify.");
		cmdHelp.put("disable", "**\\*disable** - Disables Maunz either in a specific guild or globally, only usable by guild administrators and the bot owner.");
		cmdHelp.put("colour", "**\\*colour [link]** - Returns the average RGB and HTML/Hex colour codes of an attachment or image link you specify.");
		cmdHelp.put("discord", "**\\*discord** - Sends an invite link to add the bot to your own server and an invite link to the Maunz Hub server.");
		cmdHelp.put("enable", "**\\*enable** - Enables Maunz either in a specific guild or globally, only usable by guild administrators and the bot owner.");
		cmdHelp.put("help", "**\\*help [page]** - Lists all the available bot commands and the syntax for using each.");
		cmdHelp.put("help2", "**\\*help <command>** - Gives you help on how to use a specific command.");
		cmdHelp.put("isitdown", "**\\*isitdown <hostname>** - Tells you if the given hostname is down or not.");
		cmdHelp.put("map", "**\\*map** - Tells you which map a server is playing outside of its standard map tracking channel.");
		cmdHelp.put("map2", "**\\*map [mapname]** - Gives you information on a specific map such as last time played.");
		cmdHelp.put("minecraft", "**\\*minecraft <uuid/username>** - Gives you full information about any Minecraft account.");
		cmdHelp.put("notify", "**\\*notify list [page]** - Lists your current map notifications.");
		cmdHelp.put("notify2", "**\\*notify wipe** - Wipes ALL of your map notifications.");
		cmdHelp.put("notify3", "**\\*notify <mapname>** - Adds or removes a given map to/from your map notifications, exact name is recommended for best accuracy but the bot can use it as a search term too.");
		cmdHelp.put("ping", "**\\*ping** - Makes Maunz respond to you with pong. Very useful for testing your connection!");
		cmdHelp.put("players", "**\\*players** - Lists the current players online on a server.");
		cmdHelp.put("quote", "**\\*quote** - Links you directly to the chat quotes site.");
		cmdHelp.put("quote2", "**\\*quote add** - Links you to a page where you can submit chat quotes for approval.");
		cmdHelp.put("quote3", "**\\*quote view <quoteid>** - Views a chat quote based on ID.");
		cmdHelp.put("quote4", "**\\*quote list [page]** - Lists existing chat quotes sorted by ID.");
		cmdHelp.put("reddit", "**\\*reddit <subreddit>** - Links you to a subreddit that you provide.");
		cmdHelp.put("restart", "**\\*restart** - Restarts Maunz, only usable by the bot owner.");
		cmdHelp.put("say", "**\\*say [channel] <message>** - Makes Maunz say whatever you want her to, only usable by guild administrators and the bot owner.");
		cmdHelp.put("services", "**\\*services** - Opens an interface for enabling specific services on a guild, only usable by guild administrators and the bot owner.");
		cmdHelp.put("source", "**\\*source** - Links you to the GitHub page of Maunz, you can submit issues/pull requests here.");
		cmdHelp.put("steam", "**\\*steam <steamid>** - Gives full information on a Steam account for the given input.");
		cmdHelp.put("stop", "**\\*stop** - Stops Maunz, only usable by the bot owner.");

		cmdAliases.put("color", "**\\*colour [link]** - Returns the average RGB and HTML/Hex colour codes of an attachment or image link you specify.");
		cmdAliases.put("quotes", "**\\*quote** - Links you directly to the chat quotes site.");
		cmdAliases.put("quotes2", "**\\*quote add** - Links you to a page where you can submit chat quotes for approval.");
		cmdAliases.put("quotes3", "**\\*quote view <quoteid>** - Views a chat quote based on ID.");
		cmdAliases.put("quotes4", "**\\*quote list [page]** - Lists existing chat quotes sorted by ID.");
		cmdAliases.put("invite", "**\\*discord** - Sends an invite link to add the bot to your own server and an invite link to the Maunz Hub server.");
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*help" };
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event, Message message)
	{
		if (listMessages.containsKey(event.getUser().block().getId()) && message.getId().equals(listMessages.get(event.getUser().block().getId())))
		{
			if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("▶"))
			{
				ArrayList<String> helpEntries = new ArrayList<>();
				SortedSet<String> sortedHelpEntries = new TreeSet<>(cmdHelp.keySet());

				for (String key : sortedHelpEntries)
				{
					helpEntries.add(cmdHelp.get(key));
				}

				Message m = Util.buildPage(helpEntries, "Command List", 10, listPages.get(event.getUser().block().getId()) + 1, false, false, event.getChannel().block(), event.getUser().block());

				listMessages.put(event.getUser().block().getId(), m.getId());
				waitForReaction(m.getId(), event.getUser().block().getId());
				listPages.put(event.getUser().block().getId(), listPages.get(event.getUser().block().getId()) + 1);
			}

			else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("◀"))
			{
				ArrayList<String> helpEntries = new ArrayList<>();
				SortedSet<String> sortedHelpEntries = new TreeSet<>(cmdHelp.keySet());

				for (String key : sortedHelpEntries)
				{
					helpEntries.add(cmdHelp.get(key));
				}

				Message m = Util.buildPage(helpEntries, "Command List", 10, listPages.get(event.getUser().block().getId()) - 1, false, false, event.getChannel().block(), event.getUser().block());

				listMessages.put(event.getUser().block().getId(), m.getId());
				waitForReaction(m.getId(), event.getUser().block().getId());
				listPages.put(event.getUser().block().getId(), listPages.get(event.getUser().block().getId()) - 1);
			}
		}
	}
}