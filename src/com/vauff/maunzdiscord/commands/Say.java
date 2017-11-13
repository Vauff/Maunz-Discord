package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;

public class Say extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (!event.getChannel().isPrivate())
		{
			if (Util.hasPermission(event.getAuthor(), event.getGuild()))
			{
				if (args.length != 1)
				{
					if (args[1].startsWith("<#"))
					{
						if (args.length != 2)
						{
							IChannel channel = Main.client.getChannelByID(Long.parseLong(args[1].replaceAll("[^\\d.]", "")));

							if (channel.getGuild().equals(event.getGuild()))
							{
								try
								{
									Util.msg(channel, Util.addArgs(args, 2));
								}
								catch (DiscordException e)
								{
									Util.msg(event.getChannel(), "Failed to send message, the bot doesn't have send message permissions for this channel");
								}
							}
							else
							{
								Util.msg(event.getChannel(), "Failed to send message, this channel is in another guild!");
							}
						}
						else
						{
							Util.msg(event.getChannel(), "I need a message to send! **Usage: *say [channel] <message>**");
						}

					}
					else
					{
						Util.msg(event.getChannel(), Util.addArgs(args, 1));
					}
				}
				else
				{
					Util.msg(event.getChannel(), "I need a message to send! **Usage: *say [channel] <message>**");
				}
			}
			else
			{
				Util.msg(event.getChannel(), "You do not have permission to use that command");
			}
		}
		else
		{
			Util.msg(event.getChannel(), "This command can't be done in a PM, only in a guild in which you have the administrator permission in");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*say" };
	}
}