package com.vauff.maunzdiscord.core;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

import com.vauff.maunzdiscord.commands.*;
import com.vauff.maunzdiscord.features.GFLTimer;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;

public class MainListener
{
	private LinkedList<ICommand<MessageReceivedEvent>> commands = new LinkedList<ICommand<MessageReceivedEvent>>();
	public static StopWatch uptime = new StopWatch();

	public MainListener()
	{
		commands.add(new About());
		commands.add(new Disable());
		commands.add(new Enable());
		commands.add(new Help());
		commands.add(new Map());
		commands.add(new Notify());
		commands.add(new Ping());
		commands.add(new Restart());
		commands.add(new Source());
		commands.add(new Stop());
		commands.add(new Trello());
	}

	@EventSubscriber
	public void onReady(ReadyEvent event)
	{
		File folder = new File(Util.getJarLocation() + "map-notification-data/");

		if (!folder.isDirectory())
		{
			folder.mkdir();
		}
		
		uptime.start();
		GFLTimer.timestamp = System.currentTimeMillis();
		Util.mapChannel = Main.client.getChannelByID(Main.mapChannelID);
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(GFLTimer.timer, 0, 60, TimeUnit.SECONDS);
	}

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event)
	{
		try
		{
			String cmdName = event.getMessage().getContent().split(" ")[0];

			if ((Util.devMode && event.getMessage().getChannel().getStringID().equals("252537749859598338") || event.getMessage().getChannel().isPrivate()) || (Util.devMode == false && !event.getMessage().getChannel().getStringID().equals("252537749859598338")))
			{
				for (ICommand<MessageReceivedEvent> cmd : commands)
				{
					if (Util.isEnabled || cmd instanceof Enable || cmd instanceof Disable)
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
			}
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}
}
