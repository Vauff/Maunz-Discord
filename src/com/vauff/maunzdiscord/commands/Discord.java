package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Discord extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		Util.msg(event.getChannel(), event.getAuthor(), "Bot invite link: <https://discordapp.com/oauth2/authorize?&client_id=230780946142593025&scope=bot>" + System.lineSeparator() + "Maunz Hub server invite link: https://discord.gg/v55fW9b");
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {
				"*discord",
				"*invite"
		};
	}
}