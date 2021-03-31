package com.vauff.maunzdiscord.commands.slash;

import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class Ping extends AbstractSlashCommand<Interaction>
{
	@Override
	public String exe(Interaction interaction, MessageChannel channel, User author) throws Exception
	{
		return "Pong!";
	}

	@Override
	public ApplicationCommandRequest getCommand()
	{
		return ApplicationCommandRequest.builder()
				.name(getName())
				.description("Make Maunz respond with pong")
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
