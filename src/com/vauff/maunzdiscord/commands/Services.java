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
		if (Util.hasPermission(author, event.getGuild().block()))
		{
			Util.msg(channel, author, "This command is temporarily unavailable until a future release" + System.lineSeparator() + System.lineSeparator() + "In the meantime, you can head over to the **#help** channel at https://discord.gg/v55fW9b to request manual service management (additions, edits, etc.). Please see the pinned message there for more details on how to proceed.");
		}
		else
		{
			Util.msg(channel, author, "You do not have permission to use that command");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*services" };
	}

	@Override
	public CommandHelp getHelp()
	{
		return new CommandHelp(getAliases(), new SubCommandHelp[] { new SubCommandHelp("", "Opens an interface for enabling specific services on a guild.") }, 1);
	}
}