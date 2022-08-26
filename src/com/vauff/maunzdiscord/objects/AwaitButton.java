package com.vauff.maunzdiscord.objects;

import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;

/**
 * Holds information for buttons awaiting input
 */
public class AwaitButton
{
	private Snowflake id;
	private AbstractSlashCommand command;
	private DeferrableInteractionEvent event;

	/**
	 * @param id		The user ID who owns the buttons
	 * @param command   The command using the buttons
	 * @param event     The InteractionEvent that triggered the execution of {@link AwaitButton#command}
	 */
	public AwaitButton(Snowflake id, AbstractSlashCommand command, DeferrableInteractionEvent event)
	{
		this.id = id;
		this.command = command;
		this.event = event;
	}

	/**
	 * @return The user ID who owns the buttons
	 */
	public Snowflake getID()
	{
		return id;
	}

	/**
	 * @return The command using the buttons
	 */
	public AbstractSlashCommand getCommand()
	{
		return command;
	}

	/**
	 * @return The InteractionEvent that triggered the execution of {@link AwaitButton#command}
	 */
	public DeferrableInteractionEvent getEvent()
	{
		return event;
	}
}
