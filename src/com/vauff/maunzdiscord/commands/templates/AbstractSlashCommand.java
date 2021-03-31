package com.vauff.maunzdiscord.commands.templates;

import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractSlashCommand<M extends ApplicationCommandInteraction> extends AbstractCommand
{
	/**
	 * Executes this command
	 *
	 * @param interaction The interaction that executing this command creates
	 * @throws Exception If an exception gets thrown by any implementing methods
	 */
	public abstract String exe(M interaction, MessageChannel channel, User author) throws Exception;

	/**
	 * Returns the ApplicationCommandRequest object for this command
	 */
	public abstract ApplicationCommandRequest getCommand();

	/**
	 * Defines the name used to trigger the command.
	 *
	 * @return A string containing the name of the command
	 */
	public abstract String getName();

	@Override
	public final String[] getAliases() { return new String[] { getName() }; }

	@Override
	public final CommandHelp[] getHelp()
	{
		if (getCommand().options().isAbsent())
			return new CommandHelp[] { new CommandHelp("", getCommand().description()) };

		List<CommandHelp> commandHelps = new ArrayList<>();
		CommandHelp commandHelp = new CommandHelp("", getCommand().description());
		boolean noSubCmds = true;

		for (ApplicationCommandOptionData option : getCommand().options().get())
		{
			if (option.type() == ApplicationCommandOptionType.SUB_COMMAND.getValue())
			{
				noSubCmds = false;

				commandHelps.add(parseHelpSubCommand(option, null));
			}
			else if (option.type() == ApplicationCommandOptionType.SUB_COMMAND_GROUP.getValue())
			{
				noSubCmds = false;

				if (option.options().isAbsent())
					continue;

				for (ApplicationCommandOptionData subCmd : option.options().get())
					commandHelps.add(parseHelpSubCommand(subCmd, option.name()));
			}
			else
			{
				if (option.required().isAbsent() || !option.required().get())
					commandHelp.arguments += "[" + option.name() + "] ";
				else
					commandHelp.arguments += "<" + option.name() + "> ";
			}
		}

		if (noSubCmds)
		{
			commandHelp.arguments = commandHelp.arguments.trim();
			commandHelps.add(commandHelp);
		}

		return commandHelps.toArray(new CommandHelp[commandHelps.size()]);
	}

	private CommandHelp parseHelpSubCommand(ApplicationCommandOptionData subCommand, String rootName)
	{
		CommandHelp commandHelp = new CommandHelp("", subCommand.description());

		if (!Objects.isNull(rootName))
			commandHelp.arguments = rootName + " ";

		commandHelp.arguments += subCommand.name() + " ";

		if (!subCommand.options().isAbsent())
		{
			for (ApplicationCommandOptionData option : subCommand.options().get())
			{
				if (option.required().isAbsent() || !option.required().get())
					commandHelp.arguments += "[" + option.name() + "] ";
				else
					commandHelp.arguments += "<" + option.name() + "> ";
			}
		}

		commandHelp.arguments = commandHelp.arguments.trim();

		return commandHelp;
	}
}
