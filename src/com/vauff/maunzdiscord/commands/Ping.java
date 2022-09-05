package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class Ping extends AbstractCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User user) throws Exception
	{
		Util.editReply(event, "Pong!");
	}

	@Override
	public ApplicationCommandRequest getCommand()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Makes Maunz respond with pong")
			.build();
	}

	@Override
	public String getName()
	{
		return "ping";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}
