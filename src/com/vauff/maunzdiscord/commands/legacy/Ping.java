package com.vauff.maunzdiscord.commands.legacy;

import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.commands.templates.CommandHelp;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

public class Ping extends AbstractLegacyCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		Util.msg(channel, author, "This command has been moved to a slash command available as **/ping**");
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "ping" };
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		return new CommandHelp[] { new CommandHelp("", "Makes Maunz respond to you with pong, useful for testing your connection.") };
	}
}