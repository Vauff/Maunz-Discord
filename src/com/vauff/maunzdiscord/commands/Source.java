package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Source extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		Util.msg(event.getChannel(), event.getAuthor(), "My source is available at https://github.com/Vauff/Maunz-Discord");
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*source" };
	}
}