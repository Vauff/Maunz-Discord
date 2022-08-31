package com.vauff.maunzdiscord.commands.slash;

import com.mongodb.client.FindIterable;
import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
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
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Players extends AbstractSlashCommand<ChatInputInteractionEvent>
{
	private static HashMap<Snowflake, List<Document>> selectionServices = new HashMap<>();
	private static HashMap<Snowflake, Integer> selectionPages = new HashMap<>();

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

			if (services.size() == 0)
			{
				event.editReply("A server tracking service is not enabled in this guild yet! Please have a guild administrator use **/services add** to set one up").block();
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

				runSelection(event, user, services, 1);
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
		List<Document> services = selectionServices.get(user.getId());

		if (buttonId.equals(NEXT_BTN))
		{
			runSelection(event, user, services, page + 1);
			return;
		}
		else if (buttonId.equals(PREV_BTN))
		{
			runSelection(event, user, services, page - 1);
			return;
		}

		for (Document doc : services)
		{
			if (doc.getObjectId("_id").toString().equals(buttonId))
			{
				runCmd(event, user, doc);
				break;
			}
		}
	}

	private void runCmd(DeferrableInteractionEvent event, User user, Document doc)
	{
		StringBuilder playersList = new StringBuilder();
		Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", doc.getObjectId("serverID"))).first();

		if (!doc.getBoolean("online"))
		{
			event.editReply("The server currently appears to be offline").block();
			return;
		}

		int numberOfPlayers = serverDoc.getList("players", String.class).size();

		if (numberOfPlayers == 0)
		{
			event.editReply("There are currently no players online!").block();
			return;
		}

		boolean sizeIsSmall = numberOfPlayers <= 8;

		playersList.append("```-- Players Online: " + serverDoc.getString("playerCount") + " --" + System.lineSeparator() + System.lineSeparator());

		for (String player : serverDoc.getList("players", String.class))
		{
			if (!player.equals(""))
			{
				playersList.append("- " + player + System.lineSeparator());
			}
		}

		playersList.append("```");

		try
		{
			if (sizeIsSmall)
				event.editReply(playersList.toString()).block();
			else
				Util.msg(user.getPrivateChannel().block(), playersList.toString());
		}
		catch (ClientException e)
		{
			if (sizeIsSmall)
			{
				throw e;
			}
			else
			{
				event.editReply("An error occured when trying to PM you the players list, make sure you don't have private messages disabled in any capacity or the bot blocked").block();
				return;
			}
		}

		if (!sizeIsSmall)
			event.editReply("Sending the online player list to you in a PM!").block();
	}

	private void runSelection(DeferrableInteractionEvent event, User user, List<Document> services, int page) throws Exception
	{
		ArrayList<Button> servers = new ArrayList<>();

		for (Document doc : services)
		{
			Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", doc.getObjectId("serverID"))).first();
			servers.add(Button.primary(doc.getObjectId("_id").toString(), serverDoc.getString("name")));
		}

		buildPage(event, servers, "Select Server", 8, 2, page, 0, false);

		selectionServices.put(user.getId(), services);
		selectionPages.put(user.getId(), page);
		waitForButtonPress(event.getReply().block().getId(), user.getId());
	}

	@Override
	public ApplicationCommandRequest getCommand()
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
