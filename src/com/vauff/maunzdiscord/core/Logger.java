package com.vauff.maunzdiscord.core;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.component.TopLevelMessageComponent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.List;

public class Logger
{
	public static org.apache.logging.log4j.Logger log;

	public static void onChatInputInteraction(ChatInputInteractionEvent event)
	{
		try
		{
			MessageChannel channel = event.getInteraction().getChannel().block();
			String userName = event.getInteraction().getUser().getUsername();
			String userId = event.getInteraction().getUser().getId().asString();
			String channelId = channel.getId().asString();
			String cmdName = event.getInteraction().getCommandInteraction().get().getName().get();

			if (channel instanceof GuildChannel)
			{
				String channelName = ((GuildChannel) channel).getName();
				String guildName = event.getInteraction().getGuild().block().getName();
				String guildId = event.getInteraction().getGuild().block().getId().asString();

                Logger.log.debug("{} ({}) | {} ({}) | #{} ({}) | used /{}{}", userName, userId, guildName, guildId, channelName, channelId, cmdName, getArguments(event.getInteraction().getCommandInteraction().get().getOptions()));
			}
			else
			{
                Logger.log.debug("{} ({}) | PM ({}) | used /{}{}", userName, userId, channelId, cmdName, getArguments(event.getInteraction().getCommandInteraction().get().getOptions()));
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onButtonInteraction(ButtonInteractionEvent event)
	{
		try
		{
			User user = event.getInteraction().getUser();

            Logger.log.debug("{} ({}) pressed the button {} on message ID {}", user.getUsername(), user.getId().asString(), event.getCustomId(), event.getMessageId().asString());
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onGuildCreate(GuildCreateEvent event)
	{
		try
		{
            Logger.log.debug("Joined guild {} ({})", event.getGuild().getName(), event.getGuild().getId().asString());
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onGuildDelete(GuildDeleteEvent event)
	{
		try
		{
			if (!event.isUnavailable())
			{
                Logger.log.debug("Left guild {} ({})", event.getGuild().get().getName(), event.getGuildId().asString());
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void logMessage(MessageChannel channel, String messageID, String message, Iterable<EmbedCreateSpec> embeds, Iterable<TopLevelMessageComponent> components)
	{
		String userName = Main.botUserData.username();
		String userId = Main.botUserData.id().asString();
		String channelId = channel.getId().asString();
		StringBuilder logMsg;

		if (!(channel instanceof PrivateChannel privChannel))
		{
			Guild guild = ((GuildChannel) channel).getGuild().block();
			String channelName = ((GuildChannel) channel).getName();
			String guildName = guild.getName();
			String guildId = guild.getId().asString();

			logMsg = new StringBuilder(messageID + " | " + userName + " (" + userId + ") | " + guildName + " (" + guildId + ") | #" + channelName + " (" + channelId + ") |");
		}
		else
		{
            String recipientName = privChannel.getRecipients().iterator().next().getUsername();
			String recipientId = privChannel.getRecipients().iterator().next().getId().asString();

			if (recipientId.equals(userId))
			{
				logMsg = new StringBuilder(messageID + " | " + userName + " (" + userId + ") | PM (" + channelId + ") |");
			}
			else
			{
				logMsg = new StringBuilder(messageID + " | " + userName + " (" + userId + ") | " + recipientName + " (" + recipientId + ") | PM (" + channelId + ") |");
			}
		}

		if (message != null && !message.isEmpty())
			logMsg.append(" ").append(message);

		if (embeds != null)
		{
			for (EmbedCreateSpec embed : embeds)
				logMsg.append(" [Embed]");
		}

		if (components != null)
		{
			for (TopLevelMessageComponent component : components)
				logMsg.append(" [").append(component.getType().name()).append(" Component]");
		}

		Logger.log.debug(logMsg.toString());
	}

	/**
	 * Recursive method for parsing all slash command options into plain text
	 *
	 * @param options List of options
	 * @return Plain text representation of options
	 */
	private static String getArguments(List<ApplicationCommandInteractionOption> options)
	{
		StringBuilder arguments = new StringBuilder();

		for (ApplicationCommandInteractionOption option : options)
		{
			if (option.getValue().isPresent())
			{
				switch (option.getType())
				{
					case BOOLEAN:
						arguments.append(" ").append(option.getValue().get().asBoolean());
						break;
					case CHANNEL:
						arguments.append(" #").append(((GuildChannel) option.getValue().get().asChannel().block()).getName());
						break;
					case INTEGER:
						arguments.append(" ").append(option.getValue().get().asLong());
						break;
					case NUMBER:
						arguments.append(" ").append(option.getValue().get().asDouble());
						break;
					case ROLE:
						arguments.append(" @").append(option.getValue().get().asRole().block().getName());
						break;
					case STRING:
						arguments.append(" ").append(option.getValue().get().asString());
						break;
					case USER:
						arguments.append(" @").append(option.getValue().get().asUser().block().getTag());
						break;
					default:
						Logger.log.error("New command option type needs implementing in Logger#getArguments()");
				}
			}
			else
			{
				arguments.append(" ").append(option.getName());
			}

			arguments.append(getArguments(option.getOptions()));
		}

		return arguments.toString();
	}
}
