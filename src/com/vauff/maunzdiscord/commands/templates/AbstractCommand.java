package com.vauff.maunzdiscord.commands.templates;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;

import java.util.HashMap;

public abstract class AbstractCommand
{
	/**
	 * Holds all messages as keys which await a reaction or reply by a specific user.
	 * The values hold an instance of {@link Await}
	 */
	public static final HashMap<Snowflake, Await> AWAITED = new HashMap<>();
	public static final HashMap<Snowflake, Snowflake> AWAITEDCHANNEL = new HashMap<>();

	/**
	 * Enum holding the different bot permissions commands may require to use
	 *
	 * EVERYONE - No permission required
	 * GUILD_ADMIN - ADMINISTRATOR or MANAGE_GUILD permission required
	 * BOT_ADMIN - User must be listed in config.json botOwners
	 */
	public enum BotPermission
	{
		EVERYONE,
		GUILD_ADMIN,
		BOT_ADMIN
	}

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
	 * Gets called when a specific user sends a reply defined prior in {@link AbstractLegacyCommand#waitForReply(Snowflake, Snowflake)}
	 *
	 * @param event The event holding information about the reply
	 */
	public void onMessageReceived(MessageCreateEvent event) throws Exception
	{
	}

	/**
	 * Defines aliases that can be used to trigger the command.
	 * The main alias should also be defined in here
	 *
	 * @return A string array of all valid aliases
	 */
	public abstract String[] getAliases();

	/**
	 * Permission level required to use this command
	 *
	 * @return The permission level
	 */
	public abstract BotPermission getPermissionLevel();

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
	 * Defines if the default implementation of {@link AbstractLegacyCommand#onReactionAdd(ReactionAddEvent, Message)}
	 *
	 * @return true if the default behavior of said method should be used, false otherwise
	 */
	public boolean confirmable()
	{
		return false;
	}
}
