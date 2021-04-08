package com.vauff.maunzdiscord.objects;

public class CommandHelp
{
	private String arguments;
	private String description;

	/**
	 * @param arguments   Arguments available for this command
	 * @param description Description of what this command does
	 */
	public CommandHelp(String arguments, String description)
	{
		this.arguments = arguments;
		this.description = description;
	}

	/**
	 * @return The arguments available for this command
	 */
	public String getArguments()
	{
		return arguments;
	}

	/**
	 * @return The description of what this command does
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param arguments The arguments available for this command
	 */
	public void setArguments(String arguments)
	{
		this.arguments = arguments;
	}
}