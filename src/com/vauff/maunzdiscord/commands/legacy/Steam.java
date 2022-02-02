package com.vauff.maunzdiscord.commands.legacy;

import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.objects.CommandHelp;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;

public class Steam extends AbstractLegacyCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length == 1)
		{
			Util.msg(channel, "You need to provide a Steam ID! **Usage: " + Main.cfg.getPrefix() + "steam <steamid>**");
		}
		else
		{
			try
			{
				String url = "https://steamid.xyz/" + args[1].replace("\n", "").replace("\r", "");
				Document site = Jsoup.connect(url).get();
				String siteHtml = site.html();
				String siteText = site.text();

				if (siteText.contains("Player Not Found :( or hasn't set public profile Supported inputs") || siteText.contains("Steam Id Finder - is the simplest way to find steam id online by anything you know:"))
				{
					Util.msg(channel, "Couldn't find a Steam profile with that given ID!");
				}
				else
				{
					String realName = "N/A";
					String country = "N/A";
					String creationDate = "N/A";
					String avatarURL = siteHtml.split("<img class=\"avatar\" src=\"")[1].split("\"")[0];
					String nickname = siteHtml.split("Nick Name \n    <input type=\"text\" onclick=\"this.select\\(\\);\" value=\"")[1].split("\"")[0];
					String lastLogoff = siteHtml.split("<i>Last Logoff:</i> ")[1].split("\n    ")[0];
					String status = siteHtml.split("<i>Status:</i> ")[1].split("\n    ")[0];
					String visibility = siteHtml.split("<i>Visibility:</i> ")[1].split("\n    ")[0];
					String bans = "";
					String steamID = siteHtml.split("Steam ID \n    <input type=\"text\" onclick=\"this.select\\(\\);\" value=\"")[1].split("\"")[0];
					String steamID3 = siteHtml.split("Steam ID3 \n    <input type=\"text\" onclick=\"this.select\\(\\);\" value=\"")[1].split("\"")[0];
					String steam32ID = siteHtml.split("Steam32 ID \n    <input type=\"text\" onclick=\"this.select\\(\\);\" value=\"")[1].split("\"")[0];
					String steam64ID = siteHtml.split("Steam64 ID \n    <input type=\"text\" onclick=\"this.select\\(\\);\" value=\"")[1].split("\"")[0];
					String link = siteHtml.split("Profile URL \n    <input type=\"text\" onclick=\"this.select\\(\\);\" value=\"")[1].split("\"")[0];

					if (!siteHtml.split("<i>Real Name:</i> ")[1].split("\n    ")[0].equalsIgnoreCase(""))
					{
						realName = siteHtml.split("<i>Real Name:</i> ")[1].split("\n    ")[0];
					}

					if (!siteHtml.split("<i>Country:</i> ")[1].split("\n    ")[0].equalsIgnoreCase(""))
					{
						country = ":flag_" + siteHtml.split("<i>Country:</i> ")[1].split("\n    ")[0].toLowerCase() + ":";
					}

					if (!siteHtml.split("<i>Account Created:</i> ")[1].split("\n    ")[0].equalsIgnoreCase("01 Jan 1970"))
					{
						creationDate = siteHtml.split("<i>Account Created:</i> ")[1].split("\n    ")[0];
					}

					if (visibility.equalsIgnoreCase("private"))
					{
						status = "N/A";
					}

					if (siteHtml.contains("\">X VAC</em>"))
					{
						bans += "VAC, ";
					}

					if (siteHtml.contains("\">X Trade</em>"))
					{
						bans += "Trade, ";
					}

					if (siteHtml.contains("\">X Community</em>"))
					{
						bans += "Community";
					}

					if (bans.equalsIgnoreCase(""))
					{
						bans = "None";
					}

					if (bans.substring(bans.length() - 2).equalsIgnoreCase(", "))
					{
						bans = bans.substring(0, bans.length() - 2);
					}

					URL constructedURL = new URL(avatarURL);

					EmbedCreateSpec embed = EmbedCreateSpec.builder()
						.color(Util.averageColorFromURL(constructedURL, true))
						.thumbnail(avatarURL)
						.footer("Powered by steamid.xyz", "https://i.imgur.com/GuXJIeX.png")
						.title(nickname)
						.url(link)
						.addField("Name", nickname, true)
						.addField("Real Name", realName, true)
						.addField("Country", country, true)
						.addField("Account Created", creationDate, true)
						.addField("Last Logoff", lastLogoff, true)
						.addField("Status", status, true)
						.addField("Profile Visibility", visibility, true)
						.addField("Bans", bans, true)
						.addField("Steam ID", steamID, true)
						.addField("Steam ID3", steamID3, true)
						.addField("Steam32 ID", steam32ID, true)
						.addField("Steam64 ID", steam64ID, true)
						.build();

					Util.msg(channel, embed);
				}
			}
			catch (HttpStatusException e)
			{
				Util.msg(channel, "Couldn't find a Steam profile with that given ID!");
			}
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "steam" };
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		return new CommandHelp[] { new CommandHelp("<steamid>", "Gives full information on a Steam account for the given input") };
	}
}