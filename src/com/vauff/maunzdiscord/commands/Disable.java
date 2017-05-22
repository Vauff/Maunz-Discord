package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Disable implements ICommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (Util.hasPermission(event.getMessage().getAuthor()))
		{
			if (Util.isEnabled)
			{
				Util.msg(event.getMessage().getChannel(), "Maunz is now disabled");
				Main.log.info("Maunz is now disabled, messages will no longer be parsed for commands");
				Util.isEnabled = false;
			}
			else
			{
				Util.msg(event.getMessage().getChannel(), "You silly, I was already disabled!");
			}
		}
		else
		{
			Util.msg(event.getMessage().getChannel(), "You do not have permission to use that command");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*disable" };
	}
}