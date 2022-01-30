package com.vauff.maunzdiscord.commands.legacy;

import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.objects.CommandHelp;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.net.MalformedURLException;
import java.net.URL;

public class Colour extends AbstractLegacyCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");
		String url = "";

		if (args.length == 1)
		{
			for (Attachment attachment : event.getMessage().getAttachments())
			{
				if (attachment.getUrl().endsWith(".png") || attachment.getUrl().endsWith(".jpg") || attachment.getUrl().endsWith(".jpeg"))
				{
					url = attachment.getUrl();
				}
			}
		}
		else
		{
			url = args[1];
		}

		if (!url.equals(""))
		{
			Color color;

			try
			{
				color = Util.averageColorFromURL(new URL(url), false);

				if (color == null)
				{
					Util.msg(channel, "Could not get an image from the provided attachment or link!");
					return;
				}
			}
			catch (MalformedURLException e)
			{
				Util.msg(channel, "Could not get an image from the provided attachment or link!");
				return;
			}

			EmbedCreateSpec embed = EmbedCreateSpec.builder()
				.color(color)
				.thumbnail(url)
				.title("Average Image Colour")
				.addField("RGB", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue(), true)
				.addField("HTML/Hex", String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()), true)
				.build();

			Util.msg(channel, embed);
		}
		else
		{
			Util.msg(channel, "Could not get an image from the provided attachment or link!");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {
			"colour",
			"color"
		};
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		return new CommandHelp[] { new CommandHelp("[link]", "Returns the average RGB and HTML/Hex colour codes of an attachment or image link you specify") };
	}
}