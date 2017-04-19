package com.vauff.maunzdiscord.commands;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

public class Notify implements ICommand<MessageReceivedEvent>
{
	private static HashMap<String, String> confirmationStatus = new HashMap<String, String>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");
		String fileName = "map-notification-data/" + event.getMessage().getAuthor().getID() + ".txt";
		File file = new File(Util.getJarLocation() + fileName);

		if (FileUtils.readFileToString(file, "UTF-8").contains(System.getProperty("line.separator")))
		{
			FileUtils.writeStringToFile(file, FileUtils.readFileToString(file, "UTF-8").replace(System.getProperty("line.separator"), ""), "UTF-8");
		}

		if (args.length == 1)
		{
			Util.msg(event.getMessage().getChannel(), "You need to specify a map or argument! **Usage: <list/confirm/wipe/map>**");
		}
		else
		{
			if (args[1].equals(""))
			{
				Util.msg(event.getMessage().getChannel(), "Please keep to one space between arguments to prevent breakage");
			}
			else
			{
				if (args[1].equalsIgnoreCase("list"))
				{
					if (Util.getFileContents(fileName).equals(" "))
					{
						Util.msg(event.getMessage().getChannel(), "You do not have any map notifications set! Use ***notify <map>** to add or remove one");
					}
					else
					{
						Util.msg(event.getMessage().getChannel(), "You currently have notifications set for the following maps: **" + Util.getFileContents(fileName).replace(",", " | ").replace("_", "\\_") + "**");
					}
				}
				else if (args[1].equalsIgnoreCase("confirm"))
				{
					if (confirmationStatus.containsKey(event.getMessage().getAuthor().getID()) && !confirmationStatus.get(event.getMessage().getAuthor().getID()).equals("wipe"))
					{
						Util.msg(event.getMessage().getChannel(), "Adding **" + confirmationStatus.get(event.getMessage().getAuthor().getID()).replace("_", "\\_") + "** to your map notifications!");

						if (Util.getFileContents(fileName).equals(" "))
						{
							FileUtils.writeStringToFile(file, confirmationStatus.get(event.getMessage().getAuthor().getID()), "UTF-8");
						}
						else
						{
							FileUtils.writeStringToFile(file, Util.getFileContents(fileName) + "," + confirmationStatus.get(event.getMessage().getAuthor().getID()), "UTF-8");
						}

						confirmationStatus.remove(event.getMessage().getAuthor().getID());
					}
					else if (confirmationStatus.containsKey(event.getMessage().getAuthor().getID()) && confirmationStatus.get(event.getMessage().getAuthor().getID()).equals("wipe"))
					{
						Util.msg(event.getMessage().getChannel(), "Successfully wiped all of your map notifications!");
						FileUtils.writeStringToFile(file, " ", "UTF-8");
					}
					else
					{
						Util.msg(event.getMessage().getChannel(), "You don't have any map notification action to confirm!");
					}
				}
				else if (args[1].equalsIgnoreCase("wipe"))
				{
					if (Util.getFileContents(fileName).equals(" "))
					{
						Util.msg(event.getMessage().getChannel(), "You don't have any map notifications to wipe!");
					}
					else
					{
						Util.msg(event.getMessage().getChannel(), "Are you sure you would like to wipe **ALL** of your map notifications? Type ***notify confirm** to confirm, otherwise ignore this message");
						confirmationStatus.put(event.getMessage().getAuthor().getID(), "wipe");
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
						Util.msg(event.getMessage().getChannel(), "Removing **" + args[1].replace("_", "\\_") + "** from your map notifications!");

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
						if (mapExists)
						{
							Util.msg(event.getMessage().getChannel(), "Adding **" + args[1].replace("_", "\\_") + "** to your map notifications!");

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
							Util.msg(event.getMessage().getChannel(), "The map **" + args[1].replace("_", "\\_") + "** is not in my maps database, are you sure you'd like to add it? Type ***notify confirm** to confirm, otherwise ignore this message");
							confirmationStatus.put(event.getMessage().getAuthor().getID(), args[1]);

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