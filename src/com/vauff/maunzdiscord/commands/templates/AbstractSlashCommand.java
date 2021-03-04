package com.vauff.maunzdiscord.commands.templates;

public abstract class AbstractSlashCommand extends AbstractCommand
{
	/**
	 * Defines the name used to trigger the command.
	 *
	 * @return A string containing the name of the command
	 */
	public abstract String getName();
}
