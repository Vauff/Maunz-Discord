package com.vauff.maunzdiscord.core;

import com.mongodb.client.MongoCollection;
import com.vauff.maunzdiscord.threads.ButtonInteractionThread;
import com.vauff.maunzdiscord.threads.ChatInputInteractionThread;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.bson.Document;

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
}
