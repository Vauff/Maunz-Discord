package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;

import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Map extends AbstractCommand<MessageCreateEvent>
{
	private static HashMap<Snowflake, List<String>> selectionServers = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> selectionMessages = new HashMap<>();
	private static HashMap<Snowflake, String[]> args = new HashMap<>();

	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		if (!(channel instanceof PrivateChannel))
		{
			String guildID = event.getGuild().block().getId().asString();
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
						runCmd(author, channel, json.getJSONObject(serverList.get(0)), event.getMessage().getContent().get().split(" "), false);
					}
					else
					{
						String object = "";

						for (String objectName : serverList)
						{
							if (json.getJSONObject(objectName).getLong("serverTrackingChannelID") == channel.getId().asLong())
							{
								object = objectName;
								break;
							}
						}

						if (!object.equals(""))
						{
							runCmd(author, channel, json.getJSONObject(object), event.getMessage().getContent().get().split(" "), false);
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

							Message m = Util.msg(channel, author, msg);
							waitForReaction(m.getId(), author.getId());
							selectionServers.put(author.getId(), serverList);
							selectionMessages.put(author.getId(), m.getId());
							args.put(author.getId(), event.getMessage().getContent().get().split(" "));
							Util.addNumberedReactions(m, true, serverList.size());

							ScheduledExecutorService msgDeleterPool = Executors.newScheduledThreadPool(1);

							msgDeleterPool.schedule(() ->
							{
								m.delete();
								selectionServers.remove(author.getId());
								selectionMessages.remove(author.getId());
								msgDeleterPool.shutdown();
							}, 120, TimeUnit.SECONDS);
						}
					}
				}
				else
				{
					Util.msg(channel, author, "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
				}
			}
			else
			{
				Util.msg(channel, author, "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
			}
		}
		else
		{
			Util.msg(channel, author, "This command can't be done in a PM, only in a guild with the server tracking service enabled");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*map" };
	}

	private void runCmd(User user, MessageChannel channel, JSONObject object, String[] args, boolean includeName) throws Exception
	{
		String argument;

		if (args.length == 1)
		{
			if (!(object.getInt("downtimeTimer") >= object.getInt("failedConnectionsThreshold")))
			{
				if (!object.getString("lastMap").equals("N/A"))
				{
					String url = "https://vauff.com/mapimgs/" + StringUtils.substring(object.getString("lastMap"), 0, 31) + ".jpg";

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

					final String finalUrl = url;
					final URL finalConstructedUrl = new URL(url);

					Consumer<EmbedCreateSpec> embed = spec ->
					{
						spec.setColor(Util.averageColorFromURL(finalConstructedUrl, true));
						spec.setTimestamp(Instant.ofEpochMilli(object.getLong("timestamp")));
						spec.setThumbnail(finalUrl);
						spec.setDescription("Currently Playing: **" + object.getString("lastMap").replace("_", "\\_") + "**\nPlayers Online: **" + object.getString("players") + "**\nQuick Join: **steam://connect/" + object.getString("serverIP") + ":" + object.getInt("serverPort") + "**");
					};

					if (includeName)
					{
						Util.msg(channel, user, embed.andThen(spec -> spec.setTitle(object.getString("serverName"))));
					}
					else
					{
						Util.msg(channel, user, embed);
					}
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
				String url = "https://vauff.com/mapimgs/" + formattedMap + ".jpg";

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

				final String finalUrl = url;
				final URL finalConstructedUrl = new URL(url);
				final String finalFormattedMap = formattedMap;
				final String finalLastPlayed = lastPlayed;
				final String finalFirstPlayed = firstPlayed;

				Consumer<EmbedCreateSpec> embed = spec ->
				{
					spec.setColor(Util.averageColorFromURL(finalConstructedUrl, true));
					spec.setThumbnail(finalUrl);
					spec.setDescription("**" + finalFormattedMap + "**");
					spec.addField("Last Played", finalLastPlayed, false);
					spec.addField("First Played", finalFirstPlayed, false);
				};

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

					final String finalUrl = url;
					final URL finalConstructedUrl = new URL(url);
					final String finalFormattedMap = formattedMap;
					final String finalLastPlayed = lastPlayed;
					final String finalFirstPlayed = firstPlayed;

					Consumer<EmbedCreateSpec> embed = spec ->
					{
						spec.setColor(Util.averageColorFromURL(finalConstructedUrl, true));
						spec.setThumbnail(finalUrl);
						spec.setDescription("**" + finalFormattedMap + "**");
						spec.addField("Last Played", finalLastPlayed, false);
						spec.addField("First Played", finalFirstPlayed, false);
					};

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
	public void onReactionAdd(ReactionAddEvent event, Message message) throws Exception
	{
		if (selectionMessages.containsKey(event.getUser().block().getId()) && message.getId().equals(selectionMessages.get(event.getUser().block().getId())))
		{
			int i = Util.emojiToInt(event.getEmoji().asUnicodeEmoji().get().getRaw()) - 1;

			if (i != -1)
			{
				if (selectionServers.get(event.getUser().block().getId()).contains("server" + i))
				{
					runCmd(event.getUser().block(), event.getChannel().block(), new JSONObject(Util.getFileContents("data/services/server-tracking/" + event.getGuild().block().getId().asString() + "/serverInfo.json")).getJSONObject("server" + i), args.get(event.getUser().block().getId()), true);
				}
			}
		}
	}
}