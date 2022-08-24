package com.vauff.maunzdiscord.commands.templates;

import com.vauff.maunzdiscord.objects.Await;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.HashMap;

public abstract class AbstractLegacyCommand<M extends MessageCreateEvent> extends AbstractCommand
{
	/**
	 * Holds all messages as keys which await a reaction by a specific user.
	 * The values hold an instance of {@link Await}
	 */
	public static final HashMap<Snowflake, Await> AWAITED = new HashMap<>();

	/**
	 * Executes this command
	 *
	 * @param event The event by which this command got triggered
	 * @throws Exception If an exception gets thrown by any implementing methods
	 */
	public abstract void exe(M event, MessageChannel channel, User author) throws Exception;

	/**
	 * Gets called when a reaction is added to a message defined prior in {@link AbstractLegacyCommand#waitForReaction(Snowflake, Snowflake)}
	 *
	 * @param event   The event holding information about the added reaction
	 * @param message The Message that was reacted to
	 */
	public void onReactionAdd(ReactionAddEvent event, Message message) throws Exception
	{
	}

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
}
