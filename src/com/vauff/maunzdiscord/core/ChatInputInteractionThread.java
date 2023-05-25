package com.vauff.maunzdiscord.core;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

public class ChatInputInteractionThread implements Runnable
{
	private ChatInputInteractionEvent event;
	private Thread thread;
	private String name;

	public ChatInputInteractionThread(ChatInputInteractionEvent passedEvent, String passedName)
	{
		name = passedName;
		event = passedEvent;
	}

	public void start()
	{
		if (thread == null)
		{
			thread = new Thread(this, name);
			thread.start();
		}
	}

	public void run()
	{
		try
		{
			event.deferReply().block();

			User user = event.getInteraction().getUser();
			MessageChannel channel = event.getInteraction().getChannel().block();

			for (AbstractCommand<ChatInputInteractionEvent> cmd : Main.commands)
			{
				if (!event.getInteraction().getCommandInteraction().get().getName().get().equalsIgnoreCase(cmd.getName()))
					continue;

				if ((cmd.getPermissionLevel() == AbstractCommand.BotPermission.GUILD_ADMIN && !Util.hasPermission(user, event.getInteraction().getGuild().block())) || (cmd.getPermissionLevel() == AbstractCommand.BotPermission.BOT_ADMIN && !Util.hasPermission(user)))
				{
					Util.editReply(event, "You do not have permission to use that command");
					return;
				}

				cmd.exe(event, channel, user);
				break;
			}
		}
		catch (Exception e)
		{
			Util.handleException(e, event);
		}
	}
}
