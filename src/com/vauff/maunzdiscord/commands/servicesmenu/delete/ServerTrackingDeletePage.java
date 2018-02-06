package com.vauff.maunzdiscord.commands.servicesmenu.delete;

import java.io.File;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class ServerTrackingDeletePage extends DeleteConfirmationPage
{
	public ServerTrackingDeletePage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd, String std)
	{
		super(trigger, cmd, std);

		addChild(0, (event) ->
		{
			File folder = new File(Util.getJarLocation() + "data/services/server-tracking/" + trigger.getGuild().getStringID() + "/");
			File file = new File(Util.getJarLocation() + "data/services/server-tracking/" + trigger.getGuild().getStringID() + "/serverInfo.json");

			file.delete();
			folder.delete();
			Util.msg(trigger.getChannel(), "Successfully deleted the server tracking service!");
		});

		addChild(1, (event) ->
		{
			Util.msg(trigger.getChannel(), "No problem, I won't delete the server tracking service");
		});
	}
}
