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

		if (args.length == 1)
		{
			Util.msg(event.getMessage().getChannel(), "You need to specify a map or argument! See *help notify for more info");
		}
		else
		{
			if (args[1].equals(""))
			{
				Util.msg(event.getMessage().getChannel(), "Please keep to one space between arguments to prevent breakage");
			}
			else
			{
				File file = new File(Util.getJarLocation() + fileName);

				if (args[1].equalsIgnoreCase("list"))
				{
					if (Util.getFileContents(fileName).equals(" "))
					{
						Util.msg(event.getMessage().getChannel(), "You do not have any map notifications set! Use *notify <map> to add or remove one");
					}
					else
					{
						Util.msg(event.getMessage().getChannel(), "You currently have notifications set for the following maps: **" + Util.getFileContents(fileName).replace(",", " | ").replace("_", "\\_") + "**");
					}
				}

				else if (args[1].equalsIgnoreCase("confirm"))
				{
					if (confirmationStatus.containsKey(event.getMessage().getAuthor().getID()))
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
					else
					{
						Util.msg(event.getMessage().getChannel(), "You don't have any map notification to confirm!");
					}
				}
				else
				{
					if (StringUtils.containsIgnoreCase(Util.getFileContents(fileName), args[1]))
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
						if (StringUtils.containsIgnoreCase(Util.getFileContents("maps.txt"), args[1]))
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
							Util.msg(event.getMessage().getChannel(), "The map **" + args[1].replace("_", "\\_") + "** is not in my maps database, are you sure you'd like to add it? Type *notify confirm to confirm, otherwise ignore this message");
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