package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.CommandHelp;
import com.vauff.maunzdiscord.commands.templates.SubCommandHelp;
import com.vauff.maunzdiscord.core.MainListener;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Snowflake;
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
				CommandHelp commandHelp = command.getHelp();

				if (commandHelp.permissionLevel == 1 && !Util.hasPermission(author, event.getGuild().block()))
					continue;

				if (commandHelp.permissionLevel == 2 && !Util.hasPermission(author))
					continue;

				for (int i = 0; i < commandHelp.subCommandHelps.length; i++)
				{
					SubCommandHelp subCommandHelp = commandHelp.subCommandHelps[i];

					helpEntries.add("**\\" + commandHelp.aliases[0] + " " + subCommandHelp.arguments + "** - " + subCommandHelp.description);
				}
			}

			Message m = Util.buildPage(helpEntries, "Command List", 10, page, false, false, channel, author);

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
				CommandHelp commandHelp = command.getHelp();

				for (int i = 0; i < commandHelp.aliases.length; i++)
				{
					if (commandHelp.aliases[i].equalsIgnoreCase(arg))
					{
						if (commandHelp.permissionLevel == 1 && !Util.hasPermission(author, event.getGuild().block()))
							continue;

						if (commandHelp.permissionLevel == 2 && !Util.hasPermission(author))
							continue;

						matchFound = true;

						for (int j = 0; j < commandHelp.subCommandHelps.length; j++)
						{
							SubCommandHelp subCommandHelp = commandHelp.subCommandHelps[j];

							list += "**\\" + commandHelp.aliases[0] + " " + subCommandHelp.arguments + "** - " + subCommandHelp.description + System.lineSeparator();
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
			CommandHelp helpCommand = command.getHelp();

			if (helpCommand.permissionLevel == 1 && !Util.hasPermission(event.getUser().block(), event.getGuild().block()))
				continue;

			if (helpCommand.permissionLevel == 2 && !Util.hasPermission(event.getUser().block()))
				continue;

			for (int i = 0; i < helpCommand.subCommandHelps.length; i++)
			{
				SubCommandHelp subCommandHelp = helpCommand.subCommandHelps[i];

				helpEntries.add("**\\" + helpCommand.aliases[0] + " " + subCommandHelp.arguments + "** - " + subCommandHelp.description);
			}
		}

		if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("▶"))
			pageChange = 1;
		else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("◀"))
			pageChange = -1;

		if (pageChange == 0)
			return;

		Message m = Util.buildPage(helpEntries, "Command List", 10, listPages.get(event.getUser().block().getId()) + pageChange, false, false, event.getChannel().block(), event.getUser().block());

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
	public CommandHelp getHelp()
	{
		SubCommandHelp[] subCommandHelps = new SubCommandHelp[2];

		subCommandHelps[0] = new SubCommandHelp("[page]", "Lists all the available bot commands and the syntax for using each.");
		subCommandHelps[1] = new SubCommandHelp("<command>", "Gives you help on how to use a specific command.");

		return new CommandHelp(getAliases(), subCommandHelps, 0);
	}
}