package com.vauff.maunzdiscord.commands;

import com.mongodb.client.FindIterable;
import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.http.client.ClientException;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Players extends AbstractCommand<ChatInputInteractionEvent>
{
	private final static HashMap<Snowflake, List<Document>> selectionServices = new HashMap<>();
	private final static HashMap<Snowflake, Integer> selectionPages = new HashMap<>();

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

		if (services.size() == 0)
		{
			Util.editReply(event, "Server tracking is not enabled in this guild yet! Please have a guild administrator use **/servers add** to set one up");
		}
		else if (services.size() == 1)
		{
			runCmd(event, user, services.get(0));
		}
		else
		{
			for (Document doc : services)
			{
				if (doc.getLong("channelID") == channel.getId().asLong() && !Util.isMultiTrackingChannel(guildID, channel.getId().asLong()))
				{
					runCmd(event, user, doc);
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
				runCmd(event, user, doc);
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

	private void runCmd(DeferrableInteractionEvent event, User user, Document doc)
	{
		StringBuilder playersList = new StringBuilder();
		Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", doc.getObjectId("serverID"))).first();

		if (!doc.getBoolean("online"))
		{
			Util.editReply(event, "The server currently appears to be offline");
			return;
		}

		int numberOfPlayers = serverDoc.getList("players", String.class).size();

		if (numberOfPlayers == 0)
		{
			Util.editReply(event, "There are currently no players online!");
			return;
		}

		if (numberOfPlayers == 1 && serverDoc.getList("players", String.class).get(0).equals("SERVER_UPDATEPLAYERS_FAILED"))
		{
			Util.editReply(event, "The server failed to return player information, please try again later");
			return;
		}

		boolean sizeIsSmall = numberOfPlayers <= 8;

		playersList.append("```-- Players Online: " + serverDoc.getString("playerCount") + " --" + System.lineSeparator() + System.lineSeparator());

		for (String player : serverDoc.getList("players", String.class))
		{
			if (!player.equals(""))
				playersList.append("- " + player + System.lineSeparator());
		}

		playersList.append("```");

		try
		{
			if (sizeIsSmall)
				Util.editReply(event, playersList.toString());
			else
				Util.msg(user.getPrivateChannel().block(), false, playersList.toString());
		}
		catch (ClientException e)
		{
			if (sizeIsSmall)
			{
				throw e;
			}
			else
			{
				Util.editReply(event, "An error occured when trying to PM you the players list, make sure you don't have private messages disabled in any capacity or the bot blocked");
				return;
			}
		}

		if (!sizeIsSmall)
			Util.editReply(event, "Sending the online player list to you in a PM!");
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
			.description("Lists the current players online on a server")
			.build();
	}

	@Override
	public String getName()
	{
		return "players";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}
