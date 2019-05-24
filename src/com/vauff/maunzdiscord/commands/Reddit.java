package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Reddit extends AbstractCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().get().split(" ");

		if (args.length == 1)
		{
			Util.msg(channel, author, "You need to provide a subreddit!");
		}
		else
		{
			String[] splitArgs = args[1].split("/");
			String url;

			if (args[1].startsWith("/r/"))
			{
				url = splitArgs[2] + "/";
			}
			else if (args[1].startsWith("r/"))
			{
				url = splitArgs[1] + "/";
			}
			else
			{
				url = args[1] + "/";
			}

			Document reddit = Jsoup.connect("https://old.reddit.com/r/" + url).ignoreHttpErrors(true).get();

			if (reddit.title().contains(": page not found") || reddit.title().equals("search results"))
			{
				Util.msg(channel, author, "That subreddit doesn't exist!");
			}
			else if (reddit.title().contains(": banned"))
			{
				Util.msg(channel, author, "That subreddit is banned!");
			}
			else
			{
				Util.msg(channel, author, "https://reddit.com/r/" + url);
			}
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*reddit" };
	}
}