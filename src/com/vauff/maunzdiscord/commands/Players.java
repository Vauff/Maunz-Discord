package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.features.GFLTimer;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Players implements ICommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String playersHtml = GFLTimer.playersDoc.select("span[class=ipsGrid_span4 cGFLInfo]").html();
		String[] playersHtmlSplit = playersHtml.split(System.lineSeparator());
		StringBuilder playerList = new StringBuilder();

		if (!event.getChannel().isPrivate())
		{
			Util.msg(event.getChannel(), "Sending the GFL ZE online player list to you in a PM!");
		}
		
		playerList.append("```-- Players Online: " + GFLTimer.players + " --" + System.lineSeparator() + System.lineSeparator());

		for (int i = 0; i < playersHtmlSplit.length; i += 3)
		{
			if (!playersHtmlSplit[i].equals(""))
			{
				playerList.append("- " + playersHtmlSplit[i] + System.lineSeparator());
			}
		}

		playerList.append("```");
		Util.msg(event.getAuthor().getOrCreatePMChannel(), playerList.toString());
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*players" };
	}
}