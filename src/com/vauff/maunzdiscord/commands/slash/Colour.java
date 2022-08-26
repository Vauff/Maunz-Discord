package com.vauff.maunzdiscord.commands.slash;

import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;

import java.net.MalformedURLException;
import java.net.URL;

public class Colour extends AbstractSlashCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User user) throws Exception
	{
		ApplicationCommandInteraction interaction = event.getInteraction().getCommandInteraction().get();
		String url = "";

		if (interaction.getOption("image").isPresent())
		{
			for (Attachment attachment : interaction.getResolved().get().getAttachments().values())
			{
				if (attachment.getUrl().endsWith(".png") || attachment.getUrl().endsWith(".jpg") || attachment.getUrl().endsWith(".jpeg"))
				{
					url = attachment.getUrl();
				}
			}
		}
		else if (interaction.getOption("link").isPresent())
		{
			url = interaction.getOption("link").get().getValue().get().asString();
		}
		else
		{
			event.editReply("You need to provide an image attachment or link!").block();
			return;
		}

		if (!url.equals(""))
		{
			Color color;

			try
			{
				color = Util.averageColorFromURL(new URL(url), false);

				if (color == null)
				{
					event.editReply("Could not get an image from the provided attachment or link!").block();
					return;
				}
			}
			catch (MalformedURLException e)
			{
				event.editReply("Could not get an image from the provided attachment or link!").block();
				return;
			}

			EmbedCreateSpec embed = EmbedCreateSpec.builder()
				.color(color)
				.thumbnail(url)
				.title("Average Image Colour")
				.addField("RGB", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue(), true)
				.addField("HTML/Hex", String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()), true)
				.build();

			event.editReply().withEmbeds(embed).block();
		}
		else
		{
			event.editReply("Could not get an image from the provided attachment or link!").block();
		}
	}

	@Override
	public ApplicationCommandRequest getCommand()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Returns the average RGB and HTML/Hex colour codes of an image attachment or link")
			.addOption(ApplicationCommandOptionData.builder()
				.name("image")
				.description("Image attachment")
				.type(ApplicationCommandOption.Type.ATTACHMENT.getValue())
				.required(false)
				.build())
			.addOption(ApplicationCommandOptionData.builder()
				.name("link")
				.description("Image link")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.required(false)
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "colour";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}