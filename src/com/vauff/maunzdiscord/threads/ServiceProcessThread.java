package com.vauff.maunzdiscord.threads;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.timers.ServerTimer;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.http.client.ClientException;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;

import java.net.ConnectException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

public class ServiceProcessThread implements Runnable
{
	/**
	 * Regex pattern to check for wildcards
	 */
	private static final Pattern WILDCARD_PATTERN = Pattern.compile("(?i)[^*]+|(\\*)");

	private Document doc;
	private Thread thread;
	private String id;
	private Guild guild;

	public ServiceProcessThread(Document doc, String id, Guild guild)
	{
		this.doc = doc;
		this.id = id;
		this.guild = guild;
	}

	public void start()
	{
		if (thread == null)
		{
			thread = new Thread(this, "servertracking-" + id);
			thread.start();
		}
	}

	public void run()
	{
		try
		{
			Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", doc.getObjectId("serverID"))).first();
			boolean channelExists = true;
			String msgServerName = "The server";

			try
			{
				Main.gateway.getChannelById(Snowflake.of(doc.getLong("channelID"))).block();
			}
			catch (Exception e)
			{
				channelExists = false;
			}

			if (Util.isMultiTrackingChannel(doc.getLong("guildID"), doc.getLong("channelID")))
				msgServerName = "**" + serverDoc.getString("name") + "**";

			if (serverDoc.getInteger("downtimeTimer") == serverDoc.getInteger("failedConnectionsThreshold"))
			{
				if (channelExists)
					Util.msg((MessageChannel) Main.gateway.getChannelById(Snowflake.of(doc.getLong("channelID"))).block(), msgServerName + " has gone offline");

				Main.mongoDatabase.getCollection("services").updateOne(eq("_id", doc.getObjectId("_id")), new Document("$set", new Document("online", false)));
			}

			if (serverDoc.getInteger("downtimeTimer") >= 10080)
			{
				if (channelExists)
					Util.msg((MessageChannel) Main.gateway.getChannelById(Snowflake.of(doc.getLong("channelID"))).block(), msgServerName + " has now been offline for a week and its server tracking service was automatically disabled, it can be re-enabled by a guild administrator using the ***services** command");

				Main.mongoDatabase.getCollection("services").updateOne(eq("_id", doc.getObjectId("_id")), new Document("$set", new Document("enabled", false)));
			}

			if (serverDoc.getInteger("downtimeTimer") >= 1)
				return;

			if (!doc.getBoolean("online"))
			{
				if (channelExists)
					Util.msg((MessageChannel) Main.gateway.getChannelById(Snowflake.of(doc.getLong("channelID"))).block(), msgServerName + " has come back online");

				Main.mongoDatabase.getCollection("services").updateOne(eq("_id", doc.getObjectId("_id")), new Document("$set", new Document("online", true)));
			}

			long timestamp = serverDoc.getLong("timestamp");
			String map = serverDoc.getString("map");
			String name = serverDoc.getString("name");
			String playerCount = serverDoc.getString("playerCount");
			String url = "https://vauff.com/mapimgs/" + StringUtils.substring(map, 0, 31) + ".jpg";

			if (!map.equals("") && !doc.getString("lastMap").equalsIgnoreCase(map))
			{
				try
				{
					Jsoup.connect(url).get();
				}
				catch (HttpStatusException | ConnectException e)
				{
					url = "https://image.gametracker.com/images/maps/160x120/csgo/" + StringUtils.substring(map, 0, 31) + ".jpg";
				}
				catch (UnsupportedMimeTypeException e)
				{
					// This is to be expected normally because JSoup can't parse a URL serving only a static image
				}

				final String finalUrl = url;
				final URL constructedUrl = new URL(url);

				Consumer<EmbedCreateSpec> embed = spec ->
				{
					spec.setColor(Util.averageColorFromURL(constructedUrl, true));
					spec.setTimestamp(Instant.ofEpochMilli(timestamp));
					spec.setThumbnail(finalUrl);
					spec.setDescription("Now Playing: **" + map.replace("_", "\\_") + "**\nPlayers Online: **" + playerCount + "**\nQuick Join: **steam://connect/" + serverDoc.getString("ip") + ":" + serverDoc.getInteger("port") + "**");
				};

				Consumer<EmbedCreateSpec> titledEmbed = embed.andThen(spec -> spec.setTitle(name));

				if (channelExists)
				{
					if (Util.isMultiTrackingChannel(doc.getLong("guildID"), doc.getLong("channelID")))
						Util.msg((MessageChannel) Main.gateway.getChannelById(Snowflake.of(doc.getLong("channelID"))).block(), titledEmbed);
					else
						Util.msg((MessageChannel) Main.gateway.getChannelById(Snowflake.of(doc.getLong("channelID"))).block(), embed);
				}

				parentLoop:
				for (Document notificationDoc : doc.getList("notifications", Document.class))
				{
					for (String mapNotification : notificationDoc.getList("notifications", String.class))
					{
						if (wildcardMatches(mapNotification, map))
						{
							Member member;

							try
							{
								// Setting a 10 second timeout on the block() since it has previously hung tracking threads
								member = guild.getMemberById(Snowflake.of(notificationDoc.getLong("userID")), EntityRetrievalStrategy.REST).block(Duration.ofSeconds(10));
							}
							catch (ClientException | IllegalStateException e)
							{
								//invalid member, or member is no longer in guild
								continue parentLoop;
							}

							Util.msg(member.getPrivateChannel().block(), titledEmbed);
							break;
						}
					}
				}

				Main.mongoDatabase.getCollection("services").updateOne(eq("_id", doc.getObjectId("_id")), new Document("$set", new Document("lastMap", map)));
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
		finally
		{
			ServerTimer.threadRunning.put(id, false);
		}
	}

	/**
	 * Checks if provided text matches the provided pattern with wildcards
	 *
	 * @param pattern Provided pattern with wildcards
	 * @param text    Text to match with the pattern
	 * @return Whether the text matches the pattern
	 */
	private static boolean wildcardMatches(String pattern, String text)
	{
		final Matcher matcher = WILDCARD_PATTERN.matcher(pattern);
		final StringBuffer buffer = new StringBuffer();

		buffer.append("(?i)");
		while (matcher.find())
		{
			if (matcher.group(1) != null)
			{
				matcher.appendReplacement(buffer, ".*");
			}
			else
			{
				String replacement = "\\Q" + matcher.group(0) + "\\E";
				matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
			}
		}

		matcher.appendTail(buffer);
		final String replaced = buffer.toString();

		return text.matches(replaced);
	}
}
