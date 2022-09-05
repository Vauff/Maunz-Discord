package com.vauff.maunzdiscord.threads;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.objects.Await;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.entity.User;

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
			Snowflake messageId = event.getMessageId();
			User user = event.getInteraction().getUser();

			if (!AbstractCommand.AWAITED.containsKey(messageId))
				return;

			Await<AbstractCommand> await = AbstractCommand.AWAITED.get(messageId);

			if (!user.getId().equals(await.getID()))
				return;

			AbstractCommand.AWAITED.remove(messageId);
			event.deferEdit().block();
			await.getCommand().buttonPressed(event, event.getCustomId(), event.getInteraction().getGuild().block(), event.getInteraction().getChannel().block(), user);
		}
		catch (Exception e)
		{
			Random rnd = new Random();
			int code = 100000000 + rnd.nextInt(900000000);

			event.editReply(":exclamation:  |  **An error has occured!**" + System.lineSeparator() + System.lineSeparator() + "If this was an unexpected error, please report it to Vauff in the #bugreports channel at http://discord.gg/MDx3sMz with the error code " + code).block();
			Logger.log.error(code, e);
		}
	}
}
