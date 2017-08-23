package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Trello extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		Util.msg(event.getChannel(), "My Trello board is located at https://trello.com/b/9W7PmTvX/maunz");
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*trello" };
	}
}