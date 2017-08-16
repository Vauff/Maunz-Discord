package com.vauff.maunzdiscord.commands;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
			String[] splitArgs = args[1].split("/");
			String url;

			if (args[1].startsWith("/r/"))
			{
				url = "https://www.reddit.com/r/" + splitArgs[2] + "/";
			}
			else if (args[1].startsWith("r/"))
			{
				url = "https://www.reddit.com/r/" + splitArgs[1] + "/";
			}
			else
			{
				url = "https://www.reddit.com/r/" + args[1] + "/";
			}

			Document reddit = Jsoup.connect(url).ignoreHttpErrors(true).get();

			if (reddit.title().equals("search results"))
			{
				Util.msg(event.getChannel(), "That subreddit doesn't exist!");
			}
			else if (reddit.title().contains(": banned"))
			{
				Util.msg(event.getChannel(), "That subreddit is banned!");
			}
			else if (reddit.title().contains(": private"))
			{
				Util.msg(event.getChannel(), "That subreddit is private!");
			}
			else
			{
				Util.msg(event.getChannel(), url);
			}
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*reddit" };
	}
}