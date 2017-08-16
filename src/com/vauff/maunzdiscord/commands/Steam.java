package com.vauff.maunzdiscord.commands;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Steam implements ICommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length == 1)
		{
			Util.msg(event.getChannel(), "Please give me a Steam ID!");
		}
		else
		{
			String url;

			if (args[1].matches("[0-9]+"))
			{
				Main.log.info("Detected a numeric input, using the profiles link...");
				url = "https://steamcommunity.com/profiles/" + args[1];
			}
			else
			{
				Main.log.info("Detected an alphanumeric input, using the id link...");
				url = "https://steamcommunity.com/id/" + args[1];
			}

			Document steam = Jsoup.connect(url).get();

			if (steam.title().equals("Steam Community :: Error"))
			{
				Util.msg(event.getChannel(), "That Steam profile doesn't exist!");
			}
			else
			{
				Util.msg(event.getChannel(), "Here you go! " + url);
			}
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*steam" };
	}
}