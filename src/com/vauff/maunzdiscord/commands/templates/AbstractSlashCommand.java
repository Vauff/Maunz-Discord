package com.vauff.maunzdiscord.commands.templates;

import com.vauff.maunzdiscord.objects.Await;
import com.vauff.maunzdiscord.objects.CommandHelp;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractSlashCommand<M extends ChatInputInteractionEvent> extends AbstractCommand
{
	/**
	 * Executes this command
	 *
	 * @param interaction The interaction that executing this command creates
	 * @throws Exception If an exception gets thrown by any implementing methods
	 */
	public abstract void exe(M interaction, Guild guild, MessageChannel channel, User author) throws Exception;

	/**
	 * Gets called when a reaction is added to a message defined prior in {@link AbstractSlashCommand#waitForReaction(Snowflake, Snowflake, ChatInputInteractionEvent)}
	 *
	 * @param reactionEvent    The event holding information about the added reaction
	 * @param interactionEvent The interaction event that triggered this
	 * @param message          The Message that was reacted to
	 */
	public void onReactionAdd(ReactionAddEvent reactionEvent, ChatInputInteractionEvent interactionEvent, Message message) throws Exception
	{
	}

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

	/**
	 * Sets up this command to await a reaction by the user who triggered this command
	 *
	 * @param messageID The message which should get reacted on
	 * @param userID    The user who triggered this command
	 */
	public final void waitForReaction(Snowflake messageID, Snowflake userID, ChatInputInteractionEvent event)
	{
		AWAITED.put(messageID, new Await(userID, this, event));
	}

	@Override
	public final String[] getAliases()
	{
		return new String[] { getName() };
	}

	@Override
	public final CommandHelp[] getHelp()
	{
		if (getCommand().options().isAbsent())
			return new CommandHelp[] { new CommandHelp("", getCommand().description().get()) };

		List<CommandHelp> commandHelps = new ArrayList<>();
		CommandHelp commandHelp = new CommandHelp("", getCommand().description().get());
		boolean noSubCmds = true;

		for (ApplicationCommandOptionData option : getCommand().options().get())
		{
			if (option.type() == ApplicationCommandOption.Type.SUB_COMMAND.getValue())
			{
				noSubCmds = false;

				commandHelps.add(parseHelpSubCommand(option, null));
			}
			else if (option.type() == ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
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
					commandHelp.setArguments(commandHelp.getArguments() + "[" + option.name() + "] ");
				else
					commandHelp.setArguments(commandHelp.getArguments() + "<" + option.name() + "> ");
			}
		}

		if (noSubCmds)
		{
			commandHelp.setArguments(commandHelp.getArguments().trim());
			commandHelps.add(commandHelp);
		}

		return commandHelps.toArray(new CommandHelp[commandHelps.size()]);
	}

	private CommandHelp parseHelpSubCommand(ApplicationCommandOptionData subCommand, String rootName)
	{
		CommandHelp commandHelp = new CommandHelp("", subCommand.description());

		if (!Objects.isNull(rootName))
			commandHelp.setArguments(rootName + " ");

		commandHelp.setArguments(commandHelp.getArguments() + subCommand.name() + " ");

		if (!subCommand.options().isAbsent())
		{
			for (ApplicationCommandOptionData option : subCommand.options().get())
			{
				if (option.required().isAbsent() || !option.required().get())
					commandHelp.setArguments(commandHelp.getArguments() + "[" + option.name() + "] ");
				else
					commandHelp.setArguments(commandHelp.getArguments() + "<" + option.name() + "> ");
			}
		}

		commandHelp.setArguments(commandHelp.getArguments().trim());

		return commandHelp;
	}
}
