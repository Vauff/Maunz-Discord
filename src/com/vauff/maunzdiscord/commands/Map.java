package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

public class Map implements ICommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (!Util.getFileContents("lastmap.txt").equals(""))
		{
			Util.msg(event.getMessage().getChannel(), "GFL Zombie Escape is currently playing: **" + Util.getFileContents("lastmap.txt").replace("_", "\\_") + "**");
		}
		else
		{
			Util.msg(event.getMessage().getChannel(), "Failed to grab map data, maybe the server is down?");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*map" };
	}
}