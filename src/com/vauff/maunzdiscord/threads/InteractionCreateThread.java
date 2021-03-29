package com.vauff.maunzdiscord.threads;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.MainListener;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;

import java.util.List;
import java.util.Random;

import static com.mongodb.client.model.Filters.eq;

public class InteractionCreateThread implements Runnable
{
	private InteractionCreateEvent event;
	private Thread thread;
	private String name;

	public InteractionCreateThread(InteractionCreateEvent passedEvent, String passedName)
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

			String cmdName = event.getCommandName();
			User author = event.getInteraction().getUser();
			MessageChannel channel = event.getInteraction().getChannel().block();
			Guild guild = event.getInteraction().getGuild().block();

			for (AbstractSlashCommand<ApplicationCommandInteraction> cmd : MainListener.slashCommands)
			{
				if (!cmdName.equalsIgnoreCase(cmd.getName()))
					continue;

				boolean enabled;

				if (channel instanceof PrivateChannel)
				{
					enabled = Util.isEnabled();
				}
				else
				{
					enabled = Util.isEnabled(guild);
				}

				// Fix this when enable/disable ported to slash commands
				//if (!enabled && !(cmd instanceof Enable) && !(cmd instanceof Disable))
				if (!enabled)
				{
					event.replyEphemeral("The bot is currently disabled").block();
					return;
				}

				if (MainListener.cooldownTimestamps.containsKey(author.getId()) && (MainListener.cooldownTimestamps.get(author.getId()) + 2000L) > System.currentTimeMillis())
				{
					if ((!MainListener.cooldownMessageTimestamps.containsKey(author.getId())) || (MainListener.cooldownMessageTimestamps.containsKey(author.getId()) && (MainListener.cooldownMessageTimestamps.get(author.getId()) + 10000L) < System.currentTimeMillis()))
					{
						event.replyEphemeral("Slow down!").block();
						MainListener.cooldownMessageTimestamps.put(author.getId(), System.currentTimeMillis());
					}

					return;
				}

				MainListener.cooldownTimestamps.put(author.getId(), System.currentTimeMillis());
				boolean blacklisted = false;

				if (!(channel instanceof PrivateChannel) && !Util.hasPermission(author, guild))
				{
					List<String> blacklist = Main.mongoDatabase.getCollection("guilds").find(eq("guildId", guild.getId().asLong())).first().getList("blacklist", String.class);

					for (String entry : blacklist)
					{
						if ((entry.split(":")[0].equalsIgnoreCase(channel.getId().asString()) || entry.split(":")[0].equalsIgnoreCase("all")) && (entry.split(":")[1].equalsIgnoreCase(cmdName.replace(Main.prefix, "")) || entry.split(":")[1].equalsIgnoreCase("all")))
						{
							blacklisted = true;
							break;
						}
					}
				}

				if (blacklisted)
				{
					event.replyEphemeral("A server administrator has blacklisted this command or the channel that you ran it in").block();
					return;
				}

				try
				{
					if ((cmd.getPermissionLevel() == AbstractCommand.BotPermission.GUILD_ADMIN && !Util.hasPermission(author, guild)) || (cmd.getPermissionLevel() == AbstractCommand.BotPermission.BOT_ADMIN && !Util.hasPermission(author)))
					{
						event.replyEphemeral("You do not have permission to use that command").block();
						return;
					}

					event.reply(cmd.exe(event.getInteraction().getCommandInteraction(), channel, author)).block();
				}
				catch (Exception e)
				{
					Random rnd = new Random();
					int code = 100000000 + rnd.nextInt(900000000);

					event.replyEphemeral(":exclamation:  |  **An error has occured!**" + System.lineSeparator() + System.lineSeparator() + "If this was an unexpected error, please report it to Vauff in the #bugreports channel at http://discord.gg/MDx3sMz with the error code " + code).block();
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
