package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.io.File;
import java.net.URL;

public class Map extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (!event.getChannel().isPrivate())
		{
			String guildID = event.getGuild().getStringID();
			File file = new File(Util.getJarLocation() + "data/services/server-tracking/" + guildID + "/serverInfo.json");

			if (file.exists())
			{
				JSONObject json = new JSONObject(Util.getFileContents("data/services/server-tracking/" + guildID + "/serverInfo.json"));

				if (json.getBoolean("enabled"))
				{
					if (!(json.getInt("downtimeTimer") >= json.getInt("failedConnectionsThreshold")))
					{
						if (!json.getString("lastMap").equals("N/A"))
						{
							String url = "http://158.69.59.239/mapimgs/" + json.getString("lastMap") + ".jpg";

							try
							{
								Jsoup.connect(url).get();
							}
							catch (HttpStatusException e)
							{
								url = "https://image.gametracker.com/images/maps/160x120/csgo/" + json.getString("lastMap") + ".jpg";
							}
							catch (UnsupportedMimeTypeException e)
							{
								// This is to be expected normally because JSoup can't parse a URL serving only a static image
							}

							EmbedObject embed = new EmbedBuilder().withColor(Util.averageColorFromURL(new URL(url))).withTimestamp(json.getLong("timestamp")).withThumbnail(url).withDescription("Currently Playing: **" + json.getString("lastMap").replace("_", "\\_") + "**\nPlayers Online: **" + json.getString("players") + "**\nQuick Join: **steam://connect/" + json.getString("serverIP") + ":" + json.getInt("serverPort") + "**").build();
							Util.msg(event.getChannel(), event.getAuthor(), embed);
						}
						else
						{
							Util.msg(event.getChannel(), event.getAuthor(), "There doesn't appear to be any server info cached yet (was the service just added?), please wait a moment before trying again");
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
		return new String[] { "*map" };
	}
}