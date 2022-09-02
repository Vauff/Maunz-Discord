package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Logger;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class Stop extends AbstractCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User user) throws Exception
	{
		event.editReply("Maunz is stopping...").block();
		Logger.log.info("Maunz is stopping...");
		System.exit(0);
	}

	@Override
	public ApplicationCommandRequest getCommand()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Shuts down the bot")
			.build();
	}

	@Override
	public String getName()
	{
		return "stop";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.BOT_ADMIN;
	}
}