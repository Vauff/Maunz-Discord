package com.vauff.maunzdiscord.threads;

import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.Button;

public class ButtonInteractionThread implements Runnable
{
	private ButtonInteractionEvent event;
	private Thread thread;
	private String name;

	public ButtonInteractionThread(ButtonInteractionEvent passedEvent, String passedName)
	{
		name = passedName;
		event = passedEvent;
	}

	public void start()
	{
		if (thread == null)
		{
			thread = new Thread(this, name);
			thread.start();
		}
	}

	public void run()
	{
		try
		{
			event.deferReply().block();

			String buttonId = event.getCustomId();

			for (AbstractSlashCommand<ChatInputInteractionEvent> cmd : Main.slashCommands)
			{
				for (Button button : cmd.getButtons())
				{
					if (!button.getCustomId().get().equals(buttonId))
						continue;

					if (event.getMessage().isPresent())
						event.getMessage().get().delete().block();

					cmd.buttonExe(event, buttonId);
					return;
				}
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
