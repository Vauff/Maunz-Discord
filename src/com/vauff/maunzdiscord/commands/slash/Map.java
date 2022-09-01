package com.vauff.maunzdiscord.commands.slash;

import com.mongodb.client.FindIterable;
import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
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

public class Map extends AbstractSlashCommand<ChatInputInteractionEvent>
{
	private static HashMap<Snowflake, List<Document>> selectionServices = new HashMap<>();
	private static HashMap<Snowflake, Integer> selectionPages = new HashMap<>();
	private static HashMap<Snowflake, ApplicationCommandInteraction> cmdInteractions = new HashMap<>();

	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User user) throws Exception
	{
		if (!(channel instanceof PrivateChannel))
		{
			long guildID = guild.getId().asLong();
			FindIterable<Document> servicesIterable = Main.mongoDatabase.getCollection("services").find(and(eq("enabled", true), eq("guildID", guildID)));
			List<Document> services = new ArrayList<>();

			for (Document doc : servicesIterable)
			{
				services.add(doc);
			}

			cmdInteractions.put(user.getId(), event.getInteraction().getCommandInteraction().get());

			if (services.size() == 0)
			{
				event.editReply("A server tracking service is not enabled in this guild yet! Please have a guild administrator use **/services add** to set one up").block();
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
		else
		{
			event.editReply("This command can't be done in a PM, only in a guild with the server tracking service enabled").block();
		}
	}

	@Override
	public void buttonPressed(ButtonInteractionEvent event, String buttonId, Guild guild, MessageChannel channel, User user) throws Exception
	{
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

		for (Document doc : selectionServices.get(user.getId()))
		{
			if (doc.getObjectId("_id").toString().equals(buttonId))
			{
				runCmd(event, user, doc, true);
				break;
			}
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
				event.editReply("The server currently appears to be offline").block();
				return;
			}

			if (doc.getString("lastMap").equals("N/A"))
			{
				event.editReply("There doesn't appear to be any server info cached yet (was the service just added?), please wait a moment before trying again").block();
				return;
			}

			String url = Util.getMapImageURL(doc.getString("lastMap"));

			EmbedCreateSpec embed = EmbedCreateSpec.builder()
				.color(Util.averageColorFromURL(url, true))
				.timestamp(Instant.ofEpochMilli(serverDoc.getLong("timestamp")))
				.thumbnail(url)
				.description("Currently Playing: **" + doc.getString("lastMap").replace("_", "\\_") + "**\nPlayers Online: **" + serverDoc.getString("playerCount") + "**\nQuick Join: **steam://connect/" + serverDoc.getString("ip") + ":" + serverDoc.getInteger("port") + "**")
				.build();

			if (includeName)
			{
				event.editReply("").withEmbeds(embed.withTitle(serverDoc.getString("name"))).block();
			}
			else
			{
				event.editReply("").withEmbeds(embed).block();
			}
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
				String url = Util.getMapImageURL(formattedMap);
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
					.color(Util.averageColorFromURL(url, true))
					.thumbnail(url)
					.description("**" + formattedMap + "**")
					.addField("Last Played", lastPlayed, false)
					.addField("First Played", firstPlayed, false)
					.build();

				event.editReply("").withEmbeds(embed).block();
			}
			else
			{
				event.editReply("The map **" + mapArg + "** doesn't exist!").block();
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
			servers.add(Button.primary(doc.getObjectId("_id").toString(), serverDoc.getString("name")));
		}

		buildPage(event, servers, "Select Server", 8, 2, page, 0, false, null);

		selectionPages.put(user.getId(), page);
		waitForButtonPress(event.getReply().block().getId(), user.getId());
	}

	@Override
	public ApplicationCommandRequest getCommand()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Tells you info about the current map on a server, or one played in the past")
			.addOption(ApplicationCommandOptionData.builder()
				.name("mapname")
				.description("Name of a map to see stats for")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.required(false)
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
