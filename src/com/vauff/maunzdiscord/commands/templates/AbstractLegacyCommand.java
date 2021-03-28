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
}
