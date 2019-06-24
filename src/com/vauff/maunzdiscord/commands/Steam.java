package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.function.Consumer;

public class Steam extends AbstractCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().get().split(" ");

		if (args.length == 1)
		{
			Util.msg(channel, author, "You need to provide a Steam ID! **Usage: *steam <steamid>**");
		}
		else
		{
			try
			{
				Document site = Jsoup.connect("https://steamid.xyz/" + args[1]).get();
				String siteHtml = site.html();
				String siteText = site.text();

				if (siteText.contains("Player Not Found :( or hasn't set public profile Supported inputs"))
				{
					Util.msg(channel, author, "Couldn't find a Steam profile with that given ID!");
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
					final String finalRealName = realName;
					final String finalCountry = country;
					final String finalCreationDate = creationDate;
					final String finalStatus = status;
					final String finalBans = bans;

					Consumer<EmbedCreateSpec> embed = spec ->
					{
						spec.setColor(Util.averageColorFromURL(constructedURL, true));
						spec.setThumbnail(avatarURL);
						spec.setFooter("Powered by steamid.xyz", "https://i.imgur.com/GuXJIeX.png");
						spec.setTitle(nickname);
						spec.setUrl(link);
						spec.addField("Name", nickname, true);
						spec.addField("Real Name", finalRealName, true);
						spec.addField("Country", finalCountry, true);
						spec.addField("Account Created", finalCreationDate, true);
						spec.addField("Last Logoff", lastLogoff, true);
						spec.addField("Status", finalStatus, true);
						spec.addField("Profile Visibility", visibility, true);
						spec.addField("Bans", finalBans, true);
						spec.addField("Steam ID", steamID, true);
						spec.addField("Steam ID3", steamID3, true);
						spec.addField("Steam32 ID", steam32ID, true);
						spec.addField("Steam64 ID", steam64ID, true);
					};

					Util.msg(channel, author, embed);
				}
			}
			catch (HttpStatusException e)
			{
				Util.msg(channel, author, "Couldn't find a Steam profile with that given ID!");
			}
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*steam" };
	}
}