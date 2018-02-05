package com.vauff.maunzdiscord.commands.servicesmenu;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public abstract class AbstractServiceActionPage extends AbstractMenuPage
{
	protected List<String> services = new ArrayList<String>();
	protected boolean guildHasService = false;

	public AbstractServiceActionPage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd)
	{
		super(trigger, cmd);

		List<String> fileLocationList = new ArrayList<String>(Arrays.asList(Util.getJarLocation() + "data/services/server-tracking/", Util.getJarLocation() + "data/services/csgo-updates/"));

		for (String fileLocation : fileLocationList)
		{
			File file = new File(fileLocation + trigger.getGuild().getStringID());
			File file2 = new File(fileLocation + trigger.getGuild().getStringID() + ".json");

			if (file.exists() || file2.exists())
			{
				guildHasService = true;
				services.add(fileLocation.split("data/services/")[1].replace("/", ""));
			}
		}
	}

	@Override
	public String[] getItems()
	{
		String add = "";

		if (services.contains("server-tracking"))
		{
			add += "Server Tracking,";
		}

		if (services.contains("csgo-updates"))
		{
			add += "CS:GO Update Notifications";
		}

		return add.split(",");
	}
}
