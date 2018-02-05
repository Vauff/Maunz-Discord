package com.vauff.maunzdiscord.core;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 * Holds information about a message that needs a reaction/reply to continue further execution
 */
public class Await
{
	private String id;
	private AbstractCommand<? extends MessageReceivedEvent> command;

	/**
	 * @param anID An ID of a user who triggered the message or a message to be removed later on
	 * @param cmd  The command with which to continue execution upon adding a reaction
	 */
	public Await(String anID, AbstractCommand<? extends MessageReceivedEvent> cmd)
	{
		id = anID;
		command = cmd;
	}

	/**
	 * @return The ID of the user who triggered the message or of the message to be removed later on
	 */
	public String getID()
	{
		return id;
	}

	/**
	 * @return The command with which to continue execution upon adding a reaction
	 */
	public AbstractCommand<? extends MessageReceivedEvent> getCommand()
	{
		return command;
	}
}
