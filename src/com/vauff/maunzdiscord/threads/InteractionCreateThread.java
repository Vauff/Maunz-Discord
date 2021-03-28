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
import discord4j.rest.http.client.ClientException;

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
			Logger.log.info("thread running");

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
					return;

				if (MainListener.cooldownTimestamps.containsKey(author.getId()) && (MainListener.cooldownTimestamps.get(author.getId()) + 2000L) > System.currentTimeMillis())
				{
					if ((!MainListener.cooldownMessageTimestamps.containsKey(author.getId())) || (MainListener.cooldownMessageTimestamps.containsKey(author.getId()) && (MainListener.cooldownMessageTimestamps.get(author.getId()) + 10000L) < System.currentTimeMillis()))
					{
						// Replace with interaction!
						//Util.msg(channel, author, author.getMention() + " Slow down!");
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
					Util.msg(author.getPrivateChannel().block(), ":exclamation:  |  **Command/channel blacklisted**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command in " + channel.getMention() + " because a guild administrator has blacklisted either the command or the channel that you ran it in");
					return;
				}

				try
				{
					try
					{
						if ((cmd.getPermissionLevel() == AbstractCommand.BotPermission.GUILD_ADMIN && !Util.hasPermission(author, guild)) || (cmd.getPermissionLevel() == AbstractCommand.BotPermission.BOT_ADMIN && !Util.hasPermission(author)))
						{
							// Replace with interaction!
							//Util.msg(channel, author, "You do not have permission to use that command");
							return;
						}

						event.acknowledge().then(event.getInteractionResponse().createFollowupMessage(cmd.exe(event.getInteraction().getCommandInteraction(), channel, author))).block();
					}
					catch (ClientException e)
					{
						if (e.getStatus().code() == 403)
						{
							// Replace with interaction!
							//Util.msg(author.getPrivateChannel().block(), ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command in " + channel.getMention() + " because it's lacking permissions." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
							return;
						}
						else
						{
							throw e;
						}
					}
				}
				catch (Exception e)
				{
					Random rnd = new Random();
					int code = 100000000 + rnd.nextInt(900000000);

					// Replace with interaction!
					//Util.msg(channel, author, ":exclamation:  |  **An error has occured!**" + System.lineSeparator() + System.lineSeparator() + "If this was an unexpected error, please report it to Vauff in the #bugreports channel at http://discord.gg/MDx3sMz with the error code " + code);
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
