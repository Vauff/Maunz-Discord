package com.vauff.maunzdiscord.objects;

import discord4j.common.util.Snowflake;

/**
 * Holds information about a message awaiting a reaction/button activation to continue further execution
 */
public class Await<M>
{
	private Snowflake id;
	private M command;

	/**
	 * @param id      The ID of a user who triggered the command
	 * @param command The command with which to continue execution upon activation
	 */
	public Await(Snowflake id, M command)
	{
		this.id = id;
		this.command = command;
	}

	/**
	 * @return The ID of the user who triggered the command
	 */
	public Snowflake getID()
	{
		return id;
	}

	/**
	 * @return The command with which to continue execution upon activation
	 */
	public M getCommand()
	{
		return command;
	}
}
