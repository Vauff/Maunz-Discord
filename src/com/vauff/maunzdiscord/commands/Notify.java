package com.vauff.maunzdiscord.commands;

import com.mongodb.client.FindIterable;
import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Notify extends AbstractCommand<ChatInputInteractionEvent>
{
	/**
	 * Button names used for buildPage
	 */
	private final String SELECTION_BTN_SUFFIX = "-selection";
	private final String NOTIFY_BTN_SUFFIX = "-notify";

	private final static HashMap<Snowflake, Integer> listPages = new HashMap<>();
	private final Map<String, String> cachedMaps = new LinkedHashMap<>();
	private final Map<String, Long> cachedPlayedMaps = new HashMap<>();
	private long lastCached = -1;
	private long oldestMapPlayed = System.currentTimeMillis();
	private final static HashMap<Snowflake, List<Document>> selectionServices = new HashMap<>();
	private final static HashMap<Snowflake, ObjectId> selectedServices = new HashMap<>();
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

		selectionServices.put(user.getId(), services);
		cmdInteractions.put(user.getId(), event.getInteraction().getCommandInteraction().get());

		if (services.size() == 0)
		{
			Util.editReply(event, "Server tracking is not enabled in this guild yet! Please have a guild administrator use " + Main.commands.get("servers").getCommandMention(event, "add") + " to set one up");
		}
		else if (services.size() == 1)
		{
			selectedServices.put(user.getId(), services.get(0).getObjectId("_id"));
			runCmd(event, user, services.get(0));
		}
		else
		{
			for (Document doc : services)
			{
				if (doc.getLong("channelID") == channel.getId().asLong() && !Util.isMultiTrackingChannel(guildID, channel.getId().asLong()))
				{
					selectedServices.put(user.getId(), doc.getObjectId("_id"));
					runCmd(event, user, doc);
					return;
				}
			}

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
				selectedServices.put(user.getId(), doc.getObjectId("_id"));
				runCmd(event, user, doc);
				return;
			}
		}

		Document doc = Main.mongoDatabase.getCollection("services").find(eq("_id", selectedServices.get(user.getId()))).first();

		if (buttonId.startsWith(NEXT_BTN) || buttonId.startsWith(PREV_BTN))
		{
			int page = listPages.get(user.getId()) + (buttonId.startsWith(NEXT_BTN) ? 1 : -1);

			if (buttonId.endsWith(SELECTION_BTN_SUFFIX))
				runSelection(event, user, page);
			else if (buttonId.endsWith(NOTIFY_BTN_SUFFIX))
				runNotificationList(event, user, doc, page);
		}
		else if (buttonId.startsWith("confirm-"))
		{
			List<Document> notificationDocs = doc.getList("notifications", Document.class);

			if (buttonId.endsWith("wipe"))
			{
				for (int i = 0; i < notificationDocs.size(); i++)
				{
					long userID = notificationDocs.get(i).getLong("userID");

					if (userID != user.getId().asLong())
						continue;

					Main.mongoDatabase.getCollection("services").updateOne(eq("_id", doc.getObjectId("_id")), new Document("$unset", new Document("notifications." + i, 1)));
					Main.mongoDatabase.getCollection("services").updateOne(eq("_id", doc.getObjectId("_id")), new Document("$pull", new Document("notifications", null)));
					Util.editReply(event, "Successfully wiped all of your map notifications!");
					break;
				}
			}
			else
			{
				String map = buttonId.split("confirm-")[1];
				boolean mapSet = false;
				int index = 0;

				for (int i = 0; i < notificationDocs.size(); i++)
				{
					if (notificationDocs.get(i).getLong("userID") != user.getId().asLong())
						continue;

					List<String> notifications = notificationDocs.get(i).getList("notifications", String.class);

					for (int j = 0; j < notifications.size(); j++)
					{
						if (notifications.get(j).equalsIgnoreCase(map))
						{
							mapSet = true;
							index = j;
							break;
						}
					}
				}

				if (!mapSet)
					addNotification(event, user, doc, map);
				else
					removeNotification(event, user, doc, map, index);
			}
		}
		else if (buttonId.equals("cancel-wipe"))
		{
			Util.editReply(event, "Notification wipe was cancelled");
		}
		else if (buttonId.equals("cancel-notify"))
		{
			Util.editReply(event, "Notification toggle was cancelled");
		}
	}

	private void runCmd(DeferrableInteractionEvent event, User user, Document doc) throws Exception
	{
		Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", doc.getObjectId("serverID"))).first();
		List<Document> notificationDocs = doc.getList("notifications", Document.class);
		ApplicationCommandInteraction interaction = cmdInteractions.get(user.getId());

		boolean hasNotifications = false;

		for (int i = 0; i < notificationDocs.size(); i++)
		{
			if (notificationDocs.get(i).getLong("userID") != user.getId().asLong() || notificationDocs.get(i).getList("notifications", String.class).size() == 0)
				continue;

			hasNotifications = true;
			break;
		}

		if (interaction.getOption("list").isPresent())
		{
			if (!hasNotifications)
			{
				Util.editReply(event, "You do not have any map notifications set! Use " + Main.commands.get("notify").getCommandMention(event, "toggle") + " to add one");
				return;
			}

			if (interaction.getOption("list").get().getOption("page").isPresent())
				runNotificationList(event, user, doc, (int) interaction.getOption("list").get().getOption("page").get().getValue().get().asLong());
			else
				runNotificationList(event, user, doc, 1);
		}
		else if (interaction.getOption("wipe").isPresent())
		{
			if (!hasNotifications)
			{
				Util.editReply(event, "You don't have any map notifications to wipe!");
				return;
			}

			List<Button> buttons = new ArrayList<>();
			buttons.add(Button.danger("confirm-wipe", "Wipe ALL Notifications"));
			buttons.add(Button.primary("cancel-wipe", "Cancel"));

			Util.editReply(event, "Are you sure you want to wipe **ALL** of your map notifications?", ActionRow.of(buttons));
			waitForButtonPress(event.getReply().block().getId(), user.getId());
		}
		else if (interaction.getOption("toggle").isPresent())
		{
			String mapArg = interaction.getOption("toggle").get().getOption("mapname").get().getValue().get().asString();
			boolean mapSet = false;
			boolean mapExists = false;
			int index = 0;

			if (doc.getBoolean("mapCharacterLimit"))
				mapArg = StringUtils.substring(mapArg, 0, 31);

			for (int i = 0; i < notificationDocs.size(); i++)
			{
				if (notificationDocs.get(i).getLong("userID") != user.getId().asLong())
					continue;

				List<String> notifications = notificationDocs.get(i).getList("notifications", String.class);

				for (int j = 0; j < notifications.size(); j++)
				{
					String notification = notifications.get(j);

					if (notification.equalsIgnoreCase(mapArg))
					{
						mapSet = true;
						index = j;
						break;
					}
				}
			}

			for (int i = 0; i < serverDoc.getList("mapDatabase", Document.class).size(); i++)
			{
				String map = serverDoc.getList("mapDatabase", Document.class).get(i).getString("map");

				if (map.equalsIgnoreCase(mapArg) || mapArg.contains("*"))
				{
					mapExists = true;
					break;
				}
			}

			if (mapSet)
			{
				removeNotification(event, user, doc, mapArg, index);
			}
			else
			{
				if (mapExists)
				{
					addNotification(event, user, doc, mapArg);
				}
				else
				{
					String mapSuggestion = "";
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
							mapSuggestion = map;
							break;
						}
					}

					List<Button> buttons = new ArrayList<>();

					if (mapSuggestion.equals(""))
					{
						buttons.add(Button.success("confirm-" + mapArg, StringUtils.substring("Add " + mapArg, 0, 80)));
						buttons.add(Button.danger("cancel-notify", "Cancel"));

						Util.editReply(event, "The map **" + mapArg.replace("_", "\\_") + "** is not in my maps database, are you sure you'd like to add it?", ActionRow.of(buttons));
						waitForButtonPress(event.getReply().block().getId(), user.getId());
					}
					else
					{
						buttons.add(Button.primary("confirm-" + mapSuggestion, StringUtils.substring(mapSuggestion, 0, 80)));
						buttons.add(Button.primary("confirm-" + mapArg, StringUtils.substring(mapArg, 0, 80)));

						Util.editReply(event, "The map **" + mapArg.replace("_", "\\_") + "** is not in my maps database (did you maybe mean **" + mapSuggestion.replace("_", "\\_") + "** instead?), please select which map you would like to choose", ActionRow.of(buttons));
						waitForButtonPress(event.getReply().block().getId(), user.getId());
					}
				}
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

		buildPage(event, servers, "Select Server", 8, 2, page, 0, false, null, SELECTION_BTN_SUFFIX);
		listPages.put(user.getId(), page);
		waitForButtonPress(event.getReply().block().getId(), user.getId());
	}

	private void runNotificationList(DeferrableInteractionEvent event, User user, Document doc, int page) throws Exception
	{
		List<String> notifications = new ArrayList<>();

		for (int i = 0; i < doc.getList("notifications", Document.class).size(); i++)
		{
			if (doc.getList("notifications", Document.class).get(i).getLong("userID") != user.getId().asLong())
				continue;

			notifications = doc.getList("notifications", Document.class).get(i).getList("notifications", String.class);
			break;
		}

		buildPage(event, notifications, "Notification List", 10, 0, page, 0, true, null, NOTIFY_BTN_SUFFIX);
		listPages.put(user.getId(), page);
		waitForButtonPress(event.getReply().block().getId(), user.getId());
	}

	private void addNotification(DeferrableInteractionEvent event, User user, Document doc, String notification)
	{
		List<Document> notificationDocs = doc.getList("notifications", Document.class);

		Util.editReply(event, "Adding **" + notification.replace("_", "\\_") + "** to your map notifications!");

		for (int i = 0; i < notificationDocs.size(); i++)
		{
			if (notificationDocs.get(i).getLong("userID") != user.getId().asLong())
				continue;

			Main.mongoDatabase.getCollection("services").updateOne(eq("_id", doc.getObjectId("_id")), new Document("$push", new Document("notifications." + i + ".notifications", notification.replace("﻿", ""))));
			return;
		}

		Document notificationDoc = new Document();
		ArrayList<String> notifications = new ArrayList<>();

		notificationDoc.put("userID", user.getId().asLong());
		notifications.add(notification.replace("﻿", ""));
		notificationDoc.put("notifications", notifications);

		Main.mongoDatabase.getCollection("services").updateOne(eq("_id", doc.getObjectId("_id")), new Document("$push", new Document("notifications", notificationDoc)));
	}

	private void removeNotification(DeferrableInteractionEvent event, User user, Document doc, String notification, int index)
	{
		List<Document> notificationDocs = doc.getList("notifications", Document.class);

		Util.editReply(event, "Removing **" + notification.replace("_", "\\_") + "** from your map notifications!");

		for (int i = 0; i < notificationDocs.size(); i++)
		{
			if (notificationDocs.get(i).getLong("userID") != user.getId().asLong())
				continue;

			Main.mongoDatabase.getCollection("services").updateOne(eq("_id", doc.getObjectId("_id")), new Document("$pull", new Document("notifications." + i + ".notifications", notification)));
			notificationDocs.get(i).getList("notifications", String.class).remove(index);
			break;
		}
	}

	@Override
	public ApplicationCommandRequest getCommandRequest()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Manage your map notifications for a server")
			.addOption(ApplicationCommandOptionData.builder()
				.name("toggle")
				.description("Add or removes a map to/from your map notifications")
				.type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("mapname")
					.description("Name of the map to add/remove, wildcard characters (*) are also supported here")
					.type(ApplicationCommandOption.Type.STRING.getValue())
					.autocomplete(true)
					.required(true)
					.build())
				.build())
			.addOption(ApplicationCommandOptionData.builder()
				.name("list")
				.description("Lists your current map notifications")
				.type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("page")
					.description("The page number to show")
					.type(ApplicationCommandOption.Type.INTEGER.getValue())
					.build())
				.build())
			.addOption(ApplicationCommandOptionData.builder()
				.name("wipe")
				.description("Wipes ALL of your map notifications")
				.type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "notify";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public List<ApplicationCommandOptionChoiceData> autoComplete(ChatInputAutoCompleteEvent event, ApplicationCommandInteractionOption option, String currentText){
		final List<ApplicationCommandOptionChoiceData> choices = new ArrayList<>();
		if (!option.getName().equals("mapname"))
			return choices;

		final String cleanText = currentText.trim().replace("_", " ").toLowerCase();
		final long now = System.currentTimeMillis();
		if (lastCached == -1 || (now - lastCached) > Util.MAP_AUTOCOMPLETE_CACHE_INVALIDATED){
			lastCached = now;
			final FindIterable<Document> serverDocs = Main.mongoDatabase.getCollection("servers").find();
			cachedMaps.clear();
			cachedPlayedMaps.clear();
			final List<String> mapFound = new ArrayList<>();
			for(Document doc : serverDocs){
				for (int i = 0; i < doc.getList("mapDatabase", Document.class).size(); i++) {
					final Document map = doc.getList("mapDatabase", Document.class).get(i);
					final String mapName = map.getString("map");
					final long lastPlayed = map.getLong("lastPlayed");

					mapFound.add(mapName);
					cachedPlayedMaps.put(Util.getMapKey(mapName), lastPlayed);
					oldestMapPlayed = Math.min(lastPlayed, oldestMapPlayed);
				}
			}
			mapFound.sort(Collections.reverseOrder(
					Comparator.comparing(map -> cachedPlayedMaps.get(Util.getMapKey(map)))
			));
			for(String map : mapFound)
				cachedMaps.put(Util.getMapKey(map), map);

		}
		if (cleanText.isEmpty()){
			for (String map : cachedMaps.values()) {
				choices.add(Util.mapChoice(map));
				if (choices.size() >= 25)
					break;
			}
			return choices;
		}

		List<ExtractedResult> matched = FuzzySearch.extractTop(
				cleanText, cachedMaps.keySet(), this::mapWeightRatio, 25, Util.FUZZY_LIMIT_CUTOFF
		);
		for(ExtractedResult mapMatch: matched){
			String key = mapMatch.getString();
			String map = cachedMaps.get(key);
			choices.add(Util.mapChoice(map));
		}
		return choices;
	}
	private int mapWeightRatio(String target, String mapMatching){
		final long mapLatest = cachedPlayedMaps.get(mapMatching);
		final long now = System.currentTimeMillis();
		final int similarity = FuzzySearch.tokenSetRatio(target, mapMatching);
		final float played = (float) (mapLatest - oldestMapPlayed) / (now - oldestMapPlayed);
		return (int) Math.round((((similarity / 100.0) * .8) + (played * 0.2)) * 100);
	}
}
