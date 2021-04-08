package com.vauff.maunzdiscord.objects;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.InteractionCreateEvent;

/**
 * Holds information about a message that needs a reaction/reply to continue further execution
 */
public class Await
{
	private Snowflake id;
	private AbstractCommand command;
	private InteractionCreateEvent event;

	/**
	 * @param id      An ID of a user who triggered the message or a message to be removed later on
	 * @param command The command with which to continue execution upon adding a reaction
	 * @param event   The InteractionCreateEvent that triggered the execution of {@link Await#command}
	 */
	public Await(Snowflake id, AbstractCommand command, InteractionCreateEvent event)
	{
		this.id = id;
		this.command = command;
		this.event = event;
	}

	/**
	 * @param id      An ID of a user who triggered the message or a message to be removed later on
	 * @param command The command with which to continue execution upon adding a reaction
	 */
	public Await(Snowflake id, AbstractCommand command)
	{
		this(id, command, null);
	}

	/**
	 * @return The ID of the user who triggered the message or of the message to be removed later on
	 */
	public Snowflake getID()
	{
		return id;
	}

	/**
	 * @return The command with which to continue execution upon adding a reaction
	 */
	public AbstractCommand getCommand()
	{
		return command;
	}

	/**
	 * @return The InteractionCreateEvent that triggered the execution of {@link Await#command}, only present if instance of {@link AbstractSlashCommand}
	 */
	public InteractionCreateEvent getInteractionEvent()
	{
		return event;
	}
}
