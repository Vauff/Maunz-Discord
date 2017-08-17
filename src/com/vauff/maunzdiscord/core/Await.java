package com.vauff.maunzdiscord.core;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Await
{
	private String userID;
	private AbstractCommand<? extends MessageReceivedEvent> command;
	
	public Await(String uID, AbstractCommand<? extends MessageReceivedEvent> cmd)
	{
		userID = uID;
		command = cmd;
	}
	
	public String getUserID()
	{
		return userID;
	}
	
	public AbstractCommand<? extends MessageReceivedEvent> getCommand()
	{
		return command;
	}
}
