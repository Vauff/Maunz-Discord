package com.vauff.maunzdiscord.commands.servicesmenu.delete;

import java.io.File;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class CSGONotificationsDeletePage extends DeleteConfirmationPage
{
	public CSGONotificationsDeletePage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd, String std)
	{
		super(trigger, cmd, std);

		addChild(0, (event) -> {
			File file = new File(Util.getJarLocation() + "data/services/csgo-updates/" + trigger.getGuild().getStringID() + ".json");

			file.delete();
			Util.msg(trigger.getChannel(), "Successfully deleted the CS:GO updates service!");
		});

		addChild(1, (event) -> {
			Util.msg(trigger.getChannel(), "No problem, I won't delete the CS:GO updates service");
		});
	}
}
