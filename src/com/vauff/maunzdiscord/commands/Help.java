package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.CommandHelp;
import com.vauff.maunzdiscord.core.MainListener;
import com.vauff.maunzdiscord.core.Util;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class Help extends AbstractCommand<MessageCreateEvent>
{
	private static HashMap<Snowflake, Integer> listPages = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> listMessages = new HashMap<>();

	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");
		int page = 1;

		if (args.length == 2 && NumberUtils.isCreatable(args[1]))
			page = Integer.parseInt(args[1]);

		if (args.length == 1 || (args.length == 2 && NumberUtils.isCreatable(args[1])))
		{
			ArrayList<String> helpEntries = new ArrayList<>();

			for (AbstractCommand command : MainListener.commands)
			{
				if (command.getPermissionLevel() == BotPermission.GUILD_ADMIN && !Util.hasPermission(author, event.getGuild().block()))
					continue;

				if (command.getPermissionLevel() == BotPermission.BOT_ADMIN && !Util.hasPermission(author))
					continue;

				for (CommandHelp commandHelp : command.getHelp())
				{
					helpEntries.add("**\\" + command.getAliases()[0] + " " + commandHelp.arguments + "** - " + commandHelp.description);
				}
			}

			Message m = Util.buildPage(helpEntries, "Command List", 10, page, 0, false, false, false, channel, author);

			listMessages.put(author.getId(), m.getId());
			waitForReaction(m.getId(), author.getId());
			listPages.put(author.getId(), page);
		}
		else
		{
			String arg = args[1].toLowerCase();
			String list = "";
			boolean matchFound = false;

			if (!arg.startsWith("*"))
				arg = "*" + arg;

			rootIteration:
			for (AbstractCommand command : MainListener.commands)
			{
				for (String alias : command.getAliases())
				{
					if (alias.equalsIgnoreCase(arg))
					{
						if (command.getPermissionLevel() == BotPermission.GUILD_ADMIN && !Util.hasPermission(author, event.getGuild().block()))
							continue;

						if (command.getPermissionLevel() == BotPermission.BOT_ADMIN && !Util.hasPermission(author))
							continue;

						matchFound = true;

						for (CommandHelp commandHelp : command.getHelp())
						{
							list += "**\\" + command.getAliases()[0] + " " + commandHelp.arguments + "** - " + commandHelp.description + System.lineSeparator();
						}

						list = StringUtils.removeEnd(list, System.lineSeparator());

						break rootIteration;
					}
				}
			}

			if (matchFound)
			{
				Util.msg(channel, author, list);
			}
			else
			{
				Util.msg(channel, author, "The command **" + args[1] + "** either doesn't exist, or you don't have access to it.");
			}
		}
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event, Message message) throws Exception
	{
		if (!listMessages.containsKey(event.getUser().block().getId()) || !message.getId().equals(listMessages.get(event.getUser().block().getId())))
			return;

		int pageChange = 0;
		ArrayList<String> helpEntries = new ArrayList<>();

		for (AbstractCommand command : MainListener.commands)
		{
			if (command.getPermissionLevel() == BotPermission.GUILD_ADMIN && !Util.hasPermission(event.getUser().block(), event.getGuild().block()))
				continue;

			if (command.getPermissionLevel() == BotPermission.BOT_ADMIN && !Util.hasPermission(event.getUser().block()))
				continue;

			for (CommandHelp commandHelp : command.getHelp())
			{
				helpEntries.add("**\\" + command.getAliases()[0] + " " + commandHelp.arguments + "** - " + commandHelp.description);
			}
		}

		if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("▶"))
			pageChange = 1;
		else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("◀"))
			pageChange = -1;

		if (pageChange == 0)
			return;

		Message m = Util.buildPage(helpEntries, "Command List", 10, listPages.get(event.getUser().block().getId()) + pageChange, 0, false, false, false, event.getChannel().block(), event.getUser().block());

		listMessages.put(event.getUser().block().getId(), m.getId());
		waitForReaction(m.getId(), event.getUser().block().getId());
		listPages.put(event.getUser().block().getId(), listPages.get(event.getUser().block().getId()) + pageChange);
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*help" };
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		CommandHelp[] commandHelps = new CommandHelp[2];

		commandHelps[0] = new CommandHelp("[page]", "Lists all the available bot commands and the syntax for using each.");
		commandHelps[1] = new CommandHelp("<command>", "Gives you help on how to use a specific command.");

		return commandHelps;
	}
}