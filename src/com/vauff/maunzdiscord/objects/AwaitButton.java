package com.vauff.maunzdiscord.objects;

import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds information for a button that is waiting for input
 */
public class AwaitButton
{
	private Snowflake buttonID;
	private AbstractSlashCommand command;
	private ChatInputInteractionEvent event;

	/**
	 * Various command data to persist across button presses, this is UNTYPED
	 * Use enums in {@link AwaitButton#command} to access different values
	 */
	public List data;

	/**
	 * @param buttonID  The ID of the button
	 * @param command   The command that used this button
	 * @param event     The ChatInputInteractionEvent that triggered the execution of {@link AwaitButton#command}
	 * @param keyValues Amount of keyvalues
	 */
	public AwaitButton(Snowflake buttonID, AbstractSlashCommand command, ChatInputInteractionEvent event, int keyValues)
	{
		this.buttonID = buttonID;
		this.command = command;
		this.event = event;
		data = new ArrayList<>();
	}

	/**
	 * @return The ID of the button
	 */
	public Snowflake getButtonID()
	{
		return buttonID;
	}

	/**
	 * @return The command that used this button
	 */
	public AbstractSlashCommand getCommand()
	{
		return command;
	}

	/**
	 * @return The ChatInputInteractionEvent that triggered the execution of {@link AwaitButton#command}
	 */
	public ChatInputInteractionEvent getEvent()
	{
		return event;
	}
}
