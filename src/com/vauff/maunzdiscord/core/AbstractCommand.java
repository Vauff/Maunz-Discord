package com.vauff.maunzdiscord.core;

import java.util.HashMap;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;

public abstract class AbstractCommand<M extends MessageReceivedEvent>
{
	public static final HashMap<String,AbstractCommand> AWAITED = new HashMap<String,AbstractCommand>();
	
	public abstract void exe(M event) throws Exception;
	
	public abstract String[] getAliases();
	
	public void confirm(ReactionAddEvent event) throws Exception {}
	
	public void deny(ReactionAddEvent event) throws Exception {}
	
	public final void waitForConfirmation(String messageID)
	{
		AWAITED.put(messageID, this);
	}
}
