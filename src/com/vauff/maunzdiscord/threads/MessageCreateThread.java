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
import discord4j.core.object.util.Snowflake;
import org.json.JSONObject;

import java.io.File;
import java.util.Random;

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
								if (MainListener.cooldownTimestamps.containsKey(author.getId().asString()) && (MainListener.cooldownTimestamps.get(author.getId().asString()) + 2000L) > System.currentTimeMillis())
								{
									if (MainListener.cooldownMessageTimestamps.containsKey(author.getId().asString()) && (MainListener.cooldownMessageTimestamps.get(author.getId().asString()) + 10000L) < System.currentTimeMillis())
									{
										Util.msg(channel, author, author.getMention() + " Slow down!");
										MainListener.cooldownMessageTimestamps.put(author.getId().asString(), System.currentTimeMillis());
									}
									else if (!MainListener.cooldownMessageTimestamps.containsKey(author.getId().asString()))
									{
										Util.msg(channel, author, author.getMention() + " Slow down!");
										MainListener.cooldownMessageTimestamps.put(author.getId().asString(), System.currentTimeMillis());
									}

									return;
								}

								MainListener.cooldownTimestamps.put(author.getId().asString(), System.currentTimeMillis());
								boolean blacklisted = false;

								if (!Util.hasPermission(author, event.getGuild().block()) && !(channel instanceof PrivateChannel))
								{
									JSONObject json = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().block().getId().asString() + ".json")));

									for (int i = 0; i < json.getJSONArray("blacklist").length(); i++)
									{
										String entry = json.getJSONArray("blacklist").getString(i);

										if ((entry.split(":")[0].equalsIgnoreCase(channel.getId().asString()) || entry.split(":")[0].equalsIgnoreCase("all")) && (entry.split(":")[1].equalsIgnoreCase(cmdName.replace("*", "")) || entry.split(":")[1].equalsIgnoreCase("all")))
										{
											blacklisted = true;
											break;
										}
									}
								}

								if (!blacklisted)
								{
									channel.type().block();
									Thread.sleep(250);

									try
									{
										cmd.exe(event, event.getMessage().getChannel().block(), event.getMessage().getAuthor().get());
									}
									catch (Exception e)
									{
										Random rnd = new Random();
										int code = 100000 + rnd.nextInt(900000);

										Util.msg(channel, author, ":exclamation:  |  **Uh oh, an error occured!**" + System.lineSeparator() + System.lineSeparator() + "If this was an unexpected error, please report it to Vauff in the #bugreports channel at http://discord.gg/MDx3sMz with the error code " + code);
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
					if (AbstractCommand.AWAITED.containsKey(author.getId().asString()) && channel.getId().asString().equals(AbstractCommand.AWAITEDCHANNEL.get(author.getId().asString())))
					{
						Main.client.getMessageById(channel.getId(), Snowflake.of(AbstractCommand.AWAITED.get(author.getId().asString()).getID())).block().delete();
						AbstractCommand.AWAITED.get(author.getId().asString()).getCommand().onMessageReceived(event);
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
