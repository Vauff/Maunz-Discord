package com.vauff.maunzdiscord.commands.legacy;

import com.mongodb.client.FindIterable;
import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.objects.CommandHelp;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Notify extends AbstractLegacyCommand<MessageCreateEvent>
{
	private static HashMap<Snowflake, String> confirmationMaps = new HashMap<>();
	private static HashMap<Snowflake, String> confirmationSuggestionMaps = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> confirmationMessages = new HashMap<>();

	private static HashMap<Snowflake, Integer> listPages = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> listMessages = new HashMap<>();

	private static HashMap<Snowflake, List<Document>> selectionServices = new HashMap<>();
	private static HashMap<Snowflake, Integer> selectionPages = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> selectionMessages = new HashMap<>();

	private static HashMap<Snowflake, ObjectId> selectedServices = new HashMap<>();
	private static HashMap<Snowflake, String> messageContents = new HashMap<>();

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
				Util.msg(channel, author, "A server tracking service is not enabled in this guild yet! Please have a guild administrator use **/services add** to set one up");
			}
			else if (services.size() == 1)
			{
				selectedServices.put(author.getId(), services.get(0).getObjectId("_id"));
				runCmd(author, channel, services.get(0), event.getMessage().getContent());
			}
			else
			{
				for (Document doc : services)
				{
					if (doc.getLong("channelID") == channel.getId().asLong() && !Util.isMultiTrackingChannel(guildID, channel.getId().asLong()))
					{
						selectedServices.put(author.getId(), doc.getObjectId("_id"));
						runCmd(author, channel, doc, event.getMessage().getContent());
						return;
					}
				}

				messageContents.put(author.getId(), event.getMessage().getContent());
				runSelection(author, channel, services, 1);
			}
		}
		else
		{
			Util.msg(channel, author, "This command can't be done in a PM, only in a guild with the server tracking service enabled");
		}
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event, Message message) throws Exception
	{
		String emoji = event.getEmoji().asUnicodeEmoji().get().getRaw();
		User user = event.getUser().block();

		// Server selection and pagination
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
					selectedServices.put(user.getId(), selectionServices.get(user.getId()).get(i).getObjectId("_id"));
					runCmd(user, event.getChannel().block(), selectionServices.get(user.getId()).get(i), messageContents.get(user.getId()));
				}
			}
		}

		// Confirmations
		else if (confirmationMessages.containsKey(user.getId()) && message.getId().equals(confirmationMessages.get(user.getId())))
		{
			Document serviceDoc = Main.mongoDatabase.getCollection("services").find(eq("_id", selectedServices.get(user.getId()))).first();
			List<Document> notificationDocs = serviceDoc.getList("notifications", Document.class);

			if (emoji.equals("✅"))
			{
				if (confirmationMaps.get(event.getUser().block().getId()).equals("wipe"))
				{
					for (int i = 0; i < notificationDocs.size(); i++)
					{
						long userID = notificationDocs.get(i).getLong("userID");

						if (userID != user.getId().asLong())
							continue;

						Main.mongoDatabase.getCollection("services").updateOne(eq("_id", serviceDoc.getObjectId("_id")), new Document("$unset", new Document("notifications." + i, 1)));
						Main.mongoDatabase.getCollection("services").updateOne(eq("_id", serviceDoc.getObjectId("_id")), new Document("$pull", new Document("notifications", null)));
						Util.msg(event.getChannel().block(), user, "Successfully wiped all of your map notifications!");
						break;
					}
				}
				else
				{
					addNotification(serviceDoc, user, event.getChannel().block(), confirmationMaps.get(user.getId()));
				}

				confirmationMaps.remove(user.getId());
				confirmationMessages.remove(user.getId());
			}

			else if (emoji.equals("1⃣"))
			{
				boolean mapSet = false;
				int index = 0;

				for (int i = 0; i < notificationDocs.size(); i++)
				{
					if (notificationDocs.get(i).getLong("userID") != user.getId().asLong())
						continue;

					List<String> notifications = notificationDocs.get(i).getList("notifications", String.class);

					for (int j = 0; j < notifications.size(); j++)
					{
						String notification = notifications.get(j);

						if (notification.equalsIgnoreCase(confirmationSuggestionMaps.get(user.getId())))
						{
							mapSet = true;
							index = j;
							break;
						}
					}
				}

				if (!mapSet)
				{
					addNotification(serviceDoc, user, event.getChannel().block(), confirmationSuggestionMaps.get(user.getId()));
				}
				else
				{
					removeNotification(serviceDoc, user, event.getChannel().block(), confirmationSuggestionMaps.get(user.getId()), index);
				}
			}

			else if (emoji.equals("2⃣"))
			{
				addNotification(serviceDoc, user, event.getChannel().block(), confirmationMaps.get(user.getId()));
			}
		}

		// Notification list pagination
		else if (listMessages.containsKey(user.getId()) && message.getId().equals(listMessages.get(user.getId())))
		{
			int page = -1;

			if (emoji.equals("▶"))
			{
				page = listPages.get(event.getUser().block().getId()) + 1;
			}
			else if (emoji.equals("◀"))
			{
				page = listPages.get(event.getUser().block().getId()) - 1;
			}

			List<String> notifications = new ArrayList<>();
			List<Document> notificationDocs = Main.mongoDatabase.getCollection("services").find(eq("_id", selectedServices.get(user.getId()))).first().getList("notifications", Document.class);

			for (int i = 0; i < notificationDocs.size(); i++)
			{
				if (notificationDocs.get(i).getLong("userID") != user.getId().asLong())
					continue;

				notifications = notificationDocs.get(i).getList("notifications", String.class);
				break;
			}

			Message m = Util.buildPage(notifications, "Notification List", 10, page, 0, true, false, false, event.getChannel().block(), user);

			listMessages.put(user.getId(), m.getId());
			waitForReaction(m.getId(), user.getId());
			listPages.put(user.getId(), page);
		}
	}

	private void runCmd(User user, MessageChannel channel, Document doc, String messageContent)
	{
		Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", doc.getObjectId("serverID"))).first();
		List<Document> notificationDocs = doc.getList("notifications", Document.class);
		String argument;
		String[] args = messageContent.split(" ");

		if (args.length == 1)
		{
			Util.msg(channel, user, "You need to specify an argument! See **" + Main.prefix + "help notify**");
		}
		else
		{
			if (doc.getBoolean("mapCharacterLimit"))
			{
				argument = StringUtils.substring(messageContent.split(" ")[1], 0, 31);
			}
			else
			{
				argument = messageContent.split(" ")[1];
			}

			if (argument.equals(""))
			{
				Util.msg(channel, user, "Please keep to one space between arguments to prevent breakage");
			}
			else
			{
				boolean hasNotifications = false;

				for (int i = 0; i < notificationDocs.size(); i++)
				{
					if (notificationDocs.get(i).getLong("userID") != user.getId().asLong() || notificationDocs.get(i).getList("notifications", String.class).size() == 0)
						continue;

					hasNotifications = true;
					break;
				}
				if (argument.equalsIgnoreCase("list"))
				{
					if (!hasNotifications)
					{
						Util.msg(channel, user, "You do not have any map notifications set! Use **" + Main.prefix + "notify <mapname>** to add one");
						return;
					}

					if (args.length == 2 || NumberUtils.isCreatable(args[2]))
					{
						int page;

						if (args.length == 2)
						{
							page = 1;
						}
						else
						{
							page = Integer.parseInt(args[2]);
						}

						List<String> notifications = new ArrayList<>();

						for (int i = 0; i < doc.getList("notifications", Document.class).size(); i++)
						{
							if (doc.getList("notifications", Document.class).get(i).getLong("userID") != user.getId().asLong())
								continue;

							notifications = doc.getList("notifications", Document.class).get(i).getList("notifications", String.class);
							break;
						}

						Message m = Util.buildPage(notifications, "Notification List", 10, page, 0, true, false, false, channel, user);

						listMessages.put(user.getId(), m.getId());
						waitForReaction(m.getId(), user.getId());
						listPages.put(user.getId(), page);
					}
					else
					{
						Util.msg(channel, user, "Page numbers need to be numerical!");
					}
				}
				else if (argument.equalsIgnoreCase("wipe"))
				{
					if (!hasNotifications)
					{
						Util.msg(channel, user, "You don't have any map notifications to wipe!");
						return;
					}

					Message m = Util.msg(channel, user, "Are you sure you would like to wipe **ALL** of your map notifications? Press  :white_check_mark:  to confirm or  :x:  to cancel");

					waitForReaction(m.getId(), user.getId());
					confirmationMaps.put(user.getId(), "wipe");
					confirmationMessages.put(user.getId(), m.getId());

					ArrayList<String> reactions = new ArrayList<>();

					reactions.add("\u2705");
					reactions.add("\u274C");
					Util.addReactions(m, reactions);

					ScheduledExecutorService msgDeleterPool = Executors.newScheduledThreadPool(1);

					msgDeleterPool.schedule(() ->
					{
						msgDeleterPool.shutdown();
						m.delete().block();
					}, 120, TimeUnit.SECONDS);
				}
				else
				{
					boolean mapSet = false;
					boolean mapExists = false;
					int index = 0;

					for (int i = 0; i < notificationDocs.size(); i++)
					{
						if (notificationDocs.get(i).getLong("userID") != user.getId().asLong())
							continue;

						List<String> notifications = notificationDocs.get(i).getList("notifications", String.class);

						for (int j = 0; j < notifications.size(); j++)
						{
							String notification = notifications.get(j);

							if (notification.equalsIgnoreCase(argument))
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

						if (map.equalsIgnoreCase(argument) || argument.contains("*"))
						{
							mapExists = true;
							break;
						}
					}

					if (mapSet)
					{
						removeNotification(doc, user, channel, argument, index);
					}
					else
					{
						if (mapExists)
						{
							addNotification(doc, user, channel, argument);
						}
						else
						{
							String mapSuggestion = "";
							Message m;
							ArrayList<Long> mapDatabaseTimestamps = new ArrayList<>();
							ArrayList<String> mapDatabase = new ArrayList<>();

							for (int i = 0; i < serverDoc.getList("mapDatabase", Document.class).size(); i++)
							{
								mapDatabaseTimestamps.add(serverDoc.getList("mapDatabase", Document.class).get(i).getLong("lastPlayed"));
							}

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

								if (StringUtils.containsIgnoreCase(map, argument))
								{
									mapSuggestion = map;
									break;
								}
							}

							if (mapSuggestion.equals(""))
							{
								m = Util.msg(channel, user, "The map **" + argument.replace("_", "\\_") + "** is not in my maps database, are you sure you'd like to add it? Press  :white_check_mark:  to confirm or  :x:  to cancel");
								waitForReaction(m.getId(), user.getId());
								confirmationMaps.put(user.getId(), argument);
								confirmationMessages.put(user.getId(), m.getId());

								ArrayList<String> reactions = new ArrayList<>();

								reactions.add("\u2705");
								reactions.add("\u274C");
								Util.addReactions(m, reactions);
							}
							else
							{
								m = Util.msg(channel, user, "The map **" + argument.replace("_", "\\_") + "** is not in my maps database (did you maybe mean **" + mapSuggestion.replace("_", "\\_") + "** instead?), please select which map you would like to choose" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  " + mapSuggestion.replace("_", "\\_") + System.lineSeparator() + "**`[2]`**  |  " + argument.replace("_", "\\_"));
								waitForReaction(m.getId(), user.getId());
								confirmationMaps.put(user.getId(), argument);
								confirmationSuggestionMaps.put(user.getId(), mapSuggestion);
								confirmationMessages.put(user.getId(), m.getId());
								Util.addNumberedReactions(m, true, 2);
							}

							ScheduledExecutorService msgDeleterPool = Executors.newScheduledThreadPool(1);

							msgDeleterPool.schedule(() ->
							{
								msgDeleterPool.shutdown();
								m.delete().block();
							}, 120, TimeUnit.SECONDS);
						}
					}
				}
			}
		}
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

	private void addNotification(Document serviceDoc, User user, MessageChannel channel, String notification)
	{
		List<Document> notificationDocs = serviceDoc.getList("notifications", Document.class);

		Util.msg(channel, user, "Adding **" + notification.replace("_", "\\_") + "** to your map notifications!");

		for (int i = 0; i < notificationDocs.size(); i++)
		{
			if (notificationDocs.get(i).getLong("userID") != user.getId().asLong())
				continue;

			Main.mongoDatabase.getCollection("services").updateOne(eq("_id", serviceDoc.getObjectId("_id")), new Document("$push", new Document("notifications." + i + ".notifications", notification.replace("﻿", ""))));
			return;
		}

		Document notificationDoc = new Document();
		ArrayList<String> notifications = new ArrayList<>();

		notificationDoc.put("userID", user.getId().asLong());
		notifications.add(notification.replace("﻿", ""));
		notificationDoc.put("notifications", notifications);

		Main.mongoDatabase.getCollection("services").updateOne(eq("_id", serviceDoc.getObjectId("_id")), new Document("$push", new Document("notifications", notificationDoc)));
	}

	private void removeNotification(Document serviceDoc, User user, MessageChannel channel, String notification, int index)
	{
		List<Document> notificationDocs = serviceDoc.getList("notifications", Document.class);

		Util.msg(channel, user, "Removing **" + notification.replace("_", "\\_") + "** from your map notifications!");

		for (int i = 0; i < notificationDocs.size(); i++)
		{
			if (notificationDocs.get(i).getLong("userID") != user.getId().asLong())
				continue;

			Main.mongoDatabase.getCollection("services").updateOne(eq("_id", serviceDoc.getObjectId("_id")), new Document("$pull", new Document("notifications." + i + ".notifications", notification)));
			notificationDocs.get(i).getList("notifications", String.class).remove(index);
			break;
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "notify" };
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		CommandHelp[] commandHelps = new CommandHelp[3];

		commandHelps[0] = new CommandHelp("list [page]", "Lists your current map notifications.");
		commandHelps[1] = new CommandHelp("wipe", "Wipes ALL of your map notifications.");
		commandHelps[2] = new CommandHelp("<mapname>", "Adds or removes a map to/from your map notifications, wildcard characters are also supported here.");

		return commandHelps;
	}
}