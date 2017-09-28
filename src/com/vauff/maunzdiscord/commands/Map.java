package com.vauff.maunzdiscord.commands;

import java.io.File;
import java.net.URL;

import org.json.JSONObject;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

public class Map extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (!event.getChannel().isPrivate())
		{
			String guildID = event.getGuild().getStringID();
			File file = new File(Util.getJarLocation() + "services/map-tracking/" + guildID + "/serverInfo.json");

			if (file.exists())
			{
				JSONObject json = new JSONObject(Util.getFileContents("services/map-tracking/" + guildID + "/serverInfo.json"));

				EmbedObject embed = new EmbedBuilder().withColor(Util.averageColorFromURL(new URL("http://158.69.59.239/mapimgs/" + json.getString("lastMap") + ".jpg"))).withTimestamp(json.getLong("timestamp")).withThumbnail("http://158.69.59.239/mapimgs/" + json.getString("lastMap") + ".jpg").withDescription("Currently Playing: **" + json.getString("lastMap").replace("_", "\\_") + "**\nPlayers Online: **" + json.getString("players") + "**\nQuick Join: **steam://connect/" + json.getString("serverIP") + ":" + json.getInt("serverPort") + "**").build();
				Util.msg(event.getChannel(), embed);
			}
			else
			{
				Util.msg(event.getChannel(), "The map tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set it up");
			}
		}
		else
		{
			Util.msg(event.getChannel(), "This command can't be done in a PM, only in a guild with the map tracking service enabled");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*map" };
	}
}