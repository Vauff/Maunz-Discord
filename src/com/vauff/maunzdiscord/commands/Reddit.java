package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Reddit implements ICommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length == 1)
		{
			Util.msg(event.getChannel(), "You need to provide a subreddit!");
		}
		else
		{
			if (args[1].startsWith("/r/"))
			{
				String[] splitArgs = args[1].split("/");
				Util.msg(event.getChannel(), "https://www.reddit.com/r/" + splitArgs[2] + "/");
			}
			else if (args[1].startsWith("r/"))
			{
				String[] splitArgs = args[1].split("/");
				Util.msg(event.getChannel(), "https://www.reddit.com/r/" + splitArgs[1] + "/");
			}
			else
			{
				Util.msg(event.getChannel(), "https://www.reddit.com/r/" + args[1] + "/");
			}
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*reddit" };
	}
}