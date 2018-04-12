package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class Minecraft extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length == 1)
		{
			Util.msg(event.getChannel(), event.getAuthor(), "You need to provide a username! **Usage: *minecraft <uuid/username>**");
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
				Util.msg(event.getChannel(), event.getAuthor(), "The Minecraft account name **" + username + "** must be alphanumerical or contain an underscore.");
			}
			else
			{
				if (statusRaw.equalsIgnoreCase("unknown username"))
				{
					Util.msg(event.getChannel(), event.getAuthor(), "The Minecraft account name **" + username + "** is free and does not belong to any account!");
				}

				else if (statusRaw.equalsIgnoreCase("Username must be 16 characters or less."))
				{
					Util.msg(event.getChannel(), event.getAuthor(), "The Minecraft account name **" + username + "** must be 16 characters or less.");
				}

				else if (statusRaw.equalsIgnoreCase("Username must be alphanumerical (or contain '_')."))
				{
					Util.msg(event.getChannel(), event.getAuthor(), "The Minecraft account name **" + username + "** must be alphanumerical or contain an underscore.");
				}

				else if (statusRaw.contains(","))
				{
					String uuid = status[0];

					uuid = new StringBuilder(uuid).insert(uuid.length() - 24, "-").toString();
					uuid = new StringBuilder(uuid).insert(uuid.length() - 20, "-").toString();
					uuid = new StringBuilder(uuid).insert(uuid.length() - 16, "-").toString();
					uuid = new StringBuilder(uuid).insert(uuid.length() - 12, "-").toString();

					String headURL = "https://cravatar.eu/helmavatar/" + status[1] + "/120";
					Util.msg(event.getChannel(), event.getAuthor(), new EmbedBuilder().withColor(Util.averageColorFromURL(new URL(headURL))).withThumbnail(headURL).withFooterIcon("https://i.imgur.com/4o6K42Z.png").appendField("Name", status[1], true).withFooterText("Powered by axis.iaero.me").appendField("Account Status", "Premium", true).appendField("Migrated", StringUtils.capitalize(status[2]), true).appendField("UUID", uuid, true).appendField("Skin", "https://minotar.net/body/" + status[1] + "/500.png", true).appendField("Raw Skin", "https://minotar.net/skin/" + status[1], true).build());
				}
			}

			if (args[0].equalsIgnoreCase("*accinfo"))
			{
				Util.msg(event.getChannel(), event.getAuthor(), "**\\*accinfo** has been deprecated in favour of **\\*minecraft**, please make sure to use that in the future instead");
			}
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {
				"*minecraft",
				"*accinfo"
		};
	}
}
