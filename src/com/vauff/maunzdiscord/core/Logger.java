package com.vauff.maunzdiscord.core;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.Embed;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;

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

				Logger.log.debug(userName + " (" + userId + ") | " + guildName + " (" + guildId + ") | #" + channelName + " (" + channelId + ") | used /" + cmdName + getArguments(event.getInteraction().getCommandInteraction().get().getOptions()));
			}
			else
			{
				Logger.log.debug(userName + " (" + userId + ") | PM (" + channelId + ") | used /" + cmdName + getArguments(event.getInteraction().getCommandInteraction().get().getOptions()));
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onMessageCreate(MessageCreateEvent event)
	{
		try
		{
			if (event.getMessage().getAuthor().isPresent() && !event.getMessage().getFlags().contains(Message.Flag.LOADING))
				logMessage(event.getMessage());
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onMessageUpdate(MessageUpdateEvent event)
	{
		try
		{
			Message msg;

			try
			{
				msg = event.getMessage().block();
			}
			catch (Exception e)
			{
				// Message is not available to us, unsure why this is so frequent on this event
				return;
			}

			if (msg.getAuthor().isPresent() && event.getOld().isPresent() && event.getOld().get().getFlags().contains(Message.Flag.LOADING))
				logMessage(msg);
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	private static void logMessage(Message msg)
	{
		boolean privateChannel = msg.getChannel().block() instanceof PrivateChannel;

		// Don't log messages we don't have message content access to
		if (!msg.getAuthor().get().getId().equals(Main.gateway.getSelfId()) && !privateChannel)
			return;

		String userName = msg.getAuthor().get().getUsername();
		String userId = msg.getAuthor().get().getId().asString();
		String channelId = msg.getChannelId().asString();
		String messageID = msg.getId().asString();
		String logMsg;

		if (!privateChannel)
		{
			String channelName = ((GuildChannel) msg.getChannel().block()).getName();
			String guildName = msg.getGuild().block().getName();
			String guildId = msg.getGuild().block().getId().asString();

			logMsg = messageID + " | " + userName + " (" + userId + ") | " + guildName + " (" + guildId + ") | #" + channelName + " (" + channelId + ") |";
		}
		else
		{
			PrivateChannel channel = (PrivateChannel) msg.getChannel().block();
			String recipientName = channel.getRecipients().iterator().next().getUsername();
			String recipientId = channel.getRecipients().iterator().next().getId().asString();

			if (recipientId.equals(userId))
			{
				logMsg = messageID + " | " + userName + " (" + userId + ") | PM (" + channelId + ") |";
			}
			else
			{
				logMsg = messageID + " | " + userName + " (" + userId + ") | " + recipientName + " (" + recipientId + ") | PM (" + channelId + ") |";
			}
		}

		if (!msg.getContent().isEmpty())
			logMsg += " " + msg.getContent();

		for (Attachment attachment : msg.getAttachments())
			logMsg += " [Attachment " + attachment.getUrl() + "]";
		for (Embed embed : msg.getEmbeds())
			logMsg += " [Embed]";

		Logger.log.debug(logMsg);
	}

	public static void onButtonInteraction(ButtonInteractionEvent event)
	{
		try
		{
			User user = event.getInteraction().getUser();

			Logger.log.debug(user.getUsername() + " (" + user.getId().asString() + ") pressed the button " + event.getCustomId() + " on message ID " + event.getMessageId().asString());
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
			Logger.log.debug("Joined guild " + event.getGuild().getName() + " (" + event.getGuild().getId().asString() + ")");
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
				Logger.log.debug("Left guild " + event.getGuild().get().getName() + " (" + event.getGuildId().asString() + ")");
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	/**
	 * Recursive method for parsing all slash command options into plain text
	 *
	 * @param options List of options
	 * @return Plain text representation of options
	 */
	private static String getArguments(List<ApplicationCommandInteractionOption> options)
	{
		String arguments = "";

		for (ApplicationCommandInteractionOption option : options)
		{
			if (option.getValue().isPresent())
			{
				switch (option.getType())
				{
					case BOOLEAN:
						arguments += " " + option.getValue().get().asBoolean();
						break;
					case CHANNEL:
						arguments += " #" + ((GuildChannel) option.getValue().get().asChannel().block()).getName();
						break;
					case INTEGER:
						arguments += " " + option.getValue().get().asLong();
						break;
					case NUMBER:
						arguments += " " + option.getValue().get().asDouble();
						break;
					case ROLE:
						arguments += " @" + option.getValue().get().asRole().block().getName();
						break;
					case STRING:
						arguments += " " + option.getValue().get().asString();
						break;
					case USER:
						arguments += " @" + option.getValue().get().asUser().block().getTag();
						break;
					default:
						Logger.log.error("New command option type needs implementing in Logger#getArguments()");
				}
			}
			else
			{
				arguments += " " + option.getName();
			}

			arguments += getArguments(option.getOptions());
		}

		return arguments;
	}
}
