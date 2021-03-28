package com.vauff.maunzdiscord.commands.legacy;

import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.commands.templates.CommandHelp;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

public class Services extends AbstractLegacyCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		Util.msg(channel, author, "This command has been moved to a slash command available as **/services**");
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "services" };
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.GUILD_ADMIN;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		return new CommandHelp[] { new CommandHelp("", "Opens an interface for enabling specific services on a guild.") };
	}
}