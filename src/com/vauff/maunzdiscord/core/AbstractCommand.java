package com.vauff.maunzdiscord.core;

import java.util.HashMap;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;

public abstract class AbstractCommand<M extends MessageReceivedEvent>
{
	/** 
	 * Holds all messages as keys which await a reaction by a specific user.
	 * The values hold an instance of {@link Await}
	 */
	public static final HashMap<String,Await> AWAITED = new HashMap<String,Await>();
	
	/**
	 * Executes this command
	 * @param event The event by which this command got triggered
	 * @throws Exception If an exception gets thrown by any implementing methods
	 */
	public abstract void exe(M event) throws Exception;
	
	/**
	 * Defines aliases that can be used to trigger the command.
	 * The main alias should also be defined in here
	 * @return A string array of all valid aliases
	 */
	public abstract String[] getAliases();
	
	/**
	 * Sets up this command to await a reaction by the user who triggered this command
	 * @param messageID The message which should get reacted on
	 * @param userID The user who triggered this command
	 */
	public final void waitForReaction(String messageID, String userID)
	{
		AWAITED.put(messageID, new Await(userID, this));
	}
	
	/**
	 * Defines if the default implementation of {@link AbstractCommand#onReactionAdd(ReactionAddEvent)}
	 * @return true if the default behavior of said method should be used, false otherwise
	 */
	public boolean confirmable()
	{
		return false;
	}
	
	/**
	 * Gets called when a reaction is added to a message defined prior in {@link AbstractCommand#waitForReaction(String, String)}
	 * @param event The event holding information about the added reaction
	 */
	public void onReactionAdd(ReactionAddEvent event) throws Exception
	{
		if (confirmable())
		{
			try
			{
				if (event.getReaction().getEmoji().toString().equals("✅"))
				{
					confirm(event);
				}
				else if (event.getReaction().getEmoji().toString().equals("❌"))
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
	
	/**
	 * Gets called when {@link AbstractCommand#confirmable()} is set to true and the user reacts with a :white_check_mark: emoji (✅)
	 * @param event The event holding information about the added reaction
	 * @throws Exception If an exception gets thrown by any implementing methods
	 */
	public void confirm(ReactionAddEvent event) throws Exception {}
	
	/**
	 * Gets called when {@link AbstractCommand#confirmable()} is set to true and the user reacts with a :x: emoji (❌)
	 * @param event The event holding information about the added reaction
	 * @throws Exception If an exception gets thrown by any implementing methods
	 */
	public void deny(ReactionAddEvent event) throws Exception {}
}
