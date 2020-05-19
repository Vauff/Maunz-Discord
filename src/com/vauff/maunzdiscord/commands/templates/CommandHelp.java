package com.vauff.maunzdiscord.commands.templates;

public class CommandHelp
{
	/**
	 * Array of valid command aliases, first is treated as primary
	 */
	public String[] aliases;

	/**
	 * List of sub commands for this command (different behaviour with different arguments)
	 */
	public SubCommandHelp[] subCommandHelps;

	public CommandHelp(String[] aliases, SubCommandHelp[] subCommandHelps)
	{
		this.aliases = aliases;
		this.subCommandHelps = subCommandHelps;
	}
}
