package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Notify extends AbstractCommand<MessageReceivedEvent>
{
	private static HashMap<String, String> confirmationMaps = new HashMap<>();
	private static HashMap<String, String> confirmationSuggestionMaps = new HashMap<>();
	private static HashMap<String, String> confirmationMessages = new HashMap<>();
	private static HashMap<String, Integer> listPages = new HashMap<>();
	private static HashMap<String, String> listMessages = new HashMap<>();
	private static HashMap<String, List<String>> selectionServers = new HashMap<>();
	private static HashMap<String, String> selectedServers = new HashMap<>();
	private static HashMap<String, String> selectionMessages = new HashMap<>();
	private static HashMap<String, String> messageContents = new HashMap<>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (!event.getChannel().isPrivate())
		{
			String guildID = event.getGuild().getStringID();
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
						selectedServers.put(event.getAuthor().getStringID(), serverList.get(0));
						runCmd(event.getAuthor(), event.getChannel(), serverInfoJson.getJSONObject(serverList.get(0)), serverList.get(0), event.getMessage().getContent());
					}
					else
					{
						String object = "";

						for (String objectName : serverList)
						{
							if (serverInfoJson.getJSONObject(objectName).getLong("serverTrackingChannelID") == event.getChannel().getLongID())
							{
								object = objectName;
								break;
							}
						}

						if (!object.equals(""))
						{
							selectedServers.put(event.getAuthor().getStringID(), object);
							runCmd(event.getAuthor(), event.getChannel(), serverInfoJson.getJSONObject(object), object, event.getMessage().getContent());
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

							IMessage m = Util.msg(event.getChannel(), event.getAuthor(), msg);
							waitForReaction(m.getStringID(), event.getAuthor().getStringID());
							selectionServers.put(event.getAuthor().getStringID(), serverList);
							selectionMessages.put(event.getAuthor().getStringID(), m.getStringID());
							messageContents.put(event.getAuthor().getStringID(), event.getMessage().getContent());
							Util.addNumberedReactions(m, true, serverList.size());

							ScheduledExecutorService msgDeleterPool = Executors.newScheduledThreadPool(1);

							msgDeleterPool.schedule(() ->
							{
								if (!m.isDeleted())
								{
									m.delete();
									selectionServers.remove(event.getAuthor().getStringID());
									selectionMessages.remove(event.getAuthor().getStringID());
									messageContents.remove(event.getAuthor().getStringID());
								}

								msgDeleterPool.shutdown();
							}, 120, TimeUnit.SECONDS);
						}
					}
				}
				else
				{
					Util.msg(event.getChannel(), event.getAuthor(), "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
				}
			}
			else
			{
				Util.msg(event.getChannel(), event.getAuthor(), "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
			}
		}
		else
		{
			Util.msg(event.getChannel(), event.getAuthor(), "This command can't be done in a PM, only in a guild with the server tracking service enabled");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*notify" };
	}

	private void runCmd(IUser user, IChannel channel, JSONObject object, String objectName, String messageContent) throws Exception
	{
		String argument;
		JSONObject json = null;
		File file = new File(Util.getJarLocation() + "data/services/server-tracking/" + channel.getGuild().getStringID() + "/" + user.getStringID() + ".json");
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

								IMessage m = Util.buildPage(notifications, "Notification List", 10, page, false, true, channel, user);

								listMessages.put(user.getStringID(), m.getStringID());
								waitForReaction(m.getStringID(), user.getStringID());
								listPages.put(user.getStringID(), page);
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
						IMessage m = Util.msg(channel, user, "Are you sure you would like to wipe **ALL** of your map notifications? Press  :white_check_mark:  to confirm or  :x:  to cancel");

						waitForReaction(m.getStringID(), user.getStringID());
						confirmationMaps.put(user.getStringID(), "wipe");
						confirmationMessages.put(user.getStringID(), m.getStringID());

						ArrayList<String> reactions = new ArrayList<>();

						reactions.add("white_check_mark");
						reactions.add("x");
						Util.addReactions(m, reactions);

						ScheduledExecutorService msgDeleterPool = Executors.newScheduledThreadPool(1);

						msgDeleterPool.schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								confirmationMaps.remove(user.getStringID());
								confirmationMessages.remove(user.getStringID());
							}

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
						json.put("lastName", user.getName());
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
									json.put("lastName", user.getName());
									json.getJSONObject("notifications").getJSONArray(objectName).put(argument);
									FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
								}
								else
								{
									file.createNewFile();
									json = new JSONObject();
									json.put("lastName", user.getName());
									json.put("notifications", new JSONObject().put(objectName, new JSONArray()));
									json.getJSONObject("notifications").getJSONArray(objectName).put(argument);
									FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
								}
							}
							else
							{
								String mapSuggestion = "";
								IMessage m;
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
									waitForReaction(m.getStringID(), user.getStringID());
									confirmationMaps.put(user.getStringID(), argument);
									confirmationMessages.put(user.getStringID(), m.getStringID());

									ArrayList<String> reactions = new ArrayList<>();

									reactions.add("white_check_mark");
									reactions.add("x");
									Util.addReactions(m, reactions);
								}
								else
								{
									m = Util.msg(channel, user, "The map **" + argument.replace("_", "\\_") + "** is not in my maps database (did you maybe mean **" + mapSuggestion.replace("_", "\\_") + "** instead?), please select which map you would like to choose" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  " + mapSuggestion.replace("_", "\\_") + System.lineSeparator() + "**`[2]`**  |  " + argument.replace("_", "\\_"));
									waitForReaction(m.getStringID(), user.getStringID());
									confirmationMaps.put(user.getStringID(), argument);
									confirmationSuggestionMaps.put(user.getStringID(), mapSuggestion);
									confirmationMessages.put(user.getStringID(), m.getStringID());
									Util.addNumberedReactions(m, true, 2);
								}

								ScheduledExecutorService msgDeleterPool = Executors.newScheduledThreadPool(1);

								msgDeleterPool.schedule(() ->
								{
									if (!m.isDeleted())
									{
										m.delete();
										confirmationMaps.remove(user.getStringID());
										confirmationMessages.remove(user.getStringID());
									}

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
	public void onReactionAdd(ReactionAddEvent event) throws Exception
	{
		if (confirmationMessages.containsKey(event.getUser().getStringID()) || listMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(confirmationMessages.get(event.getUser().getStringID())) || event.getMessage().getStringID().equals(listMessages.get(event.getUser().getStringID())))
			{
				String selectedServer = selectedServers.get(event.getUser().getStringID());
				String guildID = event.getGuild().getStringID();
				String fileName = "data/services/server-tracking/" + guildID + "/" + event.getUser().getStringID() + ".json";
				File file = new File(Util.getJarLocation() + fileName);
				JSONObject json = null;

				if (event.getReaction().getEmoji().toString().equals("✅"))
				{
					if (confirmationMaps.get(event.getUser().getStringID()).equals("wipe"))
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

						Util.msg(event.getChannel(), event.getUser(), "Successfully wiped all of your map notifications!");
					}
					else
					{
						Util.msg(event.getChannel(), event.getUser(), "Adding **" + confirmationMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to your map notifications!");

						if (file.exists())
						{
							json = new JSONObject(Util.getFileContents(file));
							json.put("lastName", event.getUser().getName());
							json.getJSONObject("notifications").getJSONArray(selectedServer).put(confirmationMaps.get(event.getUser().getStringID()));
							FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
						}
						else
						{
							file.createNewFile();
							json = new JSONObject();
							json.put("lastName", event.getUser().getName());
							json.put("notifications", new JSONObject().put(selectedServer, new JSONArray()));
							json.getJSONObject("notifications").getJSONArray(selectedServer).put(confirmationMaps.get(event.getUser().getStringID()));
							FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
						}
					}

					confirmationMaps.remove(event.getUser().getStringID());
					confirmationMessages.remove(event.getUser().getStringID());
					Thread.sleep(2000);
				}

				else if (event.getReaction().getEmoji().toString().equals("❌"))
				{
					confirmationMaps.remove(event.getUser().getStringID());
					confirmationMessages.remove(event.getUser().getStringID());
					Thread.sleep(2000);
				}

				else if (event.getReaction().getEmoji().toString().equals("1⃣"))
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

								if (mapNotification.equalsIgnoreCase(confirmationSuggestionMaps.get(event.getUser().getStringID())))
								{
									mapSet = true;
									index = i;
								}
							}
						}
					}

					if (!mapSet)
					{
						Util.msg(event.getChannel(), event.getUser(), "Adding **" + confirmationSuggestionMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to your map notifications!");

						if (file.exists())
						{
							json = new JSONObject(Util.getFileContents(file));

							if (json.getJSONObject("notifications").isNull(selectedServer))
							{
								json.getJSONObject("notifications").put(selectedServer, new JSONArray());
							}

							json.put("lastName", event.getUser().getName());
							json.getJSONObject("notifications").getJSONArray(selectedServer).put(confirmationSuggestionMaps.get(event.getUser().getStringID()));
							FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
						}
						else
						{
							file.createNewFile();
							json = new JSONObject();
							json.put("lastName", event.getUser().getName());
							json.put("notifications", new JSONObject().put(selectedServer, new JSONArray()));
							json.getJSONObject("notifications").getJSONArray(selectedServer).put(confirmationSuggestionMaps.get(event.getUser().getStringID()));
							FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
						}
					}
					else
					{
						Util.msg(event.getChannel(), event.getUser(), "Removing **" + confirmationSuggestionMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** from your map notifications!");
						json.put("lastName", event.getUser().getName());
						json.getJSONObject("notifications").getJSONArray(selectedServer).remove(index);
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
				}

				else if (event.getReaction().getEmoji().toString().equals("2⃣"))
				{
					Util.msg(event.getChannel(), event.getUser(), "Adding **" + confirmationMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to your map notifications!");

					if (file.exists())
					{
						json = new JSONObject(Util.getFileContents(file));
						json.put("lastName", event.getUser().getName());
						json.getJSONObject("notifications").getJSONArray(selectedServer).put(confirmationMaps.get(event.getUser().getStringID()));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
					else
					{
						file.createNewFile();
						json = new JSONObject();
						json.put("lastName", event.getUser().getName());
						json.put("notifications", new JSONObject().put(selectedServer, new JSONArray()));
						json.getJSONObject("notifications").getJSONArray(selectedServer).put(confirmationMaps.get(event.getUser().getStringID()));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
				}

				else if (event.getReaction().getEmoji().toString().equals("▶"))
				{
					ArrayList<String> notifications = new ArrayList<>();

					json = new JSONObject(Util.getFileContents(file));

					for (int i = 0; i < json.getJSONObject("notifications").getJSONArray(selectedServer).length(); i++)
					{
						notifications.add(json.getJSONObject("notifications").getJSONArray(selectedServer).getString(i));
					}

					IMessage m = Util.buildPage(notifications, "Notification List", 10, listPages.get(event.getUser().getStringID()) + 1, false, true, event.getChannel(), event.getUser());

					listMessages.put(event.getUser().getStringID(), m.getStringID());
					waitForReaction(m.getStringID(), event.getUser().getStringID());
					listPages.put(event.getUser().getStringID(), listPages.get(event.getUser().getStringID()) + 1);
				}

				else if (event.getReaction().getEmoji().toString().equals("◀"))
				{
					ArrayList<String> notifications = new ArrayList<>();

					json = new JSONObject(Util.getFileContents(file));

					for (int i = 0; i < json.getJSONObject("notifications").getJSONArray(selectedServer).length(); i++)
					{
						notifications.add(json.getJSONObject("notifications").getJSONArray(selectedServer).getString(i));
					}

					IMessage m = Util.buildPage(notifications, "Notification List", 10, listPages.get(event.getUser().getStringID()) - 1, false, true, event.getChannel(), event.getUser());

					listMessages.put(event.getUser().getStringID(), m.getStringID());
					waitForReaction(m.getStringID(), event.getUser().getStringID());
					listPages.put(event.getUser().getStringID(), listPages.get(event.getUser().getStringID()) - 1);
				}
			}
		}

		if (selectionMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(selectionMessages.get(event.getUser().getStringID())))
			{
				int i = Util.emojiToInt(event.getReaction().getEmoji().toString()) - 1;

				if (i != -1)
				{
					if (selectionServers.get(event.getUser().getStringID()).contains("server" + i))
					{
						selectedServers.put(event.getAuthor().getStringID(), "server" + i);
						runCmd(event.getUser(), event.getChannel(), new JSONObject(Util.getFileContents("data/services/server-tracking/" + event.getGuild().getStringID() + "/serverInfo.json")).getJSONObject("server" + i), "server" + i, messageContents.get(event.getUser().getStringID()));
					}
				}
			}
		}
	}
}