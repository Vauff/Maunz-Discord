package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.CommandHelp;
import com.vauff.maunzdiscord.commands.templates.SubCommandHelp;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

public class Services extends AbstractCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		Util.msg(channel, author, "This command is temporarily unavailable until a future release" + System.lineSeparator() + System.lineSeparator() + "In the meantime, you can head over to the **#help** channel at https://discord.gg/v55fW9b to request manual service management (additions, edits, etc.). Please see the pinned message there for more details on how to proceed.");
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*services" };
	}

	@Override
	public int getPermissionLevel()
	{
		return 1;
	}

	@Override
	public CommandHelp getHelp()
	{
		return new CommandHelp(getAliases(), new SubCommandHelp[] { new SubCommandHelp("", "Opens an interface for enabling specific services on a guild.") });
	}
}