package com.vauff.maunzdiscord.commands;

import java.io.File;
import java.io.IOException;
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
	public static HashMap<String, String> confirmationMaps = new HashMap<String, String>();
	public static HashMap<String, String> confirmationMessages = new HashMap<String, String>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");
		String guildID = event.getGuild().getStringID();
		File file = new File(Util.getJarLocation() + "services/map-tracking/" + guildID + "/" + event.getAuthor().getStringID() + ".json");
		File serverInfoFile = new File(Util.getJarLocation() + "services/map-tracking/" + guildID + "/serverInfo.json");
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
								StringBuilder mapsBuilder = new StringBuilder();

								for (int i = 0; i < json.getJSONArray("notifications").length(); i++)
								{
									mapsBuilder = mapsBuilder.append(json.getJSONArray("notifications").getString(i) + " | ");
								}

								String maps = mapsBuilder.toString().substring(0, mapsBuilder.toString().length() - 3);

								Util.msg(event.getChannel(), "You currently have notifications set for the following maps: **" + maps.toString().replace("_", "\\_") + "**");
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
								IMessage m = event.getChannel().sendMessage("Are you sure you would like to wipe **ALL** of your map notifications? Press  :white_check_mark:  to confirm or  :x:  to cancel. This message will auto expire in 1 minute if you do not respond");
								waitForReaction(m.getStringID(), event.getMessage().getAuthor().getStringID());
								m.addReaction(EmojiManager.getForAlias(":white_check_mark:"));
								Thread.sleep(250);
								m.addReaction(EmojiManager.getForAlias(":x:"));
								confirmationMaps.put(event.getMessage().getAuthor().getStringID(), "wipe");
								confirmationMessages.put(event.getMessage().getAuthor().getStringID(), m.getStringID());

								Executors.newScheduledThreadPool(1).schedule(() ->
								{
									if (!m.isDeleted())
									{
										m.delete();
										confirmationMaps.remove(event.getMessage().getAuthor().getStringID());
										confirmationMessages.remove(event.getMessage().getAuthor().getStringID());
									}
								}, 60, TimeUnit.SECONDS);
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
											m = event.getChannel().sendMessage("The map **" + args[1].replace("_", "\\_") + "** is not in my maps database, are you sure you'd like to add it? Press  :white_check_mark:  to confirm or  :x:  to cancel. This message will auto expire in 1 minute if you do not respond");
										}
										else
										{
											m = event.getChannel().sendMessage("The map **" + args[1].replace("_", "\\_") + "** is not in my maps database (did you maybe mean **" + mapSuggestion + "**?), are you sure you'd like to add it? Press  :white_check_mark:  to confirm or  :x:  to cancel. This message will auto expire in 1 minute if you do not respond");
										}
										
										waitForReaction(m.getStringID(), event.getMessage().getAuthor().getStringID());
										m.addReaction(EmojiManager.getForAlias(":white_check_mark:"));
										Thread.sleep(250);
										m.addReaction(EmojiManager.getForAlias(":x:"));
										confirmationMaps.put(event.getMessage().getAuthor().getStringID(), args[1]);
										confirmationMessages.put(event.getMessage().getAuthor().getStringID(), m.getStringID());

										Executors.newScheduledThreadPool(1).schedule(() ->
										{
											if (!m.isDeleted())
											{
												m.delete();
												confirmationMaps.remove(event.getMessage().getAuthor().getStringID());
												confirmationMessages.remove(event.getMessage().getAuthor().getStringID());
											}
										}, 60, TimeUnit.SECONDS);
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
				Util.msg(event.getChannel(), "The map tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set it up");
			}
		}
		else
		{
			Util.msg(event.getChannel(), "The map tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set it up");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*notify" };
	}

	@Override
	public boolean confirmable()
	{
		return true;
	}

	@Override
	public void confirm(ReactionAddEvent event) throws IOException, InterruptedException
	{
		String guildID = event.getGuild().getStringID();
		String fileName = "services/map-tracking/" + guildID + "/" + event.getUser().getStringID() + ".json";
		File file = new File(Util.getJarLocation() + fileName);
		JSONObject json = null;

		if (Notify.confirmationMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(Notify.confirmationMessages.get(event.getUser().getStringID())))
			{
				if (Notify.confirmationMaps.get(event.getUser().getStringID()).equals("wipe"))
				{
					FileUtils.forceDelete(file);
					Util.msg(event.getChannel(), ":white_check_mark:  |  Successfully wiped all of your map notifications!");
				}
				else
				{
					Util.msg(event.getChannel(), ":white_check_mark:  |  Adding **" + Notify.confirmationMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to your map notifications!");

					if (file.exists())
					{
						json = new JSONObject(Util.getFileContents(file));
						json.put("lastName", event.getUser().getName());
						json.getJSONArray("notifications").put(Notify.confirmationMaps.get(event.getUser().getStringID()));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
					else
					{
						file.createNewFile();
						json = new JSONObject();
						json.put("lastName", event.getUser().getName());
						json.put("notifications", new JSONArray());
						json.getJSONArray("notifications").put(Notify.confirmationMaps.get(event.getUser().getStringID()));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					}
				}

				Notify.confirmationMaps.remove(event.getUser().getStringID());
				Notify.confirmationMessages.remove(event.getUser().getStringID());
				Thread.sleep(2000);
			}
		}
	}

	@Override
	public void deny(ReactionAddEvent event) throws InterruptedException
	{
		if (Notify.confirmationMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(Notify.confirmationMessages.get(event.getUser().getStringID())))
			{
				if (Notify.confirmationMaps.get(event.getUser().getStringID()).equals("wipe"))
				{
					Util.msg(event.getChannel(), ":x:  |  No problem, I won't wipe all your map notifications");
				}
				else
				{
					Util.msg(event.getChannel(), ":x:  |  No problem, I won't add **" + Notify.confirmationMaps.get(event.getUser().getStringID()).replace("_", "\\_") + "** to your map notifications");
				}

				Notify.confirmationMaps.remove(event.getUser().getStringID());
				Notify.confirmationMessages.remove(event.getUser().getStringID());
				Thread.sleep(2000);
			}
		}
	}
}