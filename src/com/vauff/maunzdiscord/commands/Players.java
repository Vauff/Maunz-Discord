package com.vauff.maunzdiscord.commands;

import java.io.File;

import org.json.JSONObject;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.features.ServerTimer;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;

public class Players extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		StringBuilder playersList = new StringBuilder();

		if (!event.getChannel().isPrivate())
		{
			String guildID = event.getGuild().getStringID();
			File file = new File(Util.getJarLocation() + "data/services/server-tracking/" + guildID + "/serverInfo.json");

			if (file.exists())
			{
				JSONObject json = new JSONObject(Util.getFileContents("data/services/server-tracking/" + guildID + "/serverInfo.json"));

				if (json.getBoolean("enabled"))
				{
					if (!(json.getInt("downtimeTimer") >= 3))
					{
						if (ServerTimer.serverPlayers.containsKey(json.getString("serverIP") + ":" + json.getInt("serverPort")))
						{
							boolean sizeIsSmall = ServerTimer.serverPlayers.get(json.getString("serverIP") + ":" + json.getInt("serverPort")).size() <= 8;

							playersList.append("```-- Players Online: " + json.getString("players") + " --" + System.lineSeparator() + System.lineSeparator());

							for (String player : ServerTimer.serverPlayers.get(json.getString("serverIP") + ":" + json.getInt("serverPort")))
							{
								if (!player.equals(""))
								{
									playersList.append("- " + player + System.lineSeparator());
								}
							}

							playersList.append("```");

							try
							{
								(!sizeIsSmall ? event.getAuthor().getOrCreatePMChannel() : event.getChannel()).sendMessage(playersList.toString());

								if (!sizeIsSmall)
								{
									Util.msg(event.getChannel(), event.getAuthor(), "Sending the online player list to you in a PM!");
								}
							}
							catch (DiscordException e)
							{
								if (!sizeIsSmall)
								{
									Util.msg(event.getChannel(), event.getAuthor(), "An error occured when trying to PM you the players list, make sure you don't have private messages disabled in any capacity or the bot blocked");
								}
							}
						}
						else
						{
							Util.msg(event.getChannel(), event.getAuthor(), "There doesn't appear to be any player info cached yet (was the bot just started or the service just added?), please wait a moment before trying again");
						}
					}
					else
					{
						Util.msg(event.getChannel(), event.getAuthor(), "The server currently appears to be offline");
					}
				}
				else
				{
					Util.msg(event.getChannel(), event.getAuthor(), "The server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set it up");
				}
			}
			else
			{
				Util.msg(event.getChannel(), event.getAuthor(), "The server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set it up");
			}
		}
		else
		{
			Util.msg(event.getChannel(), event.getAuthor(), "This command can't be done in a PM, only in a guild with the server tracking service enabled");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*players" };
	}
}