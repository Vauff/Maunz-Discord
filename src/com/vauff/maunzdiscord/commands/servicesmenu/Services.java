package com.vauff.maunzdiscord.commands.servicesmenu;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Services extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (Util.hasPermission(event.getAuthor(), event.getGuild()))
		{
			new StartPage(event, this).show();
		}
		else
		{
			Util.msg(event.getChannel(), "You do not have permission to use that command");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*services" };
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) throws Exception
	{
		if(AbstractMenuPage.ACTIVE.containsKey(event.getAuthor().getLongID()))
		{
			AbstractMenuPage.ACTIVE.get(event.getAuthor().getLongID()).onReplied(event);
		}
	}
}