package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

public class Colour extends AbstractCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().get().split(" ");
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
					Util.msg(channel, author, "Could not get an image from the provided attachment or link!");
					return;
				}
			}
			catch (MalformedURLException e)
			{
				Util.msg(channel, author, "Could not get an image from the provided attachment or link!");
				return;
			}

			final String finalUrl = url;

			Consumer<EmbedCreateSpec> embed = spec ->
			{
				spec.setColor(color);
				spec.setThumbnail(finalUrl);
				spec.setTitle("Average Image Colour");
				spec.addField("RGB", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue(), true);
				spec.addField("HTML/Hex", String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()), true);
			};

			Util.msg(channel, author, embed);
		}
		else
		{
			Util.msg(channel, author, "Could not get an image from the provided attachment or link!");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {
				"*colour",
				"*color"
		};
	}
}