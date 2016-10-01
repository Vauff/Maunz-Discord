package com.vauff.maunzdiscord.core;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

public interface ICommand<M extends MessageReceivedEvent>
{
	public void exe(M event) throws Exception;

	public String[] getAliases();
}
