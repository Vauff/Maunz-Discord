package com.vauff.maunzdiscord.threads;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;

import java.util.Random;

public class ReactionAddThread implements Runnable
{
	private ReactionAddEvent event;
	private Message message;
	private Thread thread;
	private String name;

	public ReactionAddThread(ReactionAddEvent passedEvent, Message passedMessage, String passedName)
	{
		event = passedEvent;
		message = passedMessage;
		name = passedName;
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
			if (AbstractCommand.AWAITED.containsKey(message.getId()) && event.getUser().block().getId().equals(AbstractCommand.AWAITED.get(message.getId()).getID()) && event.getEmoji().asUnicodeEmoji().isPresent())
			{
				try
				{
					message.delete().block();
					AbstractCommand.AWAITED.get(message.getId()).getCommand().onReactionAdd(event, message);
				}
				catch (Exception e)
				{
					Random rnd = new Random();
					int code = 100000000 + rnd.nextInt(900000000);

					Util.msg(event.getChannel().block(), event.getUser().block(), ":exclamation:  |  **An error has occured!**" + System.lineSeparator() + System.lineSeparator() + "If this was an unexpected error, please report it to Vauff in the #bugreports channel at http://discord.gg/MDx3sMz with the error code " + code);
					Logger.log.error(code, e);
				}
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
