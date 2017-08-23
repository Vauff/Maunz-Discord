package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Stop extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (Util.hasPermission(event.getMessage().getAuthor()))
		{
			Main.log.info("Maunz is stopping...");
			Main.client.logout();
			System.exit(0);
		}
		else
		{
			Util.msg(event.getChannel(), "You do not have permission to use that command");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*stop" };
	}
}