package com.vauff.maunzdiscord.commands;

import com.mongodb.client.FindIterable;
import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.servertracking.MapImages;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Map extends AbstractCommand<ChatInputInteractionEvent>
{
	private final static HashMap<Snowflake, List<Document>> selectionServices = new HashMap<>();
	private final static HashMap<Snowflake, Integer> selectionPages = new HashMap<>();
	private final static HashMap<Snowflake, ApplicationCommandInteraction> cmdInteractions = new HashMap<>();

	@Override
	public void exe(ChatInputInteractionEvent event, MessageChannel channel, User user) throws Exception
	{
		if (channel instanceof PrivateChannel)
		{
			Util.editReply(event, "This command can't be done in a PM, only in a guild with server tracking enabled");
			return;
		}

		Guild guild = event.getInteraction().getGuild().block();

		long guildID = guild.getId().asLong();
		FindIterable<Document> servicesIterable = Main.mongoDatabase.getCollection("services").find(and(eq("enabled", true), eq("guildID", guildID)));
		List<Document> services = new ArrayList<>();

		for (Document doc : servicesIterable)
			services.add(doc);

		cmdInteractions.put(user.getId(), event.getInteraction().getCommandInteraction().get());

		if (services.size() == 0)
		{
			Util.editReply(event, "Server tracking is not enabled in this guild yet! Please have a guild administrator use " + Main.commands.get("servers").getCommandMention(event, "add") + " to set one up");
			return;
		}
		else if (services.size() == 1)
		{
			runCmd(event, user, services.get(0), services.get(0).getBoolean("alwaysShowName"));
		}
		else
		{
			for (Document doc : services)
			{
				if (doc.getLong("channelID") == channel.getId().asLong() && !Util.isMultiTrackingChannel(guildID, channel.getId().asLong()))
				{
					runCmd(event, user, doc, doc.getBoolean("alwaysShowName"));
					return;
				}
			}

			selectionServices.put(user.getId(), services);
			runSelection(event, user, 1);
		}
	}

	@Override
	public void buttonPressed(ButtonInteractionEvent event, String buttonId, MessageChannel channel, User user) throws Exception
	{
		for (Document doc : selectionServices.get(user.getId()))
		{
			if (doc.getObjectId("_id").toString().equals(buttonId))
			{
				runCmd(event, user, doc, true);
				return;
			}
		}

		int page = selectionPages.get(user.getId());

		if (buttonId.equals(NEXT_BTN))
		{
			runSelection(event, user, page + 1);
			return;
		}
		else if (buttonId.equals(PREV_BTN))
		{
			runSelection(event, user, page - 1);
			return;
		}
	}

	private void runCmd(DeferrableInteractionEvent event, User user, Document doc, boolean includeName) throws Exception
	{
		Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", doc.getObjectId("serverID"))).first();
		ApplicationCommandInteraction interaction = cmdInteractions.get(user.getId());

		if (!interaction.getOption("mapname").isPresent())
		{
			if (!doc.getBoolean("online"))
			{
				Util.editReply(event, "The server currently appears to be offline");
				return;
			}

			if (doc.getString("lastMap").equals("N/A"))
			{
				Util.editReply(event, "There doesn't appear to be any server info cached yet (was the server just added?), please wait a moment before trying again");
				return;
			}

			String url = MapImages.getMapImageURL(doc.getString("lastMap"), serverDoc.getInteger("appId"));
			String ipPort = serverDoc.getString("ip") + ":" + serverDoc.getInteger("port");

			EmbedCreateSpec embed = EmbedCreateSpec.builder()
				.color(MapImages.getMapImageColour(url))
				.timestamp(Instant.ofEpochMilli(serverDoc.getLong("timestamp")))
				.description("Currently Playing: **" + doc.getString("lastMap").replace("_", "\\_") + "**\nPlayers Online: **" + serverDoc.getString("playerCount") + "**\nQuick Join: **[" + ipPort + "](http://vauff.com/?ip=" + ipPort + ")**")
				.build();

			if (!url.equals(""))
				embed = embed.withThumbnail(url);

			if (includeName)
				Util.editReply(event, "", embed.withTitle(serverDoc.getString("name")));
			else
				Util.editReply(event, "", embed);
		}
		else
		{
			String mapArg = interaction.getOption("mapname").get().getValue().get().asString();
			String formattedMap = "";

			if (doc.getBoolean("mapCharacterLimit"))
				mapArg = StringUtils.substring(mapArg, 0, 31);

			for (int i = 0; i < serverDoc.getList("mapDatabase", Document.class).size(); i++)
			{
				String map = serverDoc.getList("mapDatabase", Document.class).get(i).getString("map");

				if (map.equalsIgnoreCase(mapArg))
				{
					formattedMap = map;
					break;
				}
			}

			if (formattedMap.equals(""))
			{
				ArrayList<Long> mapDatabaseTimestamps = new ArrayList<>();
				ArrayList<String> mapDatabase = new ArrayList<>();

				for (int i = 0; i < serverDoc.getList("mapDatabase", Document.class).size(); i++)
					mapDatabaseTimestamps.add(serverDoc.getList("mapDatabase", Document.class).get(i).getLong("lastPlayed"));

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

					if (StringUtils.containsIgnoreCase(map, mapArg))
					{
						formattedMap = map;
						break;
					}
				}
			}

			if (!formattedMap.equals(""))
			{
				String url = MapImages.getMapImageURL(formattedMap, serverDoc.getInteger("appId"));
				String lastPlayed = "";
				String firstPlayed = "";

				for (int i = 0; i < serverDoc.getList("mapDatabase", Document.class).size(); i++)
				{
					String map = serverDoc.getList("mapDatabase", Document.class).get(i).getString("map");

					if (map.equalsIgnoreCase(formattedMap))
					{
						if (serverDoc.getList("mapDatabase", Document.class).get(i).getLong("lastPlayed") != 0)
							lastPlayed = "<t:" + (serverDoc.getList("mapDatabase", Document.class).get(i).getLong("lastPlayed") / 1000) + ":F>";
						else
							lastPlayed = "N/A";

						if (serverDoc.getList("mapDatabase", Document.class).get(i).getLong("firstPlayed") != 0)
							firstPlayed = "<t:" + (serverDoc.getList("mapDatabase", Document.class).get(i).getLong("firstPlayed") / 1000) + ":F>";
						else
							firstPlayed = "N/A";

						break;
					}
				}

				EmbedCreateSpec embed = EmbedCreateSpec.builder()
					.color(MapImages.getMapImageColour(url))
					.description("**" + formattedMap + "**")
					.addField("Last Played", lastPlayed, false)
					.addField("First Played", firstPlayed, false)
					.build();

				if (!url.equals(""))
					embed = embed.withThumbnail(url);

				Util.editReply(event, "", embed);
			}
			else
			{
				Util.editReply(event, "The map **" + mapArg + "** doesn't exist!");
			}
		}
	}

	private void runSelection(DeferrableInteractionEvent event, User user, int page) throws Exception
	{
		List<Document> services = selectionServices.get(user.getId());
		ArrayList<Button> servers = new ArrayList<>();

		for (Document doc : services)
		{
			Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", doc.getObjectId("serverID"))).first();
			servers.add(Button.primary(doc.getObjectId("_id").toString(), StringUtils.substring(serverDoc.getString("name"), 0, 80)));
		}

		buildPage(event, servers, "Select Server", 8, 2, page, 0, false, null, "");

		selectionPages.put(user.getId(), page);
		waitForButtonPress(event.getReply().block().getId(), user.getId());
	}

	@Override
	public ApplicationCommandRequest getCommandRequest()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Tells you info about the current map on a server, or one played in the past")
			.addOption(ApplicationCommandOptionData.builder()
				.name("mapname")
				.description("Name of a map to see stats for")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "map";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}
