package com.vauff.maunzdiscord.commands;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.net.URL;

public class Steam extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length == 1)
		{
			Util.msg(event.getChannel(), event.getAuthor(), "You need to provide a Steam ID! **Usage: *steam <steamid>**");
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
					Util.msg(event.getChannel(), event.getAuthor(), "Couldn't find a Steam profile with that given ID!");
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

					Util.msg(event.getChannel(), event.getAuthor(), new EmbedBuilder().withColor(Util.averageColorFromURL(new URL(avatarURL))).withThumbnail(avatarURL).withFooterIcon("https://i.imgur.com/GuXJIeX.png").withTitle(nickname).withUrl(link).withFooterText("Powered by steamid.xyz").appendField("Name", nickname, true).appendField("Real Name", realName, true).appendField("Country", country, true).appendField("Account Created", creationDate, true).appendField("Last Logoff", lastLogoff, true).appendField("Status", status, true).appendField("Profile Visibility", visibility, true).appendField("Bans", bans, true).appendField("Steam ID", steamID, true).appendField("Steam ID3", steamID3, true).appendField("Steam32 ID", steam32ID, true).appendField("Steam64 ID", steam64ID, true).build());
				}
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				Util.msg(event.getChannel(), event.getAuthor(), "An unknown error has occured");
			}
			catch (HttpStatusException e)
			{
				Util.msg(event.getChannel(), event.getAuthor(), "Couldn't find a Steam profile with that given ID!");
			}
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*steam" };
	}
}