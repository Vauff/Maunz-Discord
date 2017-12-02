package com.vauff.maunzdiscord.commands;

import java.util.ArrayList;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Restart extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (Util.hasPermission(event.getAuthor()))
		{
			final ArrayList<String> command = new ArrayList<String>();

			command.add("java");
			command.add("-jar");
			command.add("Maunz-Discord.jar");

			if (Util.devMode)
			{
				command.add("-dev");
			}

			Main.log.info("Maunz is restarting...");
			new ProcessBuilder(command).start();
			Main.bot.disconnect();
			Main.client.logout();
			System.exit(0);
		}
		else
		{
			Util.msg(event.getChannel(), "You do not have permission to use that command");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*restart" };
	}
}