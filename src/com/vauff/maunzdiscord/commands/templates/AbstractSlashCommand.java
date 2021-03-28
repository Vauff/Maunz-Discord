package com.vauff.maunzdiscord.commands.templates;

import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public abstract class AbstractSlashCommand<M extends ApplicationCommandInteraction> extends AbstractCommand
{
	/**
	 * Executes this command
	 *
	 * @param interaction The interaction that executing this command creates
	 * @throws Exception If an exception gets thrown by any implementing methods
	 */
	public abstract String exe(M interaction, MessageChannel channel, User author) throws Exception;

	/**
	 * Gets the ApplicationCommandRequest object so it can be registered with Discord
	 */
	public abstract ApplicationCommandRequest getCommand();

	/**
	 * Defines the name used to trigger the command.
	 *
	 * @return A string containing the name of the command
	 */
	public abstract String getName();

	@Override
	public final String[] getAliases() { return new String[] { getName() }; }
}
