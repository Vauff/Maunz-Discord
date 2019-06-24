package com.vauff.maunzdiscord.core;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;

import java.util.HashMap;

public abstract class AbstractCommand<M extends MessageCreateEvent>
{
	/**
	 * Holds all messages as keys which await a reaction or reply by a specific user.
	 * The values hold an instance of {@link Await}
	 */
	public static final HashMap<Snowflake, Await> AWAITED = new HashMap<>();
	public static final HashMap<Snowflake, String> AWAITEDCHANNEL = new HashMap<>();

	/**
	 * Executes this command
	 *
	 * @param event The event by which this command got triggered
	 * @throws Exception If an exception gets thrown by any implementing methods
	 */
	public abstract void exe(M event, MessageChannel channel, User author) throws Exception;

	/**
	 * Defines aliases that can be used to trigger the command.
	 * The main alias should also be defined in here
	 *
	 * @return A string array of all valid aliases
	 */
	public abstract String[] getAliases();

	/**
	 * Sets up this command to await a reaction by the user who triggered this command
	 *
	 * @param messageID The message which should get reacted on
	 * @param userID    The user who triggered this command
	 */
	public final void waitForReaction(Snowflake messageID, Snowflake userID)
	{
		AWAITED.put(messageID, new Await(userID, this));
	}

	/**
	 * Sets up this command to await a reply by the user who triggered this command
	 *
	 * @param messageID The message which will get deleted afterwards
	 * @param userID    The user who triggered this command
	 */
	public final void waitForReply(Snowflake messageID, Snowflake userID)
	{
		AWAITED.put(userID, new Await(messageID, this));
	}

	/**
	 * Defines if the default implementation of {@link AbstractCommand#onReactionAdd(ReactionAddEvent, Message)}
	 *
	 * @return true if the default behavior of said method should be used, false otherwise
	 */
	public boolean confirmable()
	{
		return false;
	}

	/**
	 * Gets called when a reaction is added to a message defined prior in {@link AbstractCommand#waitForReaction(Snowflake, Snowflake)}
	 *
	 * @param event   The event holding information about the added reaction
	 * @param message The Message that was reacted to
	 */
	public void onReactionAdd(ReactionAddEvent event, Message message) throws Exception
	{
	}

	/**
	 * Gets called when a specific user sends a reply defined prior in {@link AbstractCommand#waitForReply(Snowflake, Snowflake)}
	 *
	 * @param event The event holding information about the reply
	 */
	public void onMessageReceived(MessageCreateEvent event) throws Exception
	{
	}
}
