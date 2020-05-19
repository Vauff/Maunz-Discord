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

	/**
	 * Permission level required to use this command
	 * 0 - No permission required
	 * 1 - Guild or bot administrator
	 * 2 - Bot administrator
	 */
	public int permissionLevel;

	public CommandHelp(String[] aliases, SubCommandHelp[] subCommandHelps, int permissionLevel)
	{
		this.aliases = aliases;
		this.subCommandHelps = subCommandHelps;
		this.permissionLevel = permissionLevel;
	}
}
