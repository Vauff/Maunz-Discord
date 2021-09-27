package com.vauff.maunzdiscord.commands.legacy;

import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.objects.CommandHelp;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Consumer;

public class Minecraft extends AbstractLegacyCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length == 1)
		{
			Util.msg(channel, author, "You need to provide a username! **Usage: " + Main.prefix + "minecraft <uuid/username>**");
		}
		else
		{
			BufferedReader uuidReader = new BufferedReader(new InputStreamReader(new URL("http://axis.iaero.me/uuidapi?uuid=" + args[1] + "&format=plain").openStream()));
			String uuidStatus = uuidReader.readLine();
			String username;

			if (uuidStatus.equals("") || uuidStatus.equals("Please input a valid 32-character UUID. (You may use Java format or plain text format, the 32 char limit does not count \"-\")") || uuidStatus.equals("Please input a valid format: (plain, json, xml)"))
			{
				username = args[1];
			}
			else
			{
				username = uuidStatus;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://axis.iaero.me/accinfo?username=" + username + "&format=csv").openStream()));
			String statusRaw = reader.readLine();
			String[] status = statusRaw.split(",");

			if (username.contains("#") || username.contains("&"))
			{
				Util.msg(channel, author, "The Minecraft account name **" + username + "** must be alphanumerical or contain an underscore.");
			}
			else
			{
				if (statusRaw.equalsIgnoreCase("unknown username"))
				{
					Util.msg(channel, author, "The Minecraft account name **" + username + "** is free and does not belong to any account!");
				}

				else if (statusRaw.equalsIgnoreCase("Username must be 16 characters or less."))
				{
					Util.msg(channel, author, "The Minecraft account name **" + username + "** must be 16 characters or less.");
				}

				else if (statusRaw.equalsIgnoreCase("Username must be alphanumerical (or contain '_')."))
				{
					Util.msg(channel, author, "The Minecraft account name **" + username + "** must be alphanumerical or contain an underscore.");
				}

				else if (statusRaw.contains(","))
				{
					String uuid = status[0];

					uuid = new StringBuilder(uuid).insert(uuid.length() - 24, "-").toString();
					uuid = new StringBuilder(uuid).insert(uuid.length() - 20, "-").toString();
					uuid = new StringBuilder(uuid).insert(uuid.length() - 16, "-").toString();
					uuid = new StringBuilder(uuid).insert(uuid.length() - 12, "-").toString();

					String headURL = "https://cravatar.eu/helmavatar/" + status[1] + "/120";
					URL constructedURL = new URL(headURL);

					EmbedCreateSpec embed = EmbedCreateSpec.builder()
						.color(Util.averageColorFromURL(constructedURL, true))
						.thumbnail(headURL)
						.footer("Powered by axis.iaero.me", "https://i.imgur.com/4o6K42Z.png")
						.addField("Name", status[1], true)
						.addField("Account Status", "Premium", true)
						.addField("Migrated", StringUtils.capitalize(status[2]), true)
						.addField("UUID", uuid, true)
						.addField("Skin", "https://minotar.net/body/" + status[1] + "/500.png", true)
						.addField("Raw Skin", "https://minotar.net/skin/" + status[1], true)
						.build();

					Util.msg(channel, author, embed);
				}
			}
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "minecraft" };
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		return new CommandHelp[] { new CommandHelp("<uuid/username>", "Gives you full information about any Minecraft account.") };
	}
}
