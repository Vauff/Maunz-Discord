package com.vauff.maunzdiscord.servertracking;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.retriever.RestEntityRetriever;
import discord4j.core.spec.EmbedCreateSpec;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

public class ServiceProcessThread implements Runnable
{
	/**
	 * Regex pattern to check for wildcards
	 */
	private static final Pattern WILDCARD_PATTERN = Pattern.compile("(?i)[^*]+|(\\*)");

	private Thread thread;
	public ObjectId id;
	private Guild guild;
	Boolean channelExists = null;

	public ServiceProcessThread(ObjectId id, Guild guild)
	{
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
			Document doc = Main.mongoDatabase.getCollection("services").find(eq("_id", id)).first();
			Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", doc.getObjectId("serverID"))).first();
			String msgServerName = "The server";

			if (Util.isMultiTrackingChannel(doc.getLong("guildID"), doc.getLong("channelID")) || doc.getBoolean("alwaysShowName"))
				msgServerName = "**" + serverDoc.getString("name") + "**";

			if (serverDoc.getInteger("downtimeTimer") >= serverDoc.getInteger("failedConnectionsThreshold") && doc.getBoolean("online"))
			{
				if (channelExists(doc.getLong("channelID")))
					Util.msg((MessageChannel) Main.gateway.getChannelById(Snowflake.of(doc.getLong("channelID"))).block(), true, msgServerName + " has gone offline");

				Main.mongoDatabase.getCollection("services").updateOne(eq("_id", id), new Document("$set", new Document("online", false)));
			}

			if (serverDoc.getInteger("downtimeTimer") >= 10080)
			{
				if (channelExists(doc.getLong("channelID")))
					Util.msg((MessageChannel) Main.gateway.getChannelById(Snowflake.of(doc.getLong("channelID"))).block(), true, msgServerName + " has now been offline for a week and its server tracking was automatically disabled, it can be re-enabled by a guild administrator using **/servers toggle <id> enabled**");

				Main.mongoDatabase.getCollection("services").updateOne(eq("_id", id), new Document("$set", new Document("enabled", false)));
			}

			if (serverDoc.getInteger("downtimeTimer") >= 1)
				return;

			if (!doc.getBoolean("online"))
			{
				if (channelExists(doc.getLong("channelID")))
					Util.msg((MessageChannel) Main.gateway.getChannelById(Snowflake.of(doc.getLong("channelID"))).block(), true, msgServerName + " has come back online");

				Main.mongoDatabase.getCollection("services").updateOne(eq("_id", id), new Document("$set", new Document("online", true)));
			}

			long timestamp = serverDoc.getLong("timestamp");
			String map = serverDoc.getString("map");
			String name = serverDoc.getString("name");
			String playerCount = serverDoc.getString("playerCount");

			if (!map.equals("") && !doc.getString("lastMap").equalsIgnoreCase(map))
			{
				String url = MapImages.getMapImageURL(map, serverDoc.getInteger("appId"));

				EmbedCreateSpec embed = EmbedCreateSpec.builder()
					.color(MapImages.getMapImageColour(url))
					.timestamp(Instant.ofEpochMilli(timestamp))
					.description("Now Playing: **" + map.replace("_", "\\_") + "**\nPlayers Online: **" + playerCount + "**\nQuick Join: **http://vauff.com/?ip=" + serverDoc.getString("ip") + ":" + serverDoc.getInteger("port") + "**")
					.build();

				if (!url.equals(""))
					embed = embed.withThumbnail(url);

				EmbedCreateSpec titledEmbed = embed.withTitle(name);

				if (channelExists(doc.getLong("channelID")))
				{
					if (Util.isMultiTrackingChannel(doc.getLong("guildID"), doc.getLong("channelID")) || doc.getBoolean("alwaysShowName"))
						Util.msg((MessageChannel) Main.gateway.getChannelById(Snowflake.of(doc.getLong("channelID"))).block(), true, titledEmbed);
					else
						Util.msg((MessageChannel) Main.gateway.getChannelById(Snowflake.of(doc.getLong("channelID"))).block(), true, embed);
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
							catch (Exception e)
							{
								//invalid member, or member is no longer in guild
								continue parentLoop;
							}

							Util.msg(member.getPrivateChannel().block(), true, titledEmbed);
							break;
						}
					}
				}

				Main.mongoDatabase.getCollection("services").updateOne(eq("_id", id), new Document("$set", new Document("lastMap", map)));
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

	private boolean channelExists(long channelID)
	{
		if (!Objects.isNull(channelExists))
			return channelExists;

		try
		{
			new RestEntityRetriever(Main.gateway).getChannelById(Snowflake.of(channelID)).block();
			channelExists = true;
		}
		catch (Exception e)
		{
			channelExists = false;
		}

		return channelExists;
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
