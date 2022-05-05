package com.vauff.maunzdiscord.threads;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
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

			event.acknowledge().block();

			String cmdName = event.getInteraction().getCommandInteraction().get().getName().get();
			User author = event.getInteraction().getUser();
			MessageChannel channel = event.getInteraction().getChannel().block();
			Guild guild = event.getInteraction().getGuild().block();

			for (AbstractSlashCommand<ChatInputInteractionEvent> cmd : Main.slashCommands)
			{
				if (!cmdName.equalsIgnoreCase(cmd.getName()))
					continue;

				if (MainListener.cooldownTimestamps.containsKey(author.getId()) && (MainListener.cooldownTimestamps.get(author.getId()) + 2000L) > System.currentTimeMillis())
				{
					if ((!MainListener.cooldownMessageTimestamps.containsKey(author.getId())) || (MainListener.cooldownMessageTimestamps.containsKey(author.getId()) && (MainListener.cooldownMessageTimestamps.get(author.getId()) + 10000L) < System.currentTimeMillis()))
					{
						event.getInteractionResponse().createFollowupMessage("Slow down!").block();
						MainListener.cooldownMessageTimestamps.put(author.getId(), System.currentTimeMillis());
					}

					return;
				}

				MainListener.cooldownTimestamps.put(author.getId(), System.currentTimeMillis());

				try
				{
					if ((cmd.getPermissionLevel() == AbstractCommand.BotPermission.GUILD_ADMIN && !Util.hasPermission(author, guild)) || (cmd.getPermissionLevel() == AbstractCommand.BotPermission.BOT_ADMIN && !Util.hasPermission(author)))
					{
						event.getInteractionResponse().createFollowupMessage("You do not have permission to use that command").block();
						return;
					}

					cmd.exe(event, guild, channel, author);
				}
				catch (Exception e)
				{
					Random rnd = new Random();
					int code = 100000000 + rnd.nextInt(900000000);

					event.getInteractionResponse().createFollowupMessage(":exclamation:  |  **An error has occured!**" + System.lineSeparator() + System.lineSeparator() + "If this was an unexpected error, please report it to Vauff in the #bugreports channel at http://discord.gg/MDx3sMz with the error code " + code).block();
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
