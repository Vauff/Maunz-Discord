package com.vauff.maunzdiscord.commands.legacy;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.objects.CommandHelp;
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
import java.util.Objects;

public class Help extends AbstractLegacyCommand<MessageCreateEvent>
{
	private static HashMap<Snowflake, Integer> listPages = new HashMap<>();

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

			for (AbstractCommand command : Main.commands)
			{
				if (Objects.isNull(command.getHelp()))
					continue;

				if (command.getPermissionLevel() == BotPermission.GUILD_ADMIN && !Util.hasPermission(author, event.getGuild().block()))
					continue;

				if (command.getPermissionLevel() == BotPermission.BOT_ADMIN && !Util.hasPermission(author))
					continue;

				for (CommandHelp commandHelp : command.getHelp())
				{
					helpEntries.add("**" + getPrefix(command) + command.getAliases()[0] + (commandHelp.getArguments().equals("") ? "" : " " + commandHelp.getArguments()) + "** - " + commandHelp.getDescription());
				}
			}

			Message m = Util.buildPage(helpEntries, "Command List", 10, page, 0, false, false, false, channel, author);

			waitForReaction(m.getId(), author.getId());
			listPages.put(author.getId(), page);
		}
		else
		{
			String arg = args[1].toLowerCase();
			String list = "";
			boolean matchFound = false;

			if (!arg.startsWith(Main.prefix))
				arg = Main.prefix + arg;

			rootIteration:
			for (AbstractCommand command : Main.commands)
			{
				if (Objects.isNull(command.getHelp()))
					continue;

				if (command.getPermissionLevel() == BotPermission.GUILD_ADMIN && !Util.hasPermission(author, event.getGuild().block()))
					continue;

				if (command.getPermissionLevel() == BotPermission.BOT_ADMIN && !Util.hasPermission(author))
					continue;

				for (String alias : command.getAliases())
				{
					if (arg.equalsIgnoreCase(Main.prefix + alias))
					{
						matchFound = true;

						for (CommandHelp commandHelp : command.getHelp())
						{
							list += "**" + getPrefix(command) + command.getAliases()[0] + (commandHelp.getArguments().equals("") ? "" : " " + commandHelp.getArguments()) + "** - " + commandHelp.getDescription() + System.lineSeparator();
						}

						list = StringUtils.removeEnd(list, System.lineSeparator());

						break rootIteration;
					}
				}
			}

			if (matchFound)
			{
				Util.msg(channel, list);
			}
			else
			{
				Util.msg(channel, "The command **" + arg + "** either doesn't exist, or you don't have access to it.");
			}
		}
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event, Message message) throws Exception
	{
		int pageChange = 0;
		ArrayList<String> helpEntries = new ArrayList<>();

		for (AbstractCommand command : Main.commands)
		{
			if (Objects.isNull(command.getHelp()))
				continue;

			if (command.getPermissionLevel() == BotPermission.GUILD_ADMIN && !Util.hasPermission(event.getUser().block(), event.getGuild().block()))
				continue;

			if (command.getPermissionLevel() == BotPermission.BOT_ADMIN && !Util.hasPermission(event.getUser().block()))
				continue;

			for (CommandHelp commandHelp : command.getHelp())
			{
				helpEntries.add("**" + getPrefix(command) + command.getAliases()[0] + (commandHelp.getArguments().equals("") ? "" : " " + commandHelp.getArguments()) + "** - " + commandHelp.getDescription());
			}
		}

		if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("▶"))
			pageChange = 1;
		else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("◀"))
			pageChange = -1;

		if (pageChange == 0)
			return;

		Message m = Util.buildPage(helpEntries, "Command List", 10, listPages.get(event.getUser().block().getId()) + pageChange, 0, false, false, false, event.getChannel().block(), event.getUser().block());

		waitForReaction(m.getId(), event.getUser().block().getId());
		listPages.put(event.getUser().block().getId(), listPages.get(event.getUser().block().getId()) + pageChange);
	}

	private String getPrefix(AbstractCommand cmd)
	{
		if (cmd instanceof AbstractSlashCommand)
			return "/";
		if (Main.prefix.equals("*") || Main.prefix.equals("_") || Main.prefix.equals("`") || Main.prefix.equals(">"))
			return "\\" + Main.prefix;
		else
			return Main.prefix;
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "help" };
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

		commandHelps[0] = new CommandHelp("[page]", "Lists all the available bot commands and the syntax for using each");
		commandHelps[1] = new CommandHelp("<command>", "Gives you help on how to use a specific command");

		return commandHelps;
	}
}