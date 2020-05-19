package com.vauff.maunzdiscord.commands.templates;

public class SubCommandHelp
{
	/**
	 * Arguments available for this sub command
	 */
	public String arguments;

	/**
	 * Description of what this sub command does
	 */
	public String description;

	public SubCommandHelp(String arguments, String description)
	{
		this.arguments = arguments;
		this.description = description;
	}
}
