package com.vauff.maunzdiscord.threads;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.MainListener;
import com.vauff.maunzdiscord.core.Util;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.http.client.ClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessageCreateThread implements Runnable
{
	private MessageCreateEvent event;
	private Thread thread;
	private String name;

	// These users have already been notified of the upcoming slash command migration
	// There's a week left so I'm not bothering to store this in the database, sue me
	private static List<Snowflake> slashCmdNotifiedUsers = new ArrayList();

	public MessageCreateThread(MessageCreateEvent passedEvent, String passedName)
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
			if (event.getMessage().getContent().isEmpty() || !event.getMessage().getAuthor().isPresent() || event.getMessage().getAuthor().get().isBot())
				return;

			String cmdName = event.getMessage().getContent().split(" ")[0];
			User author = event.getMessage().getAuthor().get();
			MessageChannel channel = event.getMessage().getChannel().block();

			if (cmdName.startsWith(Main.cfg.getPrefix()))
			{
				for (AbstractLegacyCommand<MessageCreateEvent> cmd : Main.legacyCommands)
				{
					for (String s : cmd.getAliases())
					{
						if (!cmdName.equalsIgnoreCase(Main.cfg.getPrefix() + s))
							continue;

						if (MainListener.cooldownTimestamps.containsKey(author.getId()) && (MainListener.cooldownTimestamps.get(author.getId()) + 2000L) > System.currentTimeMillis())
						{
							if ((!MainListener.cooldownMessageTimestamps.containsKey(author.getId())) || (MainListener.cooldownMessageTimestamps.containsKey(author.getId()) && (MainListener.cooldownMessageTimestamps.get(author.getId()) + 10000L) < System.currentTimeMillis()))
							{
								Util.msg(channel, true, true, author.getMention() + " Slow down!", null);
								MainListener.cooldownMessageTimestamps.put(author.getId(), System.currentTimeMillis());
							}

							return;
						}

						MainListener.cooldownTimestamps.put(author.getId(), System.currentTimeMillis());

						try
						{
							try
							{
								try
								{
									channel.type().block();
								}
								catch (ClientException e)
								{
									// Apparently Discord will randomly 403 this endpoint whenever they feel like it
									if (e.getStatus().code() != 403)
										throw e;
								}

								//if msg shows up too quickly, message history can somehow get out of order
								Thread.sleep(250);

								if ((cmd.getPermissionLevel() == AbstractCommand.BotPermission.GUILD_ADMIN && !Util.hasPermission(author, event.getGuild().block())) || (cmd.getPermissionLevel() == AbstractCommand.BotPermission.BOT_ADMIN && !Util.hasPermission(author)))
								{
									Util.msg(channel, "You do not have permission to use that command");
									return;
								}

								cmd.exe(event, channel, author);

								if (!slashCmdNotifiedUsers.contains(author.getId()))
								{
									Util.msg(channel, false, true, "Hey " + author.getMention() + "!" + System.lineSeparator() + System.lineSeparator() + "In case you were unaware, Discord will be enforcing slash command usage starting from **September 1st**. This means from that date onwards prefixing Maunz commands with ***** will stop working, and you will have to use the **/** prefix for all commands instead." + System.lineSeparator() + System.lineSeparator() + "While not every command is ready just yet (including the one you just ran), you can type **/** into your chat window right now, and see all the commands that have already been moved into this system.", null);
									slashCmdNotifiedUsers.add(author.getId());
								}
							}
							catch (ClientException e)
							{
								if (e.getStatus().code() == 403)
								{
									Util.msg(author.getPrivateChannel().block(), true, ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command in " + channel.getMention() + " because it's lacking permissions." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
									return;
								}
								else
								{
									throw e;
								}
							}
						}
						catch (Exception e)
						{
							Random rnd = new Random();
							int code = 100000000 + rnd.nextInt(900000000);

							Util.msg(channel, true, ":exclamation:  |  **An error has occured!**" + System.lineSeparator() + System.lineSeparator() + "If this was an unexpected error, please report it to Vauff in the #bugreports channel at http://discord.gg/MDx3sMz with the error code " + code);
							Logger.log.error(code, e);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
