package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import discord4j.rest.http.client.ClientException;

public class Say extends AbstractCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().get().split(" ");

		if (!(channel instanceof PrivateChannel))
		{
			if (Util.hasPermission(author, event.getGuild().block()))
			{
				if (args.length != 1)
				{
					if (args[1].startsWith("<#"))
					{
						if (args.length != 2)
						{
							try
							{
								GuildChannel sendChannel = (GuildChannel) Main.client.getChannelById(Snowflake.of(args[1].replaceAll("[^\\d.]", ""))).block();

								if (sendChannel.getGuild().block().equals(event.getGuild().block()))
								{
									if (Util.msg((MessageChannel) sendChannel, Util.addArgs(args, 2)) != null)
									{
										Util.msg(channel, author, "Successfully sent message!");
									}
									else
									{
										Util.msg(channel, author, "Failed to send message, the bot doesn't have permissions for this channel");
									}

								}
								else
								{
									Util.msg(channel, author, "Failed to send message, that channel is in another guild!");
								}
							}
							catch (ClientException e)
							{
								Util.msg(channel, author, "Failed to send message, that channel is in another guild!");
							}
						}
						else
						{
							Util.msg(channel, author, "I need a message to send! **Usage: *say [channel] <message>**");
						}
					}
					else
					{
						Util.msg(channel, Util.addArgs(args, 1));
					}
				}
				else
				{
					Util.msg(channel, author, "I need a message to send! **Usage: *say [channel] <message>**");
				}
			}
			else
			{
				Util.msg(channel, author, "You do not have permission to use that command");
			}
		}
		else
		{
			Util.msg(channel, author, "This command can't be done in a PM, only in a guild in which you have the administrator permission in");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*say" };
	}
}