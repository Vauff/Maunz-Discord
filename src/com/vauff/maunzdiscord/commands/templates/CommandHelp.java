package com.vauff.maunzdiscord.commands.templates;

public class CommandHelp
{
	/**
	 * Arguments available for this command
	 */
	public String arguments;

	/**
	 * Description of what this command does
	 */
	public String description;

	public CommandHelp(String arguments, String description)
	{
		this.arguments = arguments;
		this.description = description;
	}
}