package com.vauff.maunzdiscord.commands;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

public class Notify implements ICommand<MessageReceivedEvent>
{
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
			File file = new File(Util.getJarLocation() + fileName);

			if (args[1].equals("list"))
			{
				if (Util.getFileContents(fileName).equals(" "))
				{
					Util.msg(event.getMessage().getChannel(), "You do not have any map notifications set! Use *notify <map> to add or remove one");
				}
				else
				{
					Util.msg(event.getMessage().getChannel(), "You currently have notifications set for the following maps: **" + Util.getFileContents(fileName).replace(",", " | ") + "**");
				}
			}
			else
			{
				if (Util.getFileContents(fileName).contains(args[1]))
				{
					Util.msg(event.getMessage().getChannel(), "Removing **" + args[1] + "** from your map notifications!");

					if (!Util.getFileContents(fileName).contains(","))
					{
						FileUtils.writeStringToFile(file, " ", "UTF-8");
					}
					else
					{
						if (Util.getFileContents(fileName).contains(args[1] + ","))
						{
							FileUtils.writeStringToFile(file, Util.getFileContents(fileName).replace(args[1] + ",", ""), "UTF-8");
						}

						else if (Util.getFileContents(fileName).contains("," + args[1]))
						{
							FileUtils.writeStringToFile(file, Util.getFileContents(fileName).replace("," + args[1], ""), "UTF-8");
						}
					}
				}
				else
				{
					Util.msg(event.getMessage().getChannel(), "Adding **" + args[1] + "** to your map notifications!");

					if (Util.getFileContents(fileName).equals(" "))
					{
						FileUtils.writeStringToFile(file, args[1], "UTF-8");
					}
					else
					{
						FileUtils.writeStringToFile(file, Util.getFileContents(fileName) + "," + args[1], "UTF-8");
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