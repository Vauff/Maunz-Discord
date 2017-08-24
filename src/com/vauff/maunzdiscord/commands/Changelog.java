package com.vauff.maunzdiscord.commands;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

public class Changelog extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");
		Document doc = null;

		try
		{
			StringBuilder changelog = new StringBuilder();
			changelog.append("```" + System.lineSeparator());
			String html;
			String[] split;
			String link;

			if (args.length == 1)
			{
				link = "https://github.com/Vauff/Maunz-Discord/releases/tag/v" + Main.version;
				doc = Jsoup.connect(link).userAgent(" ").get();
				changelog.append("-- Maunz v" + Main.version + " --");
			}
			else
			{
				if (args[1].startsWith("v"))
				{
					link = "https://github.com/Vauff/Maunz-Discord/releases/tag/" + args[1];
					doc = Jsoup.connect(link).userAgent(" ").get();
					changelog.append("-- Maunz " + args[1] + " --");
				}
				else
				{
					link = "https://github.com/Vauff/Maunz-Discord/releases/tag/v" + args[1];
					doc = Jsoup.connect(link).userAgent(" ").get();
					changelog.append("-- Maunz v" + args[1] + " --");
				}
			}

			changelog.append(System.lineSeparator() + System.lineSeparator());

			html = doc.select("div[class=\"markdown-body\"]").html().replace("<strong>", "").replace("</strong>", "").replace("<ul>", "").replace("</ul>", "").replace("<li>", "").replace("</li>", "");
			split = html.split(System.lineSeparator());

			for (int i = 1; i < split.length; i++)
			{
				if (!split[i].replace(" ", "").equals(""))
				{
					changelog.append("- " + split[i] + System.lineSeparator());
				}
			}

			changelog.append("```");
			Util.msg(event.getChannel(), changelog.toString());
			Util.msg(event.getChannel(), "GitHub link: " + "<" + link + ">");
		}
		catch (HttpStatusException e)
		{
			Util.msg(event.getChannel(), "That version of Maunz doesn't exist!");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*changelog" };
	}
}