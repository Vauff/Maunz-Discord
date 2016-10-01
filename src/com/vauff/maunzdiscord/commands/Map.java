package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.features.GFLTimer;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.MessageBuilder;

public class Map implements ICommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		new MessageBuilder(Main.client).withChannel(event.getMessage().getChannel()).withContent("GFL Zombie Escape is currently playing: **" + GFLTimer.lastMap + "**").build();
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*map" };
	}
}