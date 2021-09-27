package com.vauff.maunzdiscord.commands.legacy;

import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.objects.CommandHelp;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

public class Stop extends AbstractLegacyCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		Util.msg(channel, author, "Maunz is stopping...");
		Logger.log.info("Maunz is stopping...");
		System.exit(0);
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "stop" };
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.BOT_ADMIN;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		return new CommandHelp[] { new CommandHelp("", "Stops Maunz.") };
	}
}