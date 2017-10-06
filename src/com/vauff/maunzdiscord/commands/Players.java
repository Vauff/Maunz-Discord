package com.vauff.maunzdiscord.commands;

import java.io.File;

import org.json.JSONObject;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.features.MapTimer;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Players extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		StringBuilder playersList = new StringBuilder();

		if (!event.getChannel().isPrivate())
		{
			String guildID = event.getGuild().getStringID();
			File file = new File(Util.getJarLocation() + "services/map-tracking/" + guildID + "/serverInfo.json");

			if (file.exists())
			{
				JSONObject json = new JSONObject(Util.getFileContents("services/map-tracking/" + guildID + "/serverInfo.json"));

				Util.msg(event.getChannel(), "Sending the online player list to you in a PM!");
				playersList.append("```-- Players Online: " + json.getString("players") + " --" + System.lineSeparator() + System.lineSeparator());

				for (String player : MapTimer.serverPlayers.get(event.getGuild().getLongID()))
				{
					if (!player.equals(""))
					{
						playersList.append("- " + player + System.lineSeparator());
					}
				}

				playersList.append("```");
				Util.msg(event.getAuthor().getOrCreatePMChannel(), playersList.toString());
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
		return new String[] { "*players" };
	}
}