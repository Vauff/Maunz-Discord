package com.vauff.maunzdiscord.commands.legacy;

import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.objects.CommandHelp;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.ArrayList;

public class Restart extends AbstractLegacyCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		final ArrayList<String> command = new ArrayList<>();

		command.add("java");
		command.add("-jar");
		command.add("Maunz-Discord.jar");
		Util.msg(channel, author, "Maunz is restarting...");
		Logger.log.info("Maunz is restarting...");
		new ProcessBuilder(command).start();
		System.exit(0);
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "restart" };
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.BOT_ADMIN;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		return new CommandHelp[] { new CommandHelp("", "Restarts Maunz.") };
	}
}