package com.vauff.maunzdiscord.core;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

import com.vauff.maunzdiscord.commands.*;
import com.vauff.maunzdiscord.features.UptimeTimer;
import com.vauff.maunzdiscord.features.MapTimer;
import com.vauff.maunzdiscord.features.StatsTimer;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;

public class MainListener
{
	/** Holds all commands */
	private LinkedList<AbstractCommand<MessageReceivedEvent>> commands = new LinkedList<AbstractCommand<MessageReceivedEvent>>();
	/** A watch to keep track of the uptime of the bot */
	public static StopWatch uptime = new StopWatch();

	/**
	 * Sets up all commands
	 */
	public MainListener()
	{
		commands.add(new About());
		commands.add(new AccInfo());
		commands.add(new Benchmark());
		commands.add(new Changelog());
		commands.add(new Disable());
		commands.add(new Enable());
		commands.add(new Help());
		commands.add(new IsItDown());
		commands.add(new Map());
		commands.add(new Notify());
		commands.add(new Ping());
		commands.add(new Players());
		commands.add(new Quote());
		commands.add(new Reddit());
		commands.add(new Restart());
		commands.add(new Services());
		commands.add(new Source());
		commands.add(new Steam());
		commands.add(new Stop());
		commands.add(new Trello());
	}

	@EventSubscriber
	public void onReady(ReadyEvent event)
	{
		List<File> folderList = new ArrayList<File>();
		
		folderList.add(new File(Util.getJarLocation() + "services/"));
		folderList.add(new File(Util.getJarLocation() + "services/map-tracking/"));
		folderList.add(new File(Util.getJarLocation() + "services/csgo-updates/"));

		for (File folder : folderList)
		{
			if (!folder.isDirectory())
			{
				folder.mkdir();
			}
		}

		uptime.start();
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(MapTimer.timer, 0, 60, TimeUnit.SECONDS);
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(UptimeTimer.timer, 60, 60, TimeUnit.SECONDS);
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(StatsTimer.timer, 0, 300, TimeUnit.SECONDS);
	}

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event)
	{
		try
		{
			String cmdName = event.getMessage().getContent().split(" ")[0];

			if ((Util.devMode && event.getChannel().getStringID().equals("252537749859598338") || event.getChannel().getStringID().equals("340273634331459594") || event.getChannel().isPrivate()) || (Util.devMode == false && !event.getChannel().getStringID().equals("252537749859598338")))
			{
				for (AbstractCommand<MessageReceivedEvent> cmd : commands)
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

	@EventSubscriber
	public void onReactionAdd(ReactionAddEvent event)
	{
		try
		{
			if (AbstractCommand.AWAITED.containsKey(event.getMessage().getStringID()) && event.getUser().getStringID().equals(AbstractCommand.AWAITED.get(event.getMessage().getStringID()).getUserID()))
			{
				event.getMessage().delete();
				AbstractCommand.AWAITED.get(event.getMessage().getStringID()).getCommand().onReactionAdd(event);
				AbstractCommand.AWAITED.remove(event.getMessage().getStringID());
			}
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}
}
