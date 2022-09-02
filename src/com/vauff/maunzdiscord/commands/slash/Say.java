package com.vauff.maunzdiscord.commands.slash;

import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.Arrays;

public class Say extends AbstractSlashCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User user) throws Exception
	{
		ApplicationCommandInteraction interaction = event.getInteraction().getCommandInteraction().get();
		String message = interaction.getOption("message").get().getValue().get().asString();

		if (channel instanceof PrivateChannel)
		{
			event.editReply("This command can't be done in a PM, only in a guild that you have admin permissions in").block();
			return;
		}

		if (interaction.getOption("channel").isPresent())
		{
			MessageChannel sendChannel = (MessageChannel) interaction.getOption("channel").get().getValue().get().asChannel().block();

			if (sendChannel.equals(channel))
			{
				event.editReply(message).block();
			}
			else
			{
				if (Util.msg(sendChannel, true, message) != null)
					event.editReply("Successfully sent message!").block();
				else
					event.editReply("Failed to send message, the bot doesn't have permissions for that channel!").block();
			}
		}
		else
		{
			event.editReply(message).block();
		}
	}

	@Override
	public ApplicationCommandRequest getCommand()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Sends a custom message from Maunz to any channel")
			.addOption(ApplicationCommandOptionData.builder()
				.name("message")
				.description("The message to send")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.required(true)
				.build())
			.addOption(ApplicationCommandOptionData.builder()
				.name("channel")
				.description("The channel to send the message to")
				.type(ApplicationCommandOption.Type.CHANNEL.getValue())
				.channelTypes(Arrays.asList(Channel.Type.GUILD_TEXT.getValue(), Channel.Type.GUILD_NEWS.getValue()))
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "say";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.GUILD_ADMIN;
	}
}