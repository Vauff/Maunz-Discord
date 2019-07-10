package com.vauff.maunzdiscord.threads;

import com.vauff.maunzdiscord.commands.Disable;
import com.vauff.maunzdiscord.commands.Enable;
import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.MainListener;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.User;
import discord4j.rest.http.client.ClientException;

import java.util.List;
import java.util.Random;

import static com.mongodb.client.model.Filters.eq;

public class MessageCreateThread implements Runnable
{
	private MessageCreateEvent event;
	private Thread thread;
	private String name;

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
			if (event.getMessage().getContent().isPresent() && event.getMessage().getAuthor().isPresent())
			{
				String cmdName = event.getMessage().getContent().get().split(" ")[0];
				User author = event.getMessage().getAuthor().get();
				MessageChannel channel = event.getMessage().getChannel().block();

				for (AbstractCommand<MessageCreateEvent> cmd : MainListener.commands)
				{
					for (String s : cmd.getAliases())
					{
						if (cmdName.equalsIgnoreCase(s))
						{
							boolean enabled;

							if (channel instanceof PrivateChannel)
							{
								enabled = Util.isEnabled();
							}
							else
							{
								enabled = Util.isEnabled(event.getGuild().block());
							}

							if (enabled || cmd instanceof Enable || cmd instanceof Disable)
							{
								if (MainListener.cooldownTimestamps.containsKey(author.getId()) && (MainListener.cooldownTimestamps.get(author.getId()) + 2000L) > System.currentTimeMillis())
								{
									if ((!MainListener.cooldownMessageTimestamps.containsKey(author.getId())) || (MainListener.cooldownMessageTimestamps.containsKey(author.getId()) && (MainListener.cooldownMessageTimestamps.get(author.getId()) + 10000L) < System.currentTimeMillis()))
									{
										Util.msg(channel, author, author.getMention() + " Slow down!");
										MainListener.cooldownMessageTimestamps.put(author.getId(), System.currentTimeMillis());
									}

									return;
								}

								MainListener.cooldownTimestamps.put(author.getId(), System.currentTimeMillis());
								boolean blacklisted = false;

								if (!(channel instanceof PrivateChannel) && !Util.hasPermission(author, event.getGuild().block()))
								{
									List<String> blacklist = Main.mongoDatabase.getCollection("guilds").find(eq("guildId", event.getGuild().block().getId().asLong())).first().getList("blacklist", String.class);

									for (String entry : blacklist)
									{
										if ((entry.split(":")[0].equalsIgnoreCase(channel.getId().asString()) || entry.split(":")[0].equalsIgnoreCase("all")) && (entry.split(":")[1].equalsIgnoreCase(cmdName.replace("*", "")) || entry.split(":")[1].equalsIgnoreCase("all")))
										{
											blacklisted = true;
											break;
										}
									}
								}

								if (!blacklisted)
								{
									try
									{
										channel.type().block();
									}
									catch (ClientException e)
									{
										if (e.getStatus().code() == 403)
										{
											Util.msg(author.getPrivateChannel().block(), ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command in " + channel.getMention() + " because it's lacking permissions." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
										}
										else
										{
											Logger.log.error("", e);
										}
									}

									Thread.sleep(250);

									try
									{
										cmd.exe(event, channel, author);
									}
									catch (Exception e)
									{
										Random rnd = new Random();
										int code = 100000000 + rnd.nextInt(900000000);

										Util.msg(channel, author, ":exclamation:  |  **An error has occured!**" + System.lineSeparator() + System.lineSeparator() + "If this was an unexpected error, please report it to Vauff in the #bugreports channel at http://discord.gg/MDx3sMz with the error code " + code);
										Logger.log.error(code, e);
									}
								}
								else
								{
									Util.msg(author.getPrivateChannel().block(), ":exclamation:  |  **Command/channel blacklisted**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command in " + channel.getMention() + " because a guild administrator has blacklisted either the command or the channel that you ran it in");
								}
							}
						}
					}
				}

				try
				{
					if (AbstractCommand.AWAITED.containsKey(author.getId()) && channel.getId().equals(AbstractCommand.AWAITEDCHANNEL.get(author.getId())))
					{
						Main.client.getMessageById(channel.getId(), AbstractCommand.AWAITED.get(author.getId()).getID()).block().delete();
						AbstractCommand.AWAITED.get(author.getId()).getCommand().onMessageReceived(event);
					}
				}
				catch (NullPointerException e)
				{
					// This means that the message ID in AbstractCommand#AWAITED for the given user ID has already been deleted, we can safely just stop executing
				}
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
