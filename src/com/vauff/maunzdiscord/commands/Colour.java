package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;

public class Colour extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");
		String url = "";

		if (args.length == 1)
		{
			for (IMessage.Attachment attachment : event.getMessage().getAttachments())
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
					Util.msg(event.getChannel(), event.getAuthor(), "Could not get an image from the provided attachment or link!");
					return;
				}
			}
			catch (MalformedURLException e)
			{
				Util.msg(event.getChannel(), event.getAuthor(), "Could not get an image from the provided attachment or link!");
				return;
			}

			EmbedObject embed = new EmbedBuilder().withColor(color).withThumbnail(url).withTitle("Average Image Colour").appendField("RGB", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue(), true).appendField("HTML/Hex", String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()), true).build();
			Util.msg(event.getChannel(), event.getAuthor(), embed);
		}
		else
		{
			Util.msg(event.getChannel(), event.getAuthor(), "Could not get an image from the provided attachment or link!");
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