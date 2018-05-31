package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.HashMap;

public class Changelog extends AbstractCommand<MessageReceivedEvent>
{
	private static HashMap<String, Integer> listPages = new HashMap<>();
	private static HashMap<String, String> listMessages = new HashMap<>();
	private static HashMap<String, String> listVersions = new HashMap<>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");
		Document doc = null;
		ArrayList<String> changelog = new ArrayList<>();
		String link;
		String title;
		String version;

		if (args.length == 1)
		{
			version = "v" + Main.version;
		}
		else
		{
			if (args[1].startsWith("v"))
			{
				version = args[1];
			}
			else
			{
				version = "v" + args[1];
			}
		}

		link = "https://github.com/Vauff/Maunz-Discord/releases/tag/" + version;
		title = "Maunz " + version;

		try
		{
			doc = Jsoup.connect(link).userAgent(" ").get();
		}
		catch (HttpStatusException e)
		{
			Util.msg(event.getChannel(), event.getAuthor(), "That version of Maunz doesn't exist!");
			return;
		}

		String html = doc.select("div[class=\"markdown-body\"]").html().replace("<strong>", "").replace("</strong>", "").replace("<ul>", "").replace("</ul>", "").replace("<li>", "").replace("</li>", "");
		String[] split = html.split("\n");

		for (int i = 1; i < split.length; i++)
		{
			if (!split[i].replace(" ", "").equals(""))
			{
				changelog.add("-" + split[i]);
			}
		}

		Util.msg(event.getChannel(), event.getAuthor(), "GitHub link: " + "<" + link + ">");

		IMessage m = Util.buildPage(changelog, title, 10, 1, false, true, event.getChannel(), event.getAuthor());

		listMessages.put(event.getAuthor().getStringID(), m.getStringID());
		listVersions.put(event.getAuthor().getStringID(), version);
		waitForReaction(m.getStringID(), event.getAuthor().getStringID());
		listPages.put(event.getAuthor().getStringID(), 1);
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event) throws Exception
	{
		if (listMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(listMessages.get(event.getUser().getStringID())))
			{
				if (event.getReaction().getEmoji().toString().equals("▶"))
				{
					Document doc = null;
					ArrayList<String> changelog = new ArrayList<>();
					String link;
					String title;

					link = "https://github.com/Vauff/Maunz-Discord/releases/tag/" + listVersions.get(event.getUser().getStringID());
					title = "Maunz " + listVersions.get(event.getUser().getStringID());

					try
					{
						doc = Jsoup.connect(link).userAgent(" ").get();
					}
					catch (HttpStatusException e)
					{
						Util.msg(event.getChannel(), event.getUser(), "That version of Maunz doesn't exist!");
						return;
					}

					String html = doc.select("div[class=\"markdown-body\"]").html().replace("<strong>", "").replace("</strong>", "").replace("<ul>", "").replace("</ul>", "").replace("<li>", "").replace("</li>", "");
					String[] split = html.split("\n");

					for (int i = 1; i < split.length; i++)
					{
						if (!split[i].replace(" ", "").equals(""))
						{
							changelog.add("-" + split[i]);
						}
					}

					IMessage m = Util.buildPage(changelog, title, 10, listPages.get(event.getUser().getStringID()) + 1, false, true, event.getChannel(), event.getUser());

					listMessages.put(event.getUser().getStringID(), m.getStringID());
					waitForReaction(m.getStringID(), event.getUser().getStringID());
					listPages.put(event.getUser().getStringID(), listPages.get(event.getUser().getStringID()) + 1);
				}

				else if (event.getReaction().getEmoji().toString().equals("◀"))
				{
					Document doc = null;
					ArrayList<String> changelog = new ArrayList<>();
					String link;
					String title;

					link = "https://github.com/Vauff/Maunz-Discord/releases/tag/" + listVersions.get(event.getUser().getStringID());
					title = "Maunz " + listVersions.get(event.getUser().getStringID());

					try
					{
						doc = Jsoup.connect(link).userAgent(" ").get();
					}
					catch (HttpStatusException e)
					{
						Util.msg(event.getChannel(), event.getUser(), "That version of Maunz doesn't exist!");
						return;
					}

					String html = doc.select("div[class=\"markdown-body\"]").html().replace("<strong>", "").replace("</strong>", "").replace("<ul>", "").replace("</ul>", "").replace("<li>", "").replace("</li>", "");
					String[] split = html.split("\n");

					for (int i = 1; i < split.length; i++)
					{
						if (!split[i].replace(" ", "").equals(""))
						{
							changelog.add("-" + split[i]);
						}
					}

					IMessage m = Util.buildPage(changelog, title, 10, listPages.get(event.getUser().getStringID()) - 1, false, true, event.getChannel(), event.getUser());

					listMessages.put(event.getUser().getStringID(), m.getStringID());
					waitForReaction(m.getStringID(), event.getUser().getStringID());
					listPages.put(event.getUser().getStringID(), listPages.get(event.getUser().getStringID()) - 1);
				}
			}
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*changelog" };
	}
}