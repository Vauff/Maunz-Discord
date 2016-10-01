package com.vauff.maunzdiscord.core;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.vauff.maunzdiscord.commands.*;
import com.vauff.maunzdiscord.features.GFLTimer;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;

public class MainListener
{
	private LinkedList<ICommand<MessageReceivedEvent>> commands = new LinkedList<ICommand<MessageReceivedEvent>>();

	public MainListener()
	{
		commands.add(new Map());
	}

	@EventSubscriber
	public void onReady(ReadyEvent event)
	{
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(GFLTimer.timer, 0, 60, TimeUnit.SECONDS);
	}

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event)
	{
		try
		{
			String cmdName = event.getMessage().getContent().split(" ")[0];

			for (ICommand<MessageReceivedEvent> cmd : commands)
			{

				for (String s : cmd.getAliases())
				{
					if (cmdName.equalsIgnoreCase(s))
					{
						cmd.exe(event);
					}
				}

			}
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}
}
