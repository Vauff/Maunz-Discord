package com.vauff.maunzdiscord.commands;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import com.vdurmont.emoji.EmojiManager;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IMessage;

public class Notify extends AbstractCommand<MessageReceivedEvent>
{
	private static HashMap<String, String> confirmationMaps = new HashMap<String, String>();
	private static HashMap<String, String> confirmationSuggestionMaps = new HashMap<String, String>();
	private static HashMap<String, String> confirmationMessages = new HashMap<String, String>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");
		String guildID = event.getGuild().getStringID();
		File file = new File(Util.getJarLocation() + "services/server-tracking/" + guildID + "/" + event.getAuthor().getStringID() + ".json");
		File serverInfoFile = new File(Util.getJarLocation() + "services/server-tracking/" + guildID + "/serverInfo.json");
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
					Util.msg(event.getChannel(), "You need to specify an argument! **Usage: *notify <list/wipe/mapname>**");
				}
				else
				{
					if (args[1].equals(""))
					{
						Util.msg(event.getChannel(), "Please keep to one space between arguments to prevent breakage");
					}
					else
					{
						if (args[1].equalsIgnoreCase("list"))
						{
							if (!file.exists())
							{
								Util.msg(event.getChannel(), "You do not have any map notifications set! Use ***notify <map>** to add or remove one");
							}
							else
							{
								if (json.getJSONArray("notifications").length() != 0)
								{
									StringBuilder mapsBuilder = new StringBuilder();

									for (int i = 0; i < json.getJSONArray("notifications").length(); i++)
									{
										mapsBuilder = mapsBuilder.append(json.getJSONArray("notifications").getString(i) + " | ");
									}

									String maps = mapsBuilder.toString().substring(0, mapsBuilder.toString().length() - 3);

									Util.msg(event.getChannel(), "You currently have notifications set for the following maps: **" + maps.toString().replace("_", "\\_") + "**");
								}
								else
								{
									Util.msg(event.getChannel(), "You do not have any map notifications set! Use ***notify <map>** to add or remove one");
								}
							}
						}
						else if (args[1].equalsIgnoreCase("wipe"))
						{
							if (!file.exists())
							{
								Util.msg(event.getChannel(), "You don't have any map notifications to wipe!");
							}
							else
							{
								IMessage m = event.getChannel().sendMessage("Are you sure you would like to wipe **ALL** of your map notifications? Press  :white_check_mark:  to confirm or  :x:  to cancel");

								waitForReaction(m.getStringID(), event.getAuthor().getStringID());
								confirmationMaps.put(event.getAuthor().getStringID(), "wipe");
								confirmationMessages.put(event.getAuthor().getStringID(), m.getStringID());
								m.addReaction(EmojiManager.getForAlias(":white_check_mark:"));
								Thread.sleep(250);
								m.addReaction(EmojiManager.getForAlias(":x:"));

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

									if (mapNotification.equalsIgnoreCase(args[1]))
									{
										mapSet = true;
										index = i;
									}

								}
							}

							for (int i = 0; i < serverInfoJson.getJSONArray("mapDatabase").length(); i++)
							{
								String map = serverInfoJson.getJSONArray("mapDatabase").getString(i);

								if (map.equalsIgnoreCase(args[1]))
								{
									mapExists = true;
								}
							}

							if (mapSet)
							{
								Util.msg(event.getChannel(), "Removing **" + args[1].replace("_", "\\_") + "** from your map notifications!");
								json.put("lastName", event.getAuthor().getName());
								json.getJSONArray("notifications").remove(index);
								FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
							}
							else
							{
								if (!args[1].contains("﻿"))
								{
									if (mapExists)
									{
										Util.msg(event.getChannel(), "Adding **" + args[1].replace("_", "\\_") + "** to your map notifications!");

										if (file.exists())
										{
											json = new JSONObject(Util.getFileContents(file));
											json.put("lastName", event.getAuthor().getName());
											json.getJSONArray("notifications").put(args[1]);
											FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
										}
										else
										{
											file.createNewFile();
											json = new JSONObject();
											json.put("lastName", event.getAuthor().getName());
											json.put("notifications", new JSONArray());
											json.getJSONArray("notifications").put(args[1]);
											FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
										}
									}
									else
									{
										String mapSuggestion = "";
										IMessage m;

										for (int i = 0; i < serverInfoJson.getJSONArray("mapDatabase").length(); i++)
										{
											String map = serverInfoJson.getJSONArray("mapDatabase").getString(i);

											if (StringUtils.containsIgnoreCase(map, args[1]))
											{
												mapSuggestion = map;
												break;
											}
										}

										if (mapSuggestion.equals(""))
										{
											m = event.getChannel().sendMessage("The map **" + args[1].replace("_", "\\_") + "** is not in my maps database, are you sure you'd like to add it? Press  :white_check_mark:  to confirm or  :x:  to cancel");
											waitForReaction(m.getStringID(), event.getAuthor().getStringID());
											confirmationMaps.put(event.getAuthor().getStringID(), args[1]);
											confirmationMessages.put(event.getAuthor().getStringID(), m.getStringID());
											m.addReaction(EmojiManager.getForAlias(":white_check_mark:"));
											Thread.sleep(250);
											m.addReaction(EmojiManager.getForAlias(":x:"));
										}
										else
										{
											m = event.getChannel().sendMessage("The map **" + args[1].replace("_", "\\_") + "** is not in my maps database (did you maybe mean **" + mapSuggestion.replace("_", "\\_") + "** instead?), please select which map you would like to add" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  " + args[1].replace("_", "\\_") + System.lineSeparator() + "**`[2]`**  |  " + mapSuggestion.replace("_", "\\_"));
											waitForReaction(m.getStringID(), event.getAuthor().getStringID());
											confirmationMaps.put(event.getAuthor().getStringID(), args[1]);
											confirmationSuggestionMaps.put(event.getAuthor().getStringID(), mapSuggestion);
											confirmationMessages.put(event.getAuthor().getStringID(), m.getStringID());
											Util.addReactions(m, true, 2);
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
									Util.msg(event.getChannel(), "Do not include invisible characters with your map name!");
								}
							}
						}
					}
				}
			}
			else
			{
				Util.msg(event.getChannel(), "The server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set it up");
			}
		}
		else
		{
			Util.msg(event.getChannel(), "The server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set it up");
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
		if (confirmationMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(confirmationMessages.get(event.getUser().getStringID())))
			{
				String guildID = event.getGuild().getStringID();
				String fileName = "services/server-tracking/" + guildID + "/" + event.getUser().getStringID() + ".json";
				File file = new File(Util.getJarLocation() + fileName);
				JSONObject json = null;

				if (event.getReaction().getEmoji().toString().equals("✅"))
				{
					if (confirmationMaps.get(event.getUser().getStringID()).equals("wipe"))
					{
						FileUtils.forceDelete(file);
						Util.msg(event.getChannel(), "Successfully wiped all of your map notifications!");
					}
					else
					{
						Util.msg(event.getChannel(), "Adding **" + confirmationMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to your map notifications!");

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
						Util.msg(event.getChannel(), "No problem, I won't wipe all your map notifications");
					}
					else
					{
						Util.msg(event.getChannel(), "No problem, I won't add **" + confirmationMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to your map notifications");
					}

					confirmationMaps.remove(event.getUser().getStringID());
					confirmationMessages.remove(event.getUser().getStringID());
					Thread.sleep(2000);
				}

				else if (event.getReaction().getEmoji().toString().equals("1⃣"))
				{
					Util.msg(event.getChannel(), "Adding **" + confirmationMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to your map notifications!");

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

					if (file.exists())
					{
						json = new JSONObject(Util.getFileContents(file));

						for (int i = 0; i < json.getJSONArray("notifications").length(); i++)
						{
							String mapNotification = json.getJSONArray("notifications").getString(i);

							if (mapNotification.equalsIgnoreCase(confirmationSuggestionMaps.get(event.getUser().getStringID())))
							{
								mapSet = true;
							}
						}
					}

					if (!mapSet)
					{
						Util.msg(event.getChannel(), "Adding **" + confirmationSuggestionMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to your map notifications!");

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
						Util.msg(event.getChannel(), "You already have this map added to your map notifications!");
					}
				}
			}
		}
	}
}