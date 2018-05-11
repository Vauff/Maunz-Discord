package com.vauff.maunzdiscord.commands.servicesmenu.delete;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Util;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.io.File;

public class ServerTrackingDeletePage extends DeleteConfirmationPage
{
	public ServerTrackingDeletePage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd, String std)
	{
		super(trigger, cmd, std);

		addChild(0, (event) ->
		{
			File folder = null;

			try
			{
				folder = new File(Util.getJarLocation() + "data/services/server-tracking/" + trigger.getGuild().getStringID() + "/");
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
			}

			for (File file : folder.listFiles())
			{
				file.delete();
			}

			folder.delete();
			Util.msg(trigger.getChannel(), "Successfully deleted the server tracking service!");
		});

		addChild(1, (event) ->
		{
			Util.msg(trigger.getChannel(), "No problem, I won't delete the server tracking service");
		});
	}
}
