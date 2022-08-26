package com.vauff.maunzdiscord.commands.templates;

import com.vauff.maunzdiscord.objects.Await;
import com.vauff.maunzdiscord.objects.CommandHelp;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.component.ActionComponent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class AbstractSlashCommand<M extends ChatInputInteractionEvent> extends AbstractCommand
{
	/**
	 * Button names used for buildPage
	 */
	public final String PREV_BTN = "prevpage";
	public final String NEXT_BTN = "nextpage";
	public final String CANCEL_BTN = "cancel";

	/**
	 * Holds messages as keys which await a button press by a specific user.
	 * The values hold an instance of {@link Await}
	 */
	public static final HashMap<Snowflake, Await<AbstractSlashCommand>> AWAITED = new HashMap<>();

	/**
	 * Executes this command
	 *
	 * @param interaction The interaction that executing this command creates
	 * @throws Exception If an exception gets thrown by any implementing methods
	 */
	public abstract void exe(M interaction, Guild guild, MessageChannel channel, User user) throws Exception;

	/**
	 * Executes a button attached to this command
	 *
	 * @param event		The ButtonInteractionEvent triggered from pressing a button
	 * @param buttonId	The button ID that was pressed
	 * @param guild		Guild the button was pressed in
	 * @param channel	Channel the button was pressed in
	 * @param user		User who pressed the button
	 */
	public void buttonPressed(ButtonInteractionEvent event, String buttonId, Guild guild, MessageChannel channel, User user)
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

	@Override
	public final String[] getAliases()
	{
		return new String[] { getName() };
	}

	/**
	 * Sets up this command to await a button press by the user who triggered this command
	 *
	 * @param messageID The message which has buttons attached
	 * @param userID    The user who triggered this command
	 */
	public final void waitForButtonPress(Snowflake messageID, Snowflake userID)
	{
		AWAITED.put(messageID, new Await(userID, this));
	}

	/**
	 * Builds a modular page message in response to an interaction for the given parameters
	 *
	 * @param entries           An ArrayList<String> that contains all the entries that should be in the page builder
	 * @param title             The title to give all the pages
	 * @param pageSize          How many entries should be in a specific page
	 * @param pageNumber        Which page the method should build and send to the provided channel
	 * @param numberStyle       Which style to use for numbered entries. 0 = none 1 = standard 2 = code block surrounded & unique per page
	 * @param codeBlock         Whether to surround all the entries in a code block or not
	 * @param numberedReactions Whether to add numbered buttons for each entry
	 * @param cancellable       Whether to add a cancel button to close the page
	 * @param event             The DeferrableInteractionEvent that is being responded to
	 */
	public final void buildPage(List<String> entries, String title, int pageSize, int pageNumber, int numberStyle, boolean codeBlock, boolean numberedReactions, boolean cancellable, DeferrableInteractionEvent event)
	{
		if (pageNumber > (int) Math.ceil((float) entries.size() / (float) pageSize))
		{
			event.editReply("That page doesn't exist!").block();
			return;
		}
		else
		{
			StringBuilder list = new StringBuilder();

			if (codeBlock)
			{
				list.append("```" + System.lineSeparator());
			}

			int usedEntries = 0;

			for (int i = (int) (entries.size() - ((((float) entries.size() / (float) pageSize) - (pageNumber - 1)) * pageSize)); entries.size() - ((((float) entries.size() / (float) pageSize) - pageNumber) * pageSize) > i; i++)
			{
				if (i > entries.size() - 1)
				{
					break;
				}
				else
				{
					usedEntries++;

					if (numberStyle == 0)
					{
						list.append(entries.get(i) + System.lineSeparator());
					}
					else if (numberStyle == 1)
					{
						list.append((i + 1) + " - " + entries.get(i) + System.lineSeparator());
					}
					else if (numberStyle == 2)
					{
						list.append("**`[" + ((i + 1) - (pageSize * (pageNumber - 1))) + "]`** | " + entries.get(i) + System.lineSeparator());
					}
				}
			}

			if (codeBlock)
			{
				list.append("```");
			}

			String msg;

			if (pageNumber == 1 && (int) Math.ceil((float) entries.size() / (float) pageSize) == 1)
				msg = "--- **" + title + "** ---" + System.lineSeparator() + list.toString();
			else
				msg = "--- **" + title + "** --- **Page " + pageNumber + "/" + (int) Math.ceil((float) entries.size() / (float) pageSize) + "** ---" + System.lineSeparator() + list.toString();

			List<ActionComponent> components = new ArrayList<>();

			if (pageNumber != 1)
			{
				components.add(Button.primary(PREV_BTN, ReactionEmoji.unicode("◀"), "Previous Page"));
			}
			if (pageNumber != (int) Math.ceil((float) entries.size() / (float) pageSize))
			{
				components.add(Button.primary(NEXT_BTN, ReactionEmoji.unicode("▶"), "Next Page"));
			}
			if (cancellable)
			{
				components.add(Button.danger(CANCEL_BTN, ReactionEmoji.unicode("❌"), "Cancel"));
			}

			if (components.size() > 0)
				event.editReply(msg).withComponents(ActionRow.of(components)).block();
			else
				event.editReply(msg).block();
		}
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
