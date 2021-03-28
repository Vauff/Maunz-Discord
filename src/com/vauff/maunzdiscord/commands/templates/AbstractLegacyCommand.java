package com.vauff.maunzdiscord.commands.templates;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

public abstract class AbstractLegacyCommand<M extends MessageCreateEvent> extends AbstractCommand
{
	/**
	 * Executes this command
	 *
	 * @param event The event by which this command got triggered
	 * @throws Exception If an exception gets thrown by any implementing methods
	 */
	public abstract void exe(M event, MessageChannel channel, User author) throws Exception;

	// This needs to be moved to AbstractCommand at some point, but I'm currently unsure how I want to implement it there
	/**
	 * An array of objects that hold information about the command to display in *help
	 *
	 * @return An array of {@link CommandHelp}
	 */
	public abstract CommandHelp[] getHelp();
}
