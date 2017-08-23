package com.vauff.maunzdiscord.core;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 * Holds information about message that needs a reaction to continue further execution
 */
public class Await
{
	private String userID;
	private AbstractCommand<? extends MessageReceivedEvent> command;
	
	/**
	 * @param uID The user who triggered the message
	 * @param cmd The command with which to continue execution upon adding a reaction
	 */
	public Await(String uID, AbstractCommand<? extends MessageReceivedEvent> cmd)
	{
		userID = uID;
		command = cmd;
	}
	
	/**
	 * @return The user who triggered the message
	 */
	public String getUserID()
	{
		return userID;
	}
	
	/**
	 * @return The command with which to continue execution upon adding a reaction
	 */
	public AbstractCommand<? extends MessageReceivedEvent> getCommand()
	{
		return command;
	}
}
