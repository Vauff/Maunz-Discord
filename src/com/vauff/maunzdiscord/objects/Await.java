package com.vauff.maunzdiscord.objects;

import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import discord4j.common.util.Snowflake;

/**
 * Holds information about a message that needs a reaction/reply to continue further execution
 */
public class Await
{
	private Snowflake id;
	private AbstractLegacyCommand command;

	/**
	 * @param id      An ID of a user who triggered the message or a message to be removed later on
	 * @param command The command with which to continue execution upon adding a reaction
	 */
	public Await(Snowflake id, AbstractLegacyCommand command)
	{
		this.id = id;
		this.command = command;
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
	public AbstractLegacyCommand getCommand()
	{
		return command;
	}
}
