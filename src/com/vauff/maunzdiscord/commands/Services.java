package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Services extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		Util.msg(event.getChannel(), "This feature has not been implemented yet, please contact Vauff directly for enabling a service");
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*services" };
	}
}