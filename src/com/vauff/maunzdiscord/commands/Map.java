package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Map extends AbstractCommand<MessageReceivedEvent>
{
	private static HashMap<String, List<String>> selectionServers = new HashMap<>();
	private static HashMap<String, String> selectionMessages = new HashMap<>();
	private static HashMap<String, String[]> args = new HashMap<>();

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
				int serverNumber = 0;
				List<String> serverList = new ArrayList<>();

				while (true)
				{
					JSONObject object;

					try
					{
						object = json.getJSONObject("server" + serverNumber);
					}
					catch (JSONException e)
					{
						break;
					}

					if (object.getBoolean("enabled"))
					{
						serverList.add("server" + serverNumber);
					}

					serverNumber++;
				}

				if (serverList.size() != 0)
				{
					if (serverList.size() == 1)
					{
						runCmd(event.getAuthor(), event.getChannel(), json.getJSONObject(serverList.get(0)), event.getMessage().getContent().split(" "), false);
					}
					else
					{
						String object = "";

						for (String objectName : serverList)
						{
							if (json.getJSONObject(objectName).getLong("serverTrackingChannelID") == event.getChannel().getLongID())
							{
								object = objectName;
								break;
							}
						}

						if (!object.equals(""))
						{
							runCmd(event.getAuthor(), event.getChannel(), json.getJSONObject(object), event.getMessage().getContent().split(" "), false);
						}
						else
						{
							String msg = "Please select which server to view the map for" + System.lineSeparator();
							int i = 1;

							for (String serverObject : serverList)
							{
								msg += System.lineSeparator() + "**`[" + i + "]`**  |  " + json.getJSONObject(serverObject).getString("serverName");
								i++;
							}

							IMessage m = Util.msg(event.getChannel(), event.getAuthor(), msg);
							waitForReaction(m.getStringID(), event.getAuthor().getStringID());
							selectionServers.put(event.getAuthor().getStringID(), serverList);
							selectionMessages.put(event.getAuthor().getStringID(), m.getStringID());
							args.put(event.getAuthor().getStringID(), event.getMessage().getContent().split(" "));
							Util.addNumberedReactions(m, true, serverList.size());

							ScheduledExecutorService msgDeleterPool = Executors.newScheduledThreadPool(1);

							msgDeleterPool.schedule(() ->
							{
								if (!m.isDeleted())
								{
									m.delete();
									selectionServers.remove(event.getAuthor().getStringID());
									selectionMessages.remove(event.getAuthor().getStringID());
								}

								msgDeleterPool.shutdown();
							}, 120, TimeUnit.SECONDS);
						}
					}
				}
				else
				{
					Util.msg(event.getChannel(), event.getAuthor(), "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
				}
			}
			else
			{
				Util.msg(event.getChannel(), event.getAuthor(), "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
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

	private void runCmd(IUser user, IChannel channel, JSONObject object, String[] args, boolean includeName) throws Exception
	{
		String argument;

		if (args.length == 1)
		{
			if (!(object.getInt("downtimeTimer") >= object.getInt("failedConnectionsThreshold")))
			{
				if (!object.getString("lastMap").equals("N/A"))
				{
					String url = "http://158.69.59.239/mapimgs/" + StringUtils.substring(object.getString("lastMap"), 0, 31) + ".jpg";

					try
					{
						Jsoup.connect(url).get();
					}
					catch (HttpStatusException e)
					{
						url = "https://image.gametracker.com/images/maps/160x120/csgo/" + StringUtils.substring(object.getString("lastMap"), 0, 31) + ".jpg";
					}
					catch (UnsupportedMimeTypeException e)
					{
						// This is to be expected normally because JSoup can't parse a URL serving only a static image
					}

					EmbedBuilder builder = new EmbedBuilder().withColor(Util.averageColorFromURL(new URL(url), true)).withTimestamp(object.getLong("timestamp")).withThumbnail(url).withDescription("Currently Playing: **" + object.getString("lastMap").replace("_", "\\_") + "**\nPlayers Online: **" + object.getString("players") + "**\nQuick Join: **steam://connect/" + object.getString("serverIP") + ":" + object.getInt("serverPort") + "**");

					if (includeName)
					{
						builder = builder.withTitle(object.getString("serverName"));
					}

					EmbedObject embed = builder.build();

					Util.msg(channel, user, embed);
				}
				else
				{
					Util.msg(channel, user, "There doesn't appear to be any server info cached yet (was the service just added?), please wait a moment before trying again");
				}
			}
			else
			{
				Util.msg(channel, user, "The server currently appears to be offline");
			}
		}
		else
		{
			if (object.getBoolean("mapCharacterLimit"))
			{
				argument = StringUtils.substring(args[1], 0, 31);
			}
			else
			{
				argument = args[1];
			}

			boolean mapExists = false;
			String formattedMap = "";
			String lastPlayed = "";
			String firstPlayed = "";

			for (int i = 0; i < object.getJSONArray("mapDatabase").length(); i++)
			{
				String map = object.getJSONArray("mapDatabase").getJSONObject(i).getString("mapName");

				if (map.equalsIgnoreCase(argument))
				{
					mapExists = true;
					formattedMap = map;

					if (object.getJSONArray("mapDatabase").getJSONObject(i).getLong("lastPlayed") != 0)
					{
						lastPlayed = Util.getTime(object.getJSONArray("mapDatabase").getJSONObject(i).getLong("lastPlayed"));
					}
					else
					{
						lastPlayed = "N/A";
					}

					if (object.getJSONArray("mapDatabase").getJSONObject(i).getLong("firstPlayed") != 0)
					{
						firstPlayed = Util.getTime(object.getJSONArray("mapDatabase").getJSONObject(i).getLong("firstPlayed"));
					}
					else
					{
						firstPlayed = "N/A";
					}

					break;
				}
			}

			if (mapExists)
			{
				String url = "http://158.69.59.239/mapimgs/" + formattedMap + ".jpg";

				try
				{
					Jsoup.connect(url).get();
				}
				catch (HttpStatusException e)
				{
					url = "https://image.gametracker.com/images/maps/160x120/csgo/" + formattedMap + ".jpg";
				}
				catch (UnsupportedMimeTypeException e)
				{
					// This is to be expected normally because JSoup can't parse a URL serving only a static image
				}

				EmbedObject embed = new EmbedBuilder().withColor(Util.averageColorFromURL(new URL(url), true)).withThumbnail(url).withDescription("**" + formattedMap + "**").appendField("Last Played", lastPlayed, false).appendField("First Played", firstPlayed, false).build();
				Util.msg(channel, user, embed);
			}
			else
			{
				ArrayList<String> mapDatabase = new ArrayList<>();

				for (int i = 0; i < object.getJSONArray("mapDatabase").length(); i++)
				{
					mapDatabase.add(object.getJSONArray("mapDatabase").getJSONObject(i).getString("mapName"));
				}

				Collections.sort(mapDatabase, String.CASE_INSENSITIVE_ORDER);
				Collections.reverse(mapDatabase);

				for (int i = 0; i < mapDatabase.size(); i++)
				{
					String map = mapDatabase.get(i);

					if (StringUtils.containsIgnoreCase(map, argument))
					{
						formattedMap = map;
						break;
					}
				}

				for (int i = 0; i < object.getJSONArray("mapDatabase").length(); i++)
				{
					String map = object.getJSONArray("mapDatabase").getJSONObject(i).getString("mapName");

					if (map.equalsIgnoreCase(formattedMap))
					{
						if (object.getJSONArray("mapDatabase").getJSONObject(i).getLong("lastPlayed") != 0)
						{
							lastPlayed = Util.getTime(object.getJSONArray("mapDatabase").getJSONObject(i).getLong("lastPlayed"));
						}
						else
						{
							lastPlayed = "N/A";
						}

						if (object.getJSONArray("mapDatabase").getJSONObject(i).getLong("firstPlayed") != 0)
						{
							firstPlayed = Util.getTime(object.getJSONArray("mapDatabase").getJSONObject(i).getLong("firstPlayed"));
						}
						else
						{
							firstPlayed = "N/A";
						}

						break;
					}
				}

				if (!formattedMap.equals(""))
				{
					String url = "http://158.69.59.239/mapimgs/" + formattedMap + ".jpg";

					try
					{
						Jsoup.connect(url).get();
					}
					catch (HttpStatusException e)
					{
						url = "https://image.gametracker.com/images/maps/160x120/csgo/" + formattedMap + ".jpg";
					}
					catch (UnsupportedMimeTypeException e)
					{
						// This is to be expected normally because JSoup can't parse a URL serving only a static image
					}

					EmbedObject embed = new EmbedBuilder().withColor(Util.averageColorFromURL(new URL(url), true)).withThumbnail(url).withDescription("**" + formattedMap + "**").appendField("Last Played", lastPlayed, false).appendField("First Played", firstPlayed, false).build();
					Util.msg(channel, user, embed);
				}
				else
				{
					Util.msg(channel, user, "The map **" + argument + "** doesn't exist!");
				}
			}
		}
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event) throws Exception
	{
		if (selectionMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(selectionMessages.get(event.getUser().getStringID())))
			{
				int i = Util.emojiToInt(event.getReaction().getEmoji().toString()) - 1;

				if (i != -1)
				{
					if (selectionServers.get(event.getUser().getStringID()).contains("server" + i))
					{
						runCmd(event.getUser(), event.getChannel(), new JSONObject(Util.getFileContents("data/services/server-tracking/" + event.getGuild().getStringID() + "/serverInfo.json")).getJSONObject("server" + i), args.get(event.getUser().getStringID()), true);
					}
				}
			}
		}
	}
}