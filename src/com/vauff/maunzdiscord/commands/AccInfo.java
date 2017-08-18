package com.vauff.maunzdiscord.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

public class AccInfo extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		try
		{
			String[] args = event.getMessage().getContent().split(" ");

			if (args.length == 1)
			{
				Util.msg(event.getChannel(), "Provide a Minecraft username for me please!");
			}
			else
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://axis.iaero.me/accinfo?username=" + args[1] + "&format=csv").openStream()));
				String statusRaw = reader.readLine();
				String[] status = statusRaw.split(",");

				if (args[1].contains("#") || args[1].contains("&"))
				{
					Util.msg(event.getChannel(), "The Minecraft account name " + args[1] + " must be alphanumerical or contain an underscore.");
				}
				else
				{
					if (statusRaw.equalsIgnoreCase("unknown username"))
					{
						Util.msg(event.getChannel(), "The Minecraft account name " + args[1] + " is free and does not belong to any account!");
					}

					else if (statusRaw.equalsIgnoreCase("Username must be 16 characters or less."))
					{
						Util.msg(event.getChannel(), "The Minecraft account name " + args[1] + " must be 16 characters or less.");
					}

					else if (statusRaw.equalsIgnoreCase("Username must be alphanumerical (or contain '_')."))
					{
						Util.msg(event.getChannel(), "The Minecraft account name " + args[1] + " must be alphanumerical or contain an underscore.");
					}

					else if (statusRaw.contains(","))
					{
						String uuid = status[0];
						
						uuid = new StringBuilder(uuid).insert(uuid.length() - 24, "-").toString();
						uuid = new StringBuilder(uuid).insert(uuid.length() - 20, "-").toString();
						uuid = new StringBuilder(uuid).insert(uuid.length() - 16, "-").toString();
						uuid = new StringBuilder(uuid).insert(uuid.length() - 12, "-").toString();

						String headURL = "http://cravatar.eu/helmavatar/" + status[1] +"/120";
						EmbedObject embed = new EmbedBuilder().withColor(Util.averageColorFromURL(new URL(headURL))).withThumbnail(headURL).appendField("Name", status[1], true).withFooterText("Powered by axis.iaero.me").appendField("Account Status", "Premium", true).appendField("Migrated", StringUtils.capitalize(status[2]), true).appendField("UUID", uuid, true).appendField("Skin", "https://minotar.net/body/" + status[1] + "/500.png", true).appendField("Raw Skin", "https://minotar.net/skin/" + status[1], true).build();
						Util.msg(event.getChannel(), embed);
					}
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			Util.msg(event.getChannel(), "An unknown error occured grabbing account information");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*accinfo" };
	}
}
