package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class Stop extends AbstractCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, MessageChannel channel, User user) throws Exception
	{
		Main.shutdownState = Main.ShutdownState.SHUTDOWN_QUEUED;
		Logger.log.info("Bot shutdown has been queued, please wait...");
		Util.editReply(event, "Bot shutdown has been queued, please wait...");

		while (Main.shutdownState != Main.ShutdownState.SHUTDOWN_SAFE)
			Thread.sleep(1000);

		Logger.log.info("Maunz is shutting down...");
		Util.editReply(event, "Maunz is shutting down...");
		System.exit(0);
	}

	@Override
	public ApplicationCommandRequest getCommandRequest()
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