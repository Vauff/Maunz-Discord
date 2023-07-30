package com.vauff.maunzdiscord.core;

import com.mongodb.client.MongoCollection;
import com.vauff.maunzdiscord.servertracking.ServerTrackingLoop;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class MainListener
{
	public static void onGuildCreate(GuildCreateEvent event)
	{
		try
		{
			long guildId = event.getGuild().getId().asLong();
			String guildName = event.getGuild().getName();
			MongoCollection<Document> col = Main.mongoDatabase.getCollection("guilds");

			if (col.countDocuments(eq("guildId", guildId)) == 0L)
			{
				Document doc = new Document("guildId", guildId).append("lastGuildName", guildName);
				col.insertOne(doc);
			}
			else if (!col.find(eq("guildId", guildId)).first().getString("lastGuildName").equals(guildName))
			{
				col.updateOne(eq("guildId", guildId), new Document("$set", new Document("lastGuildName", guildName)));
			}

			Main.guildCache.put(event.getGuild().getId(), event.getGuild());

			// Invalidate active service cache for any servers this guild tracks
			invalidateActiveServices(event.getGuild().getId().asLong());
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onGuildDelete(GuildDeleteEvent event)
	{
		try
		{
			Main.guildCache.remove(event.getGuildId());

			// Invalidate active service cache for any servers this guild tracked
			invalidateActiveServices(event.getGuildId().asLong());
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onChatInputInteraction(ChatInputInteractionEvent event)
	{
		try
		{
			new ChatInputInteractionThread(event, "chatinputinteraction-" + event.getInteraction().getId().asString()).start();
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onButtonInteraction(ButtonInteractionEvent event)
	{
		try
		{
			new ButtonInteractionThread(event, "buttoninteraction-" + event.getInteraction().getId().asString()).start();
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	private static void invalidateActiveServices(long guildId)
	{
		List<Document> serviceDocs = Main.mongoDatabase.getCollection("services").find(and(eq("enabled", true), eq("guildID", guildId))).projection(new Document("serverID", 1)).into(new ArrayList<>());

		for (Document doc : serviceDocs)
		{
			if (ServerTrackingLoop.serverActiveServices.containsKey(doc.getObjectId("serverID")))
			{
				ServerTrackingLoop.lastInvalidatedCache = Instant.now();
				ServerTrackingLoop.serverActiveServices.remove(doc.getObjectId("serverID"));
			}
		}
	}
}
