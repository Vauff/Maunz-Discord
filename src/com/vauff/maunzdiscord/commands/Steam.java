package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Steam extends AbstractCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User user) throws Exception
	{
		try
		{
			String argument = event.getInteraction().getCommandInteraction().get().getOption("steamid").get().getValue().get().asString();
			String url = "https://steamid.xyz/" + argument.replace("\n", "").replace("\r", "");
			Document site = Jsoup.connect(url).get();
			String siteHtml = site.html();
			String siteText = site.text();

			if (siteText.contains("Player Not Found :( or hasn't set public profile Supported inputs") || siteText.contains("Steam Id Finder - is the simplest way to find steam id online by anything you know:"))
			{
				event.editReply("Couldn't find a Steam profile with that given ID!").block();
			}
			else
			{
				String realName = "N/A";
				String country = "N/A";
				String creationDate = "N/A";
				String avatarURL = siteHtml.split("<img class=\"avatar\" src=\"")[1].split("\"")[0];
				String nickname = siteHtml.split("Nick Name <input type=\"text\" onclick=\"this.select\\(\\);\" value=\"")[1].split("\"")[0];
				String lastLogoff = siteHtml.split("<i>Last Logoff:</i> ")[1].split("<br>")[0];
				String status = siteHtml.split("<i>Status:</i> ")[1].split("<br>")[0];
				String visibility = siteHtml.split("<i>Visibility:</i> ")[1].split("<br>")[0];
				String bans = "";
				String steamID = siteHtml.split("Steam ID <input type=\"text\" onclick=\"this.select\\(\\);\" value=\"")[1].split("\"")[0];
				String steamID3 = siteHtml.split("Steam ID3 <input type=\"text\" onclick=\"this.select\\(\\);\" value=\"")[1].split("\"")[0];
				String steam32ID = siteHtml.split("Steam32 ID <input type=\"text\" onclick=\"this.select\\(\\);\" value=\"")[1].split("\"")[0];
				String steam64ID = siteHtml.split("Steam64 ID <input type=\"text\" onclick=\"this.select\\(\\);\" value=\"")[1].split("\"")[0];
				String link = siteHtml.split("Profile URL <input type=\"text\" onclick=\"this.select\\(\\);\" value=\"")[1].split("\"")[0];

				if (!siteHtml.split("<i>Real Name:</i> ")[1].split("<br>")[0].equalsIgnoreCase(""))
					realName = siteHtml.split("<i>Real Name:</i> ")[1].split("<br>")[0];

				if (!siteHtml.split("<i>Country:</i> ")[1].split("<br>")[0].equalsIgnoreCase(""))
					country = ":flag_" + siteHtml.split("<i>Country:</i> ")[1].split("<br>")[0].toLowerCase() + ":";

				if (!siteHtml.split("<i>Account Created:</i> ")[1].split("<br>")[0].equalsIgnoreCase(""))
					creationDate = siteHtml.split("<i>Account Created:</i> ")[1].split("<br>")[0];

				if (visibility.equalsIgnoreCase("private"))
					status = "N/A";

				if (siteHtml.contains("\">X VAC</em>"))
					bans += "VAC, ";

				if (siteHtml.contains("\">X Trade</em>"))
					bans += "Trade, ";

				if (siteHtml.contains("\">X Community</em>"))
					bans += "Community";

				if (bans.equalsIgnoreCase(""))
					bans = "None";

				if (bans.substring(bans.length() - 2).equalsIgnoreCase(", "))
					bans = bans.substring(0, bans.length() - 2);

				if (lastLogoff.equalsIgnoreCase(""))
					lastLogoff = "N/A";

				EmbedCreateSpec embed = EmbedCreateSpec.builder()
					.color(Util.averageColorFromURL(avatarURL, true))
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

				event.editReply().withEmbeds(embed).block();
			}
		}
		catch (HttpStatusException e)
		{
			event.editReply("Couldn't find a Steam profile with that given ID!").block();
		}
	}

	@Override
	public ApplicationCommandRequest getCommand()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Gives you full information about a Steam account")
			.addOption(ApplicationCommandOptionData.builder()
				.name("steamid")
				.description("Any format of Steam ID or profile URL")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.required(true)
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "steam";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}