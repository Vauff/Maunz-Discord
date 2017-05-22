package com.vauff.maunzdiscord.commands;

import java.awt.Color;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.features.GFLTimer;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

public class Map implements ICommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (!Util.getFileContents("lastmap.txt").endsWith("_OLD-DATA"))
		{
			String map = Util.getFileContents("lastmap.txt");
			EmbedObject embed = new EmbedBuilder().withColor(new Color(0, 154, 255)).withTimestamp(GFLTimer.timestamp).withThumbnail("https://vauff.me/mapimgs/" + map + ".jpg").withDescription("Currently Playing: **" + map.replace("_", "\\_") + "**\nPlayers Online: **" + GFLTimer.players + "**\nQuick Join: **steam://connect/216.52.148.47:27015**").build();
			Util.msg(event.getMessage().getChannel(), embed);
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