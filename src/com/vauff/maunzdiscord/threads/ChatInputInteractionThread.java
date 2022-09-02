package com.vauff.maunzdiscord.threads;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.MainListener;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.Random;

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
			if (event.getInteraction().getUser().isBot())
				return;

			event.deferReply().block();

			String cmdName = event.getInteraction().getCommandInteraction().get().getName().get();
			User user = event.getInteraction().getUser();
			MessageChannel channel = event.getInteraction().getChannel().block();
			Guild guild = event.getInteraction().getGuild().block();

			for (AbstractCommand<ChatInputInteractionEvent> cmd : Main.commands)
			{
				if (!cmdName.equalsIgnoreCase(cmd.getName()))
					continue;
				if (MainListener.cooldownTimestamps.containsKey(user.getId()) && (MainListener.cooldownTimestamps.get(user.getId()) + 2000L) > System.currentTimeMillis())
				{
					if ((!MainListener.cooldownMessageTimestamps.containsKey(user.getId())) || (MainListener.cooldownMessageTimestamps.containsKey(user.getId()) && (MainListener.cooldownMessageTimestamps.get(user.getId()) + 10000L) < System.currentTimeMillis()))
					{
						event.editReply("Slow down!").block();
						MainListener.cooldownMessageTimestamps.put(user.getId(), System.currentTimeMillis());
					}

					return;
				}

				MainListener.cooldownTimestamps.put(user.getId(), System.currentTimeMillis());

				try
				{
					if ((cmd.getPermissionLevel() == AbstractCommand.BotPermission.GUILD_ADMIN && !Util.hasPermission(user, guild)) || (cmd.getPermissionLevel() == AbstractCommand.BotPermission.BOT_ADMIN && !Util.hasPermission(user)))
					{
						event.editReply("You do not have permission to use that command").block();
						return;
					}

					cmd.exe(event, guild, channel, user);
				}
				catch (Exception e)
				{
					Random rnd = new Random();
					int code = 100000000 + rnd.nextInt(900000000);

					event.editReply(":exclamation:  |  **An error has occured!**" + System.lineSeparator() + System.lineSeparator() + "If this was an unexpected error, please report it to Vauff in the #bugreports channel at http://discord.gg/MDx3sMz with the error code " + code).block();
					Logger.log.error(code, e);
				}
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
