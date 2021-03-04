package com.vauff.maunzdiscord.commands.legacy;

import com.mongodb.client.FindIterable;
import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
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
import discord4j.rest.http.client.ClientException;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Players extends AbstractLegacyCommand<MessageCreateEvent>
{
	private static HashMap<Snowflake, List<Document>> selectionServices = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> selectionMessages = new HashMap<>();
	private static HashMap<Snowflake, Integer> selectionPages = new HashMap<>();

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
				Util.msg(channel, author, "A server tracking service is not enabled in this guild yet! Please have a guild administrator run **" + Main.prefix + "services** to set one up");
			}
			else if (services.size() == 1)
			{
				runCmd(author, channel, services.get(0));
			}
			else
			{
				for (Document doc : services)
				{
					if (doc.getLong("channelID") == channel.getId().asLong() && !Util.isMultiTrackingChannel(guildID, channel.getId().asLong()))
					{
						runCmd(author, channel, doc);
						return;
					}
				}

				runSelection(author, channel, services, 1);
			}
		}
		else
		{
			Util.msg(channel, author, "This command can't be done in a PM, only in a guild with the server tracking service enabled");
		}
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event, Message message)
	{
		String emoji = event.getEmoji().asUnicodeEmoji().get().getRaw();
		User user = event.getUser().block();

		if (selectionMessages.containsKey(user.getId()) && message.getId().equals(selectionMessages.get(user.getId())))
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
				if (selectionServices.get(user.getId()).size() >= i)
				{
					runCmd(user, event.getChannel().block(), selectionServices.get(user.getId()).get(i));
				}
			}
		}
	}

	private void runCmd(User user, MessageChannel channel, Document doc)
	{
		StringBuilder playersList = new StringBuilder();
		Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", doc.getObjectId("serverID"))).first();

		if (!doc.getBoolean("online"))
		{
			Util.msg(channel, user, "The server currently appears to be offline");
			return;
		}

		int numberOfPlayers = serverDoc.getList("players", String.class).size();

		if (numberOfPlayers == 0)
		{
			Util.msg(channel, user, "There are currently no players online!");
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
			Util.msg((!sizeIsSmall ? user.getPrivateChannel().block() : channel), user, playersList.toString());
		}
		catch (ClientException e)
		{
			if (!sizeIsSmall)
			{
				Util.msg(channel, user, "An error occured when trying to PM you the players list, make sure you don't have private messages disabled in any capacity or the bot blocked");
				return;
			}
			else
			{
				throw e;
			}
		}

		if (!sizeIsSmall)
			Util.msg(channel, user, "Sending the online player list to you in a PM!");
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
			msgDeleterPool.shutdown();
			m.delete().block();
		}, 120, TimeUnit.SECONDS);
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "players" };
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		return new CommandHelp[] { new CommandHelp("", "Lists the current players online on a server.") };
	}
}