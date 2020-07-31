package com.vauff.maunzdiscord.commands;

import com.mongodb.client.FindIterable;
import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.CommandHelp;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;

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

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Map extends AbstractCommand<MessageCreateEvent>
{
	private static HashMap<Snowflake, List<Document>> selectionServices = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> selectionMessages = new HashMap<>();
	private static HashMap<Snowflake, Integer> selectionPages = new HashMap<>();
	private static HashMap<Snowflake, String[]> args = new HashMap<>();

	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		if (!(channel instanceof PrivateChannel))
		{
			long guildID = event.getGuild().block().getId().asLong();
			FindIterable<Document> servicesIterable = Main.mongoDatabase.getCollection("services").find(and(eq("enabled", true), eq("guildID", guildID)));
			List<Document> services = new ArrayList<>();

			for (Document doc : servicesIterable)
			{
				services.add(doc);
			}

			if (services.size() == 0)
			{
				Util.msg(channel, author, "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
				return;
			}
			else if (services.size() == 1)
			{
				runCmd(author, channel, services.get(0), event.getMessage().getContent().split(" "), false);
			}
			else
			{
				for (Document doc : services)
				{
					if (doc.getLong("channelID") == channel.getId().asLong() && !Util.isMultiTrackingChannel(guildID, channel.getId().asLong()))
					{
						runCmd(author, channel, doc, event.getMessage().getContent().split(" "), false);
						return;
					}
				}

				args.put(author.getId(), event.getMessage().getContent().split(" "));
				runSelection(author, channel, services, 1);
			}
		}
		else
		{
			Util.msg(channel, author, "This command can't be done in a PM, only in a guild with the server tracking service enabled");
		}
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event, Message message) throws Exception
	{
		String emoji = event.getEmoji().asUnicodeEmoji().get().getRaw();
		User user = event.getUser().block();

		if (selectionMessages.containsKey(event.getUser().block().getId()) && message.getId().equals(selectionMessages.get(event.getUser().block().getId())))
		{
			if (emoji.equals("▶"))
			{
				runSelection(user, event.getChannel().block(), selectionServices.get(user.getId()), selectionPages.get(user.getId()) + 1);
				return;
			}

			else if (emoji.equals("◀"))
			{
				runSelection(user, event.getChannel().block(), selectionServices.get(user.getId()), selectionPages.get(user.getId()) - 1);
				return;
			}

			int i = Util.emojiToInt(emoji) + ((selectionPages.get(user.getId()) - 1) * 5) - 1;

			if (i != -2)
			{
				if (selectionServices.get(event.getUser().block().getId()).size() >= i)
				{
					runCmd(event.getUser().block(), event.getChannel().block(), selectionServices.get(event.getUser().block().getId()).get(i), args.get(event.getUser().block().getId()), true);
				}
			}
		}
	}

	private void runCmd(User user, MessageChannel channel, Document doc, String[] args, boolean includeName) throws Exception
	{
		Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", doc.getObjectId("serverID"))).first();
		String argument;

		if (args.length == 1)
		{
			if (!doc.getBoolean("online"))
			{
				Util.msg(channel, user, "The server currently appears to be offline");
				return;
			}

			if (doc.getString("lastMap").equals("N/A"))
			{
				Util.msg(channel, user, "There doesn't appear to be any server info cached yet (was the service just added?), please wait a moment before trying again");
				return;
			}

			String url = "https://vauff.com/mapimgs/" + StringUtils.substring(doc.getString("lastMap"), 0, 31) + ".jpg";

			try
			{
				Jsoup.connect(url).get();
			}
			catch (HttpStatusException e)
			{
				url = "https://image.gametracker.com/images/maps/160x120/csgo/" + StringUtils.substring(doc.getString("lastMap"), 0, 31) + ".jpg";
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
				spec.setTimestamp(Instant.ofEpochMilli(serverDoc.getLong("timestamp")));
				spec.setThumbnail(finalUrl);
				spec.setDescription("Currently Playing: **" + doc.getString("lastMap").replace("_", "\\_") + "**\nPlayers Online: **" + serverDoc.getString("playerCount") + "**\nQuick Join: **steam://connect/" + serverDoc.getString("ip") + ":" + serverDoc.getInteger("port") + "**");
			};

			if (includeName)
			{
				Util.msg(channel, user, embed.andThen(spec -> spec.setTitle(serverDoc.getString("name"))));
			}
			else
			{
				Util.msg(channel, user, embed);
			}
		}
		else
		{
			if (doc.getBoolean("mapCharacterLimit"))
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

			for (int i = 0; i < serverDoc.getList("mapDatabase", Document.class).size(); i++)
			{
				String map = serverDoc.getList("mapDatabase", Document.class).get(i).getString("map");

				if (map.equalsIgnoreCase(argument))
				{
					mapExists = true;
					formattedMap = map;

					if (serverDoc.getList("mapDatabase", Document.class).get(i).getLong("lastPlayed") != 0)
					{
						lastPlayed = Util.getTime(serverDoc.getList("mapDatabase", Document.class).get(i).getLong("lastPlayed"));
					}
					else
					{
						lastPlayed = "N/A";
					}

					if (serverDoc.getList("mapDatabase", Document.class).get(i).getLong("firstPlayed") != 0)
					{
						firstPlayed = Util.getTime(serverDoc.getList("mapDatabase", Document.class).get(i).getLong("firstPlayed"));
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
				ArrayList<Long> mapDatabaseTimestamps = new ArrayList<>();
				ArrayList<String> mapDatabase = new ArrayList<>();

				for (int i = 0; i < serverDoc.getList("mapDatabase", Document.class).size(); i++)
				{
					mapDatabaseTimestamps.add(serverDoc.getList("mapDatabase", Document.class).get(i).getLong("lastPlayed"));
				}

				Collections.sort(mapDatabaseTimestamps);
				Collections.reverse(mapDatabaseTimestamps);

				for (int i = 0; i < mapDatabaseTimestamps.size(); i++)
				{
					long timestamp = mapDatabaseTimestamps.get(i);

					for (int j = 0; j < serverDoc.getList("mapDatabase", Document.class).size(); j++)
					{
						Document databaseEntry = serverDoc.getList("mapDatabase", Document.class).get(j);

						if (databaseEntry.getLong("lastPlayed") == timestamp)
							mapDatabase.add(databaseEntry.getString("map"));
					}
				}

				for (int i = 0; i < mapDatabase.size(); i++)
				{
					String map = mapDatabase.get(i);

					if (StringUtils.containsIgnoreCase(map, argument))
					{
						formattedMap = map;
						break;
					}
				}

				for (int i = 0; i < serverDoc.getList("mapDatabase", Document.class).size(); i++)
				{
					String map = serverDoc.getList("mapDatabase", Document.class).get(i).getString("map");

					if (map.equalsIgnoreCase(formattedMap))
					{
						if (serverDoc.getList("mapDatabase", Document.class).get(i).getLong("lastPlayed") != 0)
						{
							lastPlayed = Util.getTime(serverDoc.getList("mapDatabase", Document.class).get(i).getLong("lastPlayed"));
						}
						else
						{
							lastPlayed = "N/A";
						}

						if (serverDoc.getList("mapDatabase", Document.class).get(i).getLong("firstPlayed") != 0)
						{
							firstPlayed = Util.getTime(serverDoc.getList("mapDatabase", Document.class).get(i).getLong("firstPlayed"));
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
					Util.msg(channel, user, "The map **" + argument + "** doesn't exist!");
				}
			}
		}
	}

	private void runSelection(User user, MessageChannel channel, List<Document> services, int page)
	{
		ArrayList<String> servers = new ArrayList<>();

		for (Document doc : services)
		{
			Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", doc.getObjectId("serverID"))).first();
			servers.add(serverDoc.getString("name"));
		}

		Message m = Util.buildPage(servers, "Select Server", 5, page, 2, false, true, true, channel, user);

		selectionServices.put(user.getId(), services);
		selectionMessages.put(user.getId(), m.getId());
		selectionPages.put(user.getId(), page);
		waitForReaction(m.getId(), user.getId());

		ScheduledExecutorService msgDeleterPool = Executors.newScheduledThreadPool(1);

		msgDeleterPool.schedule(() ->
		{
			m.delete();
			selectionServices.remove(user.getId());
			selectionMessages.remove(user.getId());
			selectionPages.remove(user.getId());
			msgDeleterPool.shutdown();
		}, 120, TimeUnit.SECONDS);
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*map" };
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		CommandHelp[] commandHelps = new CommandHelp[2];

		commandHelps[0] = new CommandHelp("", "Tells you which map a server is playing outside of its standard map tracking channel.");
		commandHelps[1] = new CommandHelp("[mapname]", "Gives you information on a specific map such as last time played.");

		return commandHelps;
	}
}