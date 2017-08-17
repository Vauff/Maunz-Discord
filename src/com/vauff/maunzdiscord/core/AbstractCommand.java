package com.vauff.maunzdiscord.core;

import java.util.HashMap;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;

public abstract class AbstractCommand<M extends MessageReceivedEvent>
{
	public static final HashMap<String,Await> AWAITED = new HashMap<String,Await>();
	
	public abstract void exe(M event) throws Exception;
	
	public abstract String[] getAliases();
	
	public final void waitForReaction(String messageID, String userID)
	{
		AWAITED.put(messageID, new Await(userID, this));
	}
	
	/**
	 * If set to true you shouldn't be overriding {@link AbstractCommand#onReactionAdd(ReactionAddEvent)}
	 * as intended behaviour is already handled within the default implementation of that method
	 */
	public boolean confirmable()
	{
		return false;
	}
	
	public void onReactionAdd(ReactionAddEvent event)
	{
		if (confirmable())
		{
			try
			{
				if (event.getReaction().toString().equals("✅"))
				{
					confirm(event);
				}
				else if (event.getReaction().toString().equals("❌"))
				{
					deny(event);
				}
			}
			catch (Exception e)
			{
				Main.log.error("", e);
			}
		}
	}
	
	public void confirm(ReactionAddEvent event) throws Exception {}
	
	public void deny(ReactionAddEvent event) throws Exception {}
}
