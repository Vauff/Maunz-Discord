package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Notify extends AbstractCommand<MessageReceivedEvent>
{
	private static HashMap<String, String> confirmationMaps = new HashMap<>();
	private static HashMap<String, String> confirmationSuggestionMaps = new HashMap<>();
	private static HashMap<String, String> confirmationMessages = new HashMap<>();
	private static HashMap<String, Integer> listPages = new HashMap<>();
	private static HashMap<String, String> listMessages = new HashMap<>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");
		String argument;
		String guildID = event.getGuild().getStringID();
		File file = new File(Util.getJarLocation() + "data/services/server-tracking/" + guildID + "/" + event.getAuthor().getStringID() + ".json");
		File serverInfoFile = new File(Util.getJarLocation() + "data/services/server-tracking/" + guildID + "/serverInfo.json");
		JSONObject json = null;

		if (serverInfoFile.exists())
		{
			JSONObject serverInfoJson = new JSONObject(Util.getFileContents(serverInfoFile));

			if (serverInfoJson.getBoolean("enabled"))
			{
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
					Util.msg(event.getChannel(), event.getAuthor(), "You need to specify an argument! See **\\*help notify**");
				}
				else
				{
					if (serverInfoJson.getBoolean("mapCharacterLimit"))
					{
						argument = StringUtils.substring(event.getMessage().getContent().split(" ")[1], 0, 31);
					}
					else
					{
						argument = event.getMessage().getContent().split(" ")[1];
					}

					if (argument.equals(""))
					{
						Util.msg(event.getChannel(), event.getAuthor(), "Please keep to one space between arguments to prevent breakage");
					}
					else
					{
						if (argument.equalsIgnoreCase("list"))
						{
							if (!file.exists())
							{
								Util.msg(event.getChannel(), event.getAuthor(), "You do not have any map notifications set! Use **\\*notify <mapname>** to add one");
							}
							else
							{
								if (json.getJSONArray("notifications").length() != 0)
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

										for (int i = 0; i < json.getJSONArray("notifications").length(); i++)
										{
											notifications.add(json.getJSONArray("notifications").getString(i));
										}

										IMessage m = Util.buildPage(notifications, "Notification List", 10, page, false, true, event.getChannel(), event.getAuthor());

										listMessages.put(event.getAuthor().getStringID(), m.getStringID());
										waitForReaction(m.getStringID(), event.getAuthor().getStringID());
										listPages.put(event.getAuthor().getStringID(), page);
									}
									else
									{
										Util.msg(event.getChannel(), event.getAuthor(), "Page numbers need to be numerical!");
									}
								}
								else
								{
									Util.msg(event.getChannel(), event.getAuthor(), "\"You do not have any map notifications set! Use **\\\\*notify <mapname>** to add one");
								}
							}
						}
						else if (argument.equalsIgnoreCase("wipe"))
						{
							if (!file.exists())
							{
								Util.msg(event.getChannel(), event.getAuthor(), "You don't have any map notifications to wipe!");
							}
							else
							{
								IMessage m = Util.msg(event.getChannel(), event.getAuthor(), "Are you sure you would like to wipe **ALL** of your map notifications? Press  :white_check_mark:  to confirm or  :x:  to cancel");

								waitForReaction(m.getStringID(), event.getAuthor().getStringID());
								confirmationMaps.put(event.getAuthor().getStringID(), "wipe");
								confirmationMessages.put(event.getAuthor().getStringID(), m.getStringID());

								ArrayList<String> reactions = new ArrayList<String>();

								reactions.add("white_check_mark");
								reactions.add("x");
								Util.addReactions(m, reactions);

								Executors.newScheduledThreadPool(1).schedule(() ->
								{
									if (!m.isDeleted())
									{
										m.delete();
										confirmationMaps.remove(event.getAuthor().getStringID());
										confirmationMessages.remove(event.getAuthor().getStringID());
									}
								}, 120, TimeUnit.SECONDS);
							}
						}
						else
						{
							boolean mapSet = false;
							boolean mapExists = false;
							int index = 0;

							if (file.exists())
							{
								for (int i = 0; i < json.getJSONArray("notifications").length(); i++)
								{
									String mapNotification = json.getJSONArray("notifications").getString(i);

									if (mapNotification.equalsIgnoreCase(argument))
									{
										mapSet = true;
										index = i;
									}

								}
							}

							for (int i = 0; i < serverInfoJson.getJSONArray("mapDatabase").length(); i++)
							{
								String map = serverInfoJson.getJSONArray("mapDatabase").getString(i);

								if (map.equalsIgnoreCase(argument))
								{
									mapExists = true;
								}
							}

							if (mapSet)
							{
								Util.msg(event.getChannel(), event.getAuthor(), "Removing **" + argument.replace("_", "\\_") + "** from your map notifications!");
								json.put("lastName", event.getAuthor().getName());
								json.getJSONArray("notifications").remove(index);
								FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
							}
							else
							{
								if (!argument.contains("﻿"))
								{
									if (mapExists)
									{
										Util.msg(event.getChannel(), event.getAuthor(), "Adding **" + argument.replace("_", "\\_") + "** to your map notifications!");

										if (file.exists())
										{
											json = new JSONObject(Util.getFileContents(file));
											json.put("lastName", event.getAuthor().getName());
											json.getJSONArray("notifications").put(argument);
											FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
										}
										else
										{
											file.createNewFile();
											json = new JSONObject();
											json.put("lastName", event.getAuthor().getName());
											json.put("notifications", new JSONArray());
											json.getJSONArray("notifications").put(argument);
											FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
										}
									}
									else
									{
										String mapSuggestion = "";
										IMessage m;
										ArrayList<String> mapDatabase = new ArrayList<String>();

										for (int i = 0; i < serverInfoJson.getJSONArray("mapDatabase").length(); i++)
										{
											mapDatabase.add(serverInfoJson.getJSONArray("mapDatabase").getString(i));
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
											m = Util.msg(event.getChannel(), event.getAuthor(), "The map **" + argument.replace("_", "\\_") + "** is not in my maps database, are you sure you'd like to add it? Press  :white_check_mark:  to confirm or  :x:  to cancel");
											waitForReaction(m.getStringID(), event.getAuthor().getStringID());
											confirmationMaps.put(event.getAuthor().getStringID(), argument);
											confirmationMessages.put(event.getAuthor().getStringID(), m.getStringID());

											ArrayList<String> reactions = new ArrayList<String>();

											reactions.add("white_check_mark");
											reactions.add("x");
											Util.addReactions(m, reactions);
										}
										else
										{
											m = Util.msg(event.getChannel(), event.getAuthor(), "The map **" + argument.replace("_", "\\_") + "** is not in my maps database (did you maybe mean **" + mapSuggestion.replace("_", "\\_") + "** instead?), please select which map you would like to choose" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  " + argument.replace("_", "\\_") + System.lineSeparator() + "**`[2]`**  |  " + mapSuggestion.replace("_", "\\_"));
											waitForReaction(m.getStringID(), event.getAuthor().getStringID());
											confirmationMaps.put(event.getAuthor().getStringID(), argument);
											confirmationSuggestionMaps.put(event.getAuthor().getStringID(), mapSuggestion);
											confirmationMessages.put(event.getAuthor().getStringID(), m.getStringID());
											Util.addNumberedReactions(m, true, 2);
										}

										Executors.newScheduledThreadPool(1).schedule(() ->
										{
											if (!m.isDeleted())
											{
												m.delete();
												confirmationMaps.remove(event.getAuthor().getStringID());
												confirmationMessages.remove(event.getAuthor().getStringID());
											}
										}, 120, TimeUnit.SECONDS);
									}
								}
								else
								{
									Util.msg(event.getChannel(), event.getAuthor(), "Do not include invisible characters with your map name!");
								}
							}
						}
					}
				}
			}
			else
			{
				Util.msg(event.getChannel(), event.getAuthor(), "The server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set it up");
			}
		}
		else
		{
			Util.msg(event.getChannel(), event.getAuthor(), "The server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set it up");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*notify" };
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event) throws Exception
	{
		if (confirmationMessages.containsKey(event.getUser().getStringID()) || listMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(confirmationMessages.get(event.getUser().getStringID())) || event.getMessage().getStringID().equals(listMessages.get(event.getUser().getStringID())))
			{
				String guildID = event.getGuild().getStringID();
				String fileName = "data/services/server-tracking/" + guildID + "/" + event.getUser().getStringID() + ".json";
				File file = new File(Util.getJarLocation() + fileName);
				JSONObject json = null;

				if (event.getReaction().getEmoji().toString().equals("✅"))
				{
					if (confirmationMaps.get(event.getUser().getStringID()).equals("wipe"))
					{
						FileUtils.forceDelete(file);
						Util.msg(event.getChannel(), event.getUser(), "Successfully wiped all of your map notifications!");
					}
					else
					{
						Util.msg(event.getChannel(), event.getUser(), "Adding **" + confirmationMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to your map notifications!");

						if (file.exists())
						{
							json = new JSONObject(Util.getFileContents(file));
							json.put("lastName", event.getUser().getName());
							json.getJSONArray("notifications").put(confirmationMaps.get(event.getUser().getStringID()));
							FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
						}
						else
						{
							file.createNewFile();
							json = new JSONObject();
							json.put("lastName", event.getUser().getName());
							json.put("notifications", new JSONArray());
							json.getJSONArray("notifications").put(confirmationMaps.get(event.getUser().getStringID()));
							FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
						}
					}

					confirmationMaps.remove(event.getUser().getStringID());
					confirmationMessages.remove(event.getUser().getStringID());
					Thread.sleep(2000);
				}

				else if (event.getReaction().getEmoji().toString().equals("❌"))
				{
					if (confirmationMaps.get(event.getUser().getStringID()).equals("wipe"))
					{
						Util.msg(event.getChannel(), event.getUser(), "No problem, I won't wipe all your map notifications");
					}
					else
					{
						Util.msg(event.getChannel(), event.getUser(), "No problem, I won't add or remove **" + confirmationMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to/from your map notifications");
					}

					confirmationMaps.remove(event.getUser().getStringID());
					confirmationMessages.remove(event.getUser().getStringID());
					Thread.sleep(2000);
				}

				else if (event.getReaction().getEmoji().toString().equals("1⃣"))
				{
					Util.msg(event.getChannel(), event.getUser(), "Adding **" + confirmationMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to your map notifications!");

					if (file.exists())
					{
						json = new JSONObject(Util.getFileContents(file));
						json.put("lastName", event.getUser().getName());
						json.getJSONArray("notifications").put(confirmationMaps.get(event.getUser().getStringID()));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
					else
					{
						file.createNewFile();
						json = new JSONObject();
						json.put("lastName", event.getUser().getName());
						json.put("notifications", new JSONArray());
						json.getJSONArray("notifications").put(confirmationMaps.get(event.getUser().getStringID()));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
				}

				else if (event.getReaction().getEmoji().toString().equals("2⃣"))
				{
					boolean mapSet = false;
					int index = 0;

					if (file.exists())
					{
						json = new JSONObject(Util.getFileContents(file));

						for (int i = 0; i < json.getJSONArray("notifications").length(); i++)
						{
							String mapNotification = json.getJSONArray("notifications").getString(i);

							if (mapNotification.equalsIgnoreCase(confirmationSuggestionMaps.get(event.getUser().getStringID())))
							{
								mapSet = true;
								index = i;
							}
						}
					}

					if (!mapSet)
					{
						Util.msg(event.getChannel(), event.getUser(), "Adding **" + confirmationSuggestionMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to your map notifications!");

						if (file.exists())
						{
							json = new JSONObject(Util.getFileContents(file));
							json.put("lastName", event.getUser().getName());
							json.getJSONArray("notifications").put(confirmationSuggestionMaps.get(event.getUser().getStringID()));
							FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
						}
						else
						{
							file.createNewFile();
							json = new JSONObject();
							json.put("lastName", event.getUser().getName());
							json.put("notifications", new JSONArray());
							json.getJSONArray("notifications").put(confirmationSuggestionMaps.get(event.getUser().getStringID()));
							FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
						}
					}
					else
					{
						Util.msg(event.getChannel(), event.getUser(), "Removing **" + confirmationSuggestionMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** from your map notifications!");
						json.put("lastName", event.getUser().getName());
						json.getJSONArray("notifications").remove(index);
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
				}

				else if (event.getReaction().getEmoji().toString().equals("▶"))
				{
					ArrayList<String> notifications = new ArrayList<>();

					json = new JSONObject(Util.getFileContents(file));

					for (int i = 0; i < json.getJSONArray("notifications").length(); i++)
					{
						notifications.add(json.getJSONArray("notifications").getString(i));
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

					for (int i = 0; i < json.getJSONArray("notifications").length(); i++)
					{
						notifications.add(json.getJSONArray("notifications").getString(i));
					}

					IMessage m = Util.buildPage(notifications, "Notification List", 10, listPages.get(event.getUser().getStringID()) - 1, false, true, event.getChannel(), event.getUser());

					listMessages.put(event.getUser().getStringID(), m.getStringID());
					waitForReaction(m.getStringID(), event.getUser().getStringID());
					listPages.put(event.getUser().getStringID(), listPages.get(event.getUser().getStringID()) - 1);
				}
			}
		}
	}
}