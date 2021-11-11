package com.vauff.maunzdiscord.commands.templates;

import com.vauff.maunzdiscord.objects.CommandHelp;

public abstract class AbstractCommand
{
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
	 * Defines aliases that can be used to trigger the command.
	 * The main alias should also be defined in here
	 *
	 * @return A string array of all valid aliases
	 */
	public abstract String[] getAliases();

	/**
	 * Helper method that returns the primary (first) alias
	 *
	 * @return The primary alias
	 */
	public final String getFirstAlias()
	{
		return getAliases()[0];
	}

	/**
	 * An array of CommandHelp objects that hold information about the command to display in /help
	 *
	 * @return An array of {@link CommandHelp}
	 */
	public abstract CommandHelp[] getHelp();

	/**
	 * Permission level required to use this command
	 *
	 * @return The permission level
	 */
	public abstract BotPermission getPermissionLevel();
}
