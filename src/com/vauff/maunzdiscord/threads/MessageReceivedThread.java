package com.vauff.maunzdiscord.threads;

import com.vauff.maunzdiscord.commands.Disable;
import com.vauff.maunzdiscord.commands.Enable;
import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.MainListener;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.features.Intelligence;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.io.File;

public class MessageReceivedThread implements Runnable
{
	private MessageReceivedEvent event;
	private Thread thread;
	private String name;

	public MessageReceivedThread(MessageReceivedEvent passedEvent, String passedName)
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
			String cmdName = event.getMessage().getContent().split(" ")[0];

			for (AbstractCommand<MessageReceivedEvent> cmd : MainListener.commands)
			{
				boolean enabled;

				if (event.getChannel().isPrivate())
				{
					enabled = Util.isEnabled();
				}
				else
				{
					enabled = Util.isEnabled(event.getGuild());
				}

				if (enabled || cmd instanceof Enable || cmd instanceof Disable)
				{
					for (String s : cmd.getAliases())
					{
						if (cmdName.equalsIgnoreCase(s))
						{
							boolean blacklisted = false;

							if (!Util.hasPermission(event.getAuthor(), event.getGuild()) && !event.getChannel().isPrivate())
							{
								JSONObject json = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().getStringID() + ".json")));

								for (int i = 0; i < json.getJSONArray("blacklist").length(); i++)
								{
									String entry = json.getJSONArray("blacklist").getString(i);

									if ((entry.split(":")[0].equalsIgnoreCase(event.getChannel().getStringID()) || entry.split(":")[0].equalsIgnoreCase("all")) && (entry.split(":")[1].equalsIgnoreCase(cmdName.replace("*", "")) || entry.split(":")[1].equalsIgnoreCase("all")))
									{
										blacklisted = true;
										break;
									}
								}
							}

							if (!blacklisted)
							{
								if (!(cmd instanceof Intelligence))
								{
									event.getChannel().setTypingStatus(true);
									Thread.sleep(250);
								}

								try
								{
									cmd.exe(event);
								}
								catch (Exception e)
								{
									String message = ":exclamation:  |  **Uh oh, an error occured!**" + System.lineSeparator() + System.lineSeparator() + "If this was an unexpected error, please report it to Vauff in the #bugreports channel at <https://goo.gl/igb7hc> with the stacktrace provided below" + System.lineSeparator() + "```" + System.lineSeparator() + ExceptionUtils.getStackTrace(e);

									if (message.length() > 1997)
									{
										message = message.substring(0, Math.min(message.length(), 1997));
									}

									Util.msg(event.getChannel(), event.getAuthor(), message + "```");
									Logger.log.error("", e);
								}

								event.getChannel().setTypingStatus(false);
							}
							else
							{
								Util.msg(event.getAuthor().getOrCreatePMChannel(), ":exclamation:  |  **Command/channel blacklisted**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command in " + event.getChannel().mention() + " because a guild administrator has blacklisted either the command or the channel that you ran it in");
							}
						}
					}
				}
			}

			try
			{
				if (AbstractCommand.AWAITED.containsKey(event.getAuthor().getStringID()) && event.getChannel().equals(Main.client.getMessageByID(Long.parseLong(AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).getID())).getChannel()))
				{
					Main.client.getMessageByID(Long.parseLong(AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).getID())).delete();
					AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).getCommand().onMessageReceived(event);
				}
			}
			catch (NullPointerException e)
			{
				// This means that the message ID in AbstractCommand#AWAITED for the given user ID has already been deleted, we can safely just stop executing
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
