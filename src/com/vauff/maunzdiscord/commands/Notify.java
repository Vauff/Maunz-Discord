package com.vauff.maunzdiscord.commands;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Util;

import com.vdurmont.emoji.EmojiManager;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

public class Notify implements ICommand<MessageReceivedEvent>
{
	public static HashMap<String, String> confirmationMaps = new HashMap<String, String>();
	public static HashMap<String, String> confirmationMessages = new HashMap<String, String>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");
		String fileName = "map-notification-data/" + event.getMessage().getAuthor().getStringID() + ".txt";
		File file = new File(Util.getJarLocation() + fileName);

		if (Util.getFileContents(file).contains(System.getProperty("line.separator")) || Util.getFileContents(file).contains("﻿"))
		{
			FileUtils.writeStringToFile(file, Util.getFileContents(file).replace(System.getProperty("line.separator"), "").replace("﻿", ""), "UTF-8");
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
					if (Util.getFileContents(fileName).equals(" "))
					{
						Util.msg(event.getChannel(), "You do not have any map notifications set! Use ***notify <map>** to add or remove one");
					}
					else
					{
						Util.msg(event.getChannel(), "You currently have notifications set for the following maps: **" + Util.getFileContents(fileName).replace(",", " | ").replace("_", "\\_") + "**");
					}
				}
				else if (args[1].equalsIgnoreCase("wipe"))
				{
					if (Util.getFileContents(fileName).equals(" "))
					{
						Util.msg(event.getChannel(), "You don't have any map notifications to wipe!");
					}
					else
					{
						IMessage m = event.getChannel().sendMessage("Are you sure you would like to wipe **ALL** of your map notifications? Press  :white_check_mark:  to confirm or  :x:  to cancel. This message will auto expire in 1 minute if you do not respond");
						m.addReaction(EmojiManager.getForAlias(":white_check_mark:"));
						Thread.sleep(250);
						m.addReaction(EmojiManager.getForAlias(":x:"));
						confirmationMaps.put(event.getMessage().getAuthor().getStringID(), "wipe");
						confirmationMessages.put(event.getMessage().getAuthor().getStringID(), m.getStringID());
						Thread.sleep(60000);

						if (!m.isDeleted())
						{
							m.delete();
							confirmationMaps.remove(event.getMessage().getAuthor().getStringID());
							confirmationMessages.remove(event.getMessage().getAuthor().getStringID());
						}
					}
				}
				else
				{
					String[] mapNotifications = Util.getFileContents(fileName).split(",");
					boolean mapSet = false;
					String[] maps = Util.getFileContents("maps.txt").split(",");
					boolean mapExists = false;

					for (String mapNotification : mapNotifications)
					{
						if (mapNotification.equalsIgnoreCase(args[1]))
						{
							mapSet = true;
						}
					}

					for (String map : maps)
					{
						if (map.equalsIgnoreCase(args[1]))
						{
							mapExists = true;
						}
					}

					if (mapSet)
					{
						Util.msg(event.getChannel(), "Removing **" + args[1].replace("_", "\\_") + "** from your map notifications!");

						if (!Util.getFileContents(fileName).contains(","))
						{
							FileUtils.writeStringToFile(file, " ", "UTF-8");
						}
						else
						{
							if (StringUtils.containsIgnoreCase(Util.getFileContents(fileName), args[1] + ","))
							{
								FileUtils.writeStringToFile(file, StringUtils.replaceIgnoreCase(Util.getFileContents(fileName), args[1] + ",", ""), "UTF-8");
							}

							else if (StringUtils.containsIgnoreCase(Util.getFileContents(fileName), "," + args[1]))
							{
								FileUtils.writeStringToFile(file, StringUtils.replaceIgnoreCase(Util.getFileContents(fileName), "," + args[1], ""), "UTF-8");
							}
						}
					}
					else
					{
						if (!args[1].contains("﻿"))
						{
							if (mapExists)
							{
								Util.msg(event.getChannel(), "Adding **" + args[1].replace("_", "\\_") + "** to your map notifications!");

								if (Util.getFileContents(fileName).equals(" "))
								{
									FileUtils.writeStringToFile(file, args[1], "UTF-8");
								}
								else
								{
									FileUtils.writeStringToFile(file, Util.getFileContents(fileName) + "," + args[1], "UTF-8");
								}
							}
							else
							{
								IMessage m = event.getChannel().sendMessage("The map **" + args[1].replace("_", "\\_") + "** is not in my maps database, are you sure you'd like to add it? Press  :white_check_mark:  to confirm or  :x:  to cancel. This message will auto expire in 1 minute if you do not respond");
								m.addReaction(EmojiManager.getForAlias(":white_check_mark:"));
								Thread.sleep(250);
								m.addReaction(EmojiManager.getForAlias(":x:"));
								confirmationMaps.put(event.getMessage().getAuthor().getStringID(), args[1]);
								confirmationMessages.put(event.getMessage().getAuthor().getStringID(), m.getStringID());
								Thread.sleep(60000);

								if (!m.isDeleted())
								{
									m.delete();
									confirmationMaps.remove(event.getMessage().getAuthor().getStringID());
									confirmationMessages.remove(event.getMessage().getAuthor().getStringID());
								}
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

	@Override
	public String[] getAliases()
	{
		return new String[] { "*notify" };
	}
}