package com.vauff.maunzdiscord.threads;

import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.objects.AwaitButton;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;

import java.util.Random;

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
			if (!AbstractSlashCommand.AWAITED.containsKey(event.getMessageId()))
				return;

			AwaitButton await = AbstractSlashCommand.AWAITED.get(event.getMessageId());

			if (!event.getInteraction().getUser().getId().equals(await.getID()))
				return;

			event.deferReply().block();

			if (event.getMessage().isPresent())
				event.getMessage().get().delete().block();

			try
			{
				await.getCommand().buttonExe(event, event.getCustomId());
			}
			catch (Exception e)
			{
				Random rnd = new Random();
				int code = 100000000 + rnd.nextInt(900000000);

				event.editReply(":exclamation:  |  **An error has occured!**" + System.lineSeparator() + System.lineSeparator() + "If this was an unexpected error, please report it to Vauff in the #bugreports channel at http://discord.gg/MDx3sMz with the error code " + code).block();
				Logger.log.error(code, e);
			}

			return;
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
