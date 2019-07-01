package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Notify extends AbstractCommand<MessageCreateEvent>
{
	private static HashMap<Snowflake, String> confirmationMaps = new HashMap<>();
	private static HashMap<Snowflake, String> confirmationSuggestionMaps = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> confirmationMessages = new HashMap<>();
	private static HashMap<Snowflake, Integer> listPages = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> listMessages = new HashMap<>();
	private static HashMap<Snowflake, List<String>> selectionServers = new HashMap<>();
	private static HashMap<Snowflake, String> selectedServers = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> selectionMessages = new HashMap<>();
	private static HashMap<Snowflake, String> messageContents = new HashMap<>();

	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		if (!(channel instanceof PrivateChannel))
		{
			String guildID = event.getGuild().block().getId().asString();
			File serverInfoFile = new File(Util.getJarLocation() + "data/services/server-tracking/" + guildID + "/serverInfo.json");

			if (serverInfoFile.exists())
			{
				JSONObject serverInfoJson = new JSONObject(Util.getFileContents(serverInfoFile));
				int serverNumber = 0;
				List<String> serverList = new ArrayList<>();

				while (true)
				{
					JSONObject object;

					try
					{
						object = serverInfoJson.getJSONObject("server" + serverNumber);
					}
					catch (JSONException e)
					{
						break;
					}

					if (object.getBoolean("enabled"))
					{
						serverList.add("server" + serverNumber);
					}

					serverNumber++;
				}

				if (serverList.size() != 0)
				{
					if (serverList.size() == 1)
					{
						selectedServers.put(author.getId(), serverList.get(0));
						runCmd(author, channel, serverInfoJson.getJSONObject(serverList.get(0)), serverList.get(0), event.getMessage().getContent().get());
					}
					else
					{
						String object = "";

						for (String objectName : serverList)
						{
							if (serverInfoJson.getJSONObject(objectName).getLong("serverTrackingChannelID") == channel.getId().asLong())
							{
								object = objectName;
								break;
							}
						}

						if (!object.equals(""))
						{
							selectedServers.put(author.getId(), object);
							runCmd(author, channel, serverInfoJson.getJSONObject(object), object, event.getMessage().getContent().get());
						}
						else
						{
							String msg = "Please select which server to manage your notifications for" + System.lineSeparator();
							int i = 1;

							for (String serverObject : serverList)
							{
								msg += System.lineSeparator() + "**`[" + i + "]`**  |  " + serverInfoJson.getJSONObject(serverObject).getString("serverName");
								i++;
							}

							Message m = Util.msg(channel, author, msg);
							waitForReaction(m.getId(), author.getId());
							selectionServers.put(author.getId(), serverList);
							selectionMessages.put(author.getId(), m.getId());
							messageContents.put(author.getId(), event.getMessage().getContent().get());
							Util.addNumberedReactions(m, true, serverList.size());

							ScheduledExecutorService msgDeleterPool = Executors.newScheduledThreadPool(1);

							msgDeleterPool.schedule(() ->
							{
								m.delete();
								selectionServers.remove(author.getId());
								selectionMessages.remove(author.getId());
								messageContents.remove(author.getId());
								msgDeleterPool.shutdown();
							}, 120, TimeUnit.SECONDS);
						}
					}
				}
				else
				{
					Util.msg(channel, author, "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
				}
			}
			else
			{
				Util.msg(channel, author, "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
			}
		}
		else
		{
			Util.msg(channel, author, "This command can't be done in a PM, only in a guild with the server tracking service enabled");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*notify" };
	}

	private void runCmd(User user, MessageChannel channel, JSONObject object, String objectName, String messageContent) throws Exception
	{
		String argument;
		JSONObject json = null;
		File file = new File(Util.getJarLocation() + "data/services/server-tracking/" + ((GuildChannel) channel).getGuild().block().getId().asString() + "/" + user.getId().asString() + ".json");
		String[] args = messageContent.split(" ");

		if (file.exists())
		{
			json = new JSONObject(Util.getFileContents(file));

			if (Util.getFileContents(file).contains("﻿"))
			{
				FileUtils.writeStringToFile(file, Util.getFileContents(file).replace("﻿", ""), "UTF-8");
			}
		}

		if (args.length == 1)
		{
			Util.msg(channel, user, "You need to specify an argument! See **\\*help notify**");
		}
		else
		{
			if (object.getBoolean("mapCharacterLimit"))
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
				if (argument.equalsIgnoreCase("list"))
				{
					if (!file.exists())
					{
						Util.msg(channel, user, "You do not have any map notifications set! Use **\\*notify <mapname>** to add one");
					}
					else
					{
						if (json.getJSONObject("notifications").has(objectName) && json.getJSONObject("notifications").getJSONArray(objectName).length() != 0)
						{
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

								ArrayList<String> notifications = new ArrayList<>();

								for (int i = 0; i < json.getJSONObject("notifications").getJSONArray(objectName).length(); i++)
								{
									notifications.add(json.getJSONObject("notifications").getJSONArray(objectName).getString(i));
								}

								Message m = Util.buildPage(notifications, "Notification List", 10, page, false, true, channel, user);

								listMessages.put(user.getId(), m.getId());
								waitForReaction(m.getId(), user.getId());
								listPages.put(user.getId(), page);
							}
							else
							{
								Util.msg(channel, user, "Page numbers need to be numerical!");
							}
						}
						else
						{
							Util.msg(channel, user, "You do not have any map notifications set! Use **\\*notify <mapname>** to add one");
						}
					}
				}
				else if (argument.equalsIgnoreCase("wipe"))
				{
					if (!file.exists())
					{
						Util.msg(channel, user, "You don't have any map notifications to wipe!");
					}
					else if (file.exists() && new JSONObject(Util.getFileContents(file)).getJSONObject("notifications").isNull(objectName))
					{
						Util.msg(channel, user, "You don't have any map notifications to wipe!");
					}
					else
					{
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
							m.delete();
							confirmationMaps.remove(user.getId());
							confirmationMessages.remove(user.getId());
							msgDeleterPool.shutdown();
						}, 120, TimeUnit.SECONDS);
					}
				}
				else
				{
					boolean mapSet = false;
					boolean mapExists = false;
					int index = 0;

					if (file.exists() && !json.getJSONObject("notifications").isNull(objectName))
					{
						for (int i = 0; i < json.getJSONObject("notifications").getJSONArray(objectName).length(); i++)
						{
							String mapNotification = json.getJSONObject("notifications").getJSONArray(objectName).getString(i);

							if (mapNotification.equalsIgnoreCase(argument))
							{
								mapSet = true;
								index = i;
								break;
							}
						}
					}

					for (int i = 0; i < object.getJSONArray("mapDatabase").length(); i++)
					{
						String map = object.getJSONArray("mapDatabase").getJSONObject(i).getString("mapName");

						if (map.equalsIgnoreCase(argument))
						{
							mapExists = true;
							break;
						}
					}

					if (mapSet)
					{
						Util.msg(channel, user, "Removing **" + argument.replace("_", "\\_") + "** from your map notifications!");
						json.put("lastName", user.getUsername());
						json.getJSONObject("notifications").getJSONArray(objectName).remove(index);
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
					else
					{
						if (!argument.contains("﻿"))
						{
							if (mapExists)
							{
								Util.msg(channel, user, "Adding **" + argument.replace("_", "\\_") + "** to your map notifications!");

								if (file.exists())
								{
									json = new JSONObject(Util.getFileContents(file));
									json.put("lastName", user.getUsername());
									json.getJSONObject("notifications").getJSONArray(objectName).put(argument);
									FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
								}
								else
								{
									file.createNewFile();
									json = new JSONObject();
									json.put("lastName", user.getUsername());
									json.put("notifications", new JSONObject().put(objectName, new JSONArray()));
									json.getJSONObject("notifications").getJSONArray(objectName).put(argument);
									FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
								}
							}
							else
							{
								String mapSuggestion = "";
								Message m;
								ArrayList<String> mapDatabase = new ArrayList<>();

								for (int i = 0; i < object.getJSONArray("mapDatabase").length(); i++)
								{
									mapDatabase.add(object.getJSONArray("mapDatabase").getJSONObject(i).getString("mapName"));
								}

								Collections.sort(mapDatabase, String.CASE_INSENSITIVE_ORDER);
								Collections.reverse(mapDatabase);

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
									m.delete();
									confirmationMaps.remove(user.getId());
									confirmationMessages.remove(user.getId());
									msgDeleterPool.shutdown();
								}, 120, TimeUnit.SECONDS);
							}
						}
						else
						{
							Util.msg(channel, user, "Do not include invisible characters with your map name!");
						}
					}
				}
			}
		}
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event, Message message) throws Exception
	{
		if ((confirmationMessages.containsKey(event.getUser().block().getId()) || listMessages.containsKey(event.getUser().block().getId())) && (message.getId().equals(confirmationMessages.get(event.getUser().block().getId())) || message.getId().equals(listMessages.get(event.getUser().block().getId()))))
		{
			String selectedServer = selectedServers.get(event.getUser().block().getId());
			String guildID = event.getGuild().block().getId().asString();
			String fileName = "data/services/server-tracking/" + guildID + "/" + event.getUser().block().getId().asString() + ".json";
			File file = new File(Util.getJarLocation() + fileName);
			JSONObject json = null;

			if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("✅"))
			{
				if (confirmationMaps.get(event.getUser().block().getId()).equals("wipe"))
				{
					json = new JSONObject(Util.getFileContents(file));

					json.getJSONObject("notifications").remove(selectedServer);

					if (json.getJSONObject("notifications").length() == 0)
					{
						FileUtils.forceDelete(file);
					}
					else
					{
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}

					Util.msg(event.getChannel().block(), event.getUser().block(), "Successfully wiped all of your map notifications!");
				}
				else
				{
					Util.msg(event.getChannel().block(), event.getUser().block(), "Adding **" + confirmationMaps.get(event.getUser().block().getId()).replace("_", "\\_") + "** to your map notifications!");

					if (file.exists())
					{
						json = new JSONObject(Util.getFileContents(file));
						json.put("lastName", event.getUser().block().getUsername());
						json.getJSONObject("notifications").getJSONArray(selectedServer).put(confirmationMaps.get(event.getUser().block().getId()));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
					else
					{
						file.createNewFile();
						json = new JSONObject();
						json.put("lastName", event.getUser().block().getUsername());
						json.put("notifications", new JSONObject().put(selectedServer, new JSONArray()));
						json.getJSONObject("notifications").getJSONArray(selectedServer).put(confirmationMaps.get(event.getUser().block().getId()));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
				}

				confirmationMaps.remove(event.getUser().block().getId());
				confirmationMessages.remove(event.getUser().block().getId());
				Thread.sleep(2000);
			}

			else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("❌"))
			{
				confirmationMaps.remove(event.getUser().block().getId());
				confirmationMessages.remove(event.getUser().block().getId());
				Thread.sleep(2000);
			}

			else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("1⃣"))
			{
				boolean mapSet = false;
				int index = 0;

				if (file.exists())
				{
					json = new JSONObject(Util.getFileContents(file));

					if (!json.getJSONObject("notifications").isNull(selectedServer))
					{
						for (int i = 0; i < json.getJSONObject("notifications").getJSONArray(selectedServer).length(); i++)
						{
							String mapNotification = json.getJSONObject("notifications").getJSONArray(selectedServer).getString(i);

							if (mapNotification.equalsIgnoreCase(confirmationSuggestionMaps.get(event.getUser().block().getId())))
							{
								mapSet = true;
								index = i;
							}
						}
					}
				}

				if (!mapSet)
				{
					Util.msg(event.getChannel().block(), event.getUser().block(), "Adding **" + confirmationSuggestionMaps.get(event.getUser().block().getId()).replace("_", "\\_") + "** to your map notifications!");

					if (file.exists())
					{
						json = new JSONObject(Util.getFileContents(file));

						if (json.getJSONObject("notifications").isNull(selectedServer))
						{
							json.getJSONObject("notifications").put(selectedServer, new JSONArray());
						}

						json.put("lastName", event.getUser().block().getUsername());
						json.getJSONObject("notifications").getJSONArray(selectedServer).put(confirmationSuggestionMaps.get(event.getUser().block().getId()));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
					else
					{
						file.createNewFile();
						json = new JSONObject();
						json.put("lastName", event.getUser().block().getUsername());
						json.put("notifications", new JSONObject().put(selectedServer, new JSONArray()));
						json.getJSONObject("notifications").getJSONArray(selectedServer).put(confirmationSuggestionMaps.get(event.getUser().block().getId()));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
				}
				else
				{
					Util.msg(event.getChannel().block(), event.getUser().block(), "Removing **" + confirmationSuggestionMaps.get(event.getUser().block().getId()).replace("_", "\\_") + "** from your map notifications!");
					json.put("lastName", event.getUser().block().getUsername());
					json.getJSONObject("notifications").getJSONArray(selectedServer).remove(index);
					FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
				}
			}

			else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("2⃣"))
			{
				Util.msg(event.getChannel().block(), event.getUser().block(), "Adding **" + confirmationMaps.get(event.getUser().block().getId()).replace("_", "\\_") + "** to your map notifications!");

				if (file.exists())
				{
					json = new JSONObject(Util.getFileContents(file));
					json.put("lastName", event.getUser().block().getUsername());
					json.getJSONObject("notifications").getJSONArray(selectedServer).put(confirmationMaps.get(event.getUser().block().getId()));
					FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
				}
				else
				{
					file.createNewFile();
					json = new JSONObject();
					json.put("lastName", event.getUser().block().getUsername());
					json.put("notifications", new JSONObject().put(selectedServer, new JSONArray()));
					json.getJSONObject("notifications").getJSONArray(selectedServer).put(confirmationMaps.get(event.getUser().block().getId()));
					FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
				}
			}

			else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("▶"))
			{
				ArrayList<String> notifications = new ArrayList<>();

				json = new JSONObject(Util.getFileContents(file));

				for (int i = 0; i < json.getJSONObject("notifications").getJSONArray(selectedServer).length(); i++)
				{
					notifications.add(json.getJSONObject("notifications").getJSONArray(selectedServer).getString(i));
				}

				Message m = Util.buildPage(notifications, "Notification List", 10, listPages.get(event.getUser().block().getId()) + 1, false, true, event.getChannel().block(), event.getUser().block());

				listMessages.put(event.getUser().block().getId(), m.getId());
				waitForReaction(m.getId(), event.getUser().block().getId());
				listPages.put(event.getUser().block().getId(), listPages.get(event.getUser().block().getId()) + 1);
			}

			else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("◀"))
			{
				ArrayList<String> notifications = new ArrayList<>();

				json = new JSONObject(Util.getFileContents(file));

				for (int i = 0; i < json.getJSONObject("notifications").getJSONArray(selectedServer).length(); i++)
				{
					notifications.add(json.getJSONObject("notifications").getJSONArray(selectedServer).getString(i));
				}

				Message m = Util.buildPage(notifications, "Notification List", 10, listPages.get(event.getUser().block().getId()) - 1, false, true, event.getChannel().block(), event.getUser().block());

				listMessages.put(event.getUser().block().getId(), m.getId());
				waitForReaction(m.getId(), event.getUser().block().getId());
				listPages.put(event.getUser().block().getId(), listPages.get(event.getUser().block().getId()) - 1);
			}
		}

		if (selectionMessages.containsKey(event.getUser().block().getId()) && message.getId().equals(selectionMessages.get(event.getUser().block().getId())))
		{
			int i = Util.emojiToInt(event.getEmoji().asUnicodeEmoji().get().getRaw()) - 1;

			if (i != -1)
			{
				if (selectionServers.get(event.getUser().block().getId()).contains("server" + i))
				{
					selectedServers.put(event.getUser().block().getId(), "server" + i);
					runCmd(event.getUser().block(), event.getChannel().block(), new JSONObject(Util.getFileContents("data/services/server-tracking/" + event.getGuild().block().getId().asString() + "/serverInfo.json")).getJSONObject("server" + i), "server" + i, messageContents.get(event.getUser().block().getId()));
				}
			}
		}
	}
}