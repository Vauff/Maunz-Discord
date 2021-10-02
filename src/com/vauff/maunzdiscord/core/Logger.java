package com.vauff.maunzdiscord.core;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.Embed;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.http.client.ClientException;

import java.util.List;

public class Logger
{
	public static org.apache.logging.log4j.Logger log;

	public static void onChatInputInteraction(ChatInputInteractionEvent event)
	{
		String userName = event.getInteraction().getUser().getUsername();
		String userId = event.getInteraction().getUser().getId().asString();
		String channelId = event.getInteraction().getChannel().block().getId().asString();
		String channelName = ((GuildChannel) event.getInteraction().getChannel().block()).getName();
		String guildName = event.getInteraction().getGuild().block().getName();
		String guildId = event.getInteraction().getGuild().block().getId().asString();
		String cmdName = event.getInteraction().getCommandInteraction().get().getName().get();
		String msg;

		msg = userName + " (" + userId + ") | " + guildName + " (" + guildId + ") | #" + channelName + " (" + channelId + ") | used /" + cmdName + getArguments(event.getInteraction().getCommandInteraction().get().getOptions());
		Logger.log.debug(msg);
	}

	public static void onMessageCreate(MessageCreateEvent event)
	{
		try
		{
			if (event.getMessage().getAuthor().isPresent())
			{
				String userName = event.getMessage().getAuthor().get().getUsername();
				String userId = event.getMessage().getAuthor().get().getId().asString();
				String channelId = event.getMessage().getChannelId().asString();
				String messageID = event.getMessage().getId().asString();
				String msg;

				if (!(event.getMessage().getChannel().block() instanceof PrivateChannel))
				{
					String channelName = ((GuildChannel) event.getMessage().getChannel().block()).getName();
					String guildName = event.getGuild().block().getName();
					String guildId = event.getGuild().block().getId().asString();

					msg = messageID + " | " + userName + " (" + userId + ") | " + guildName + " (" + guildId + ") | #" + channelName + " (" + channelId + ") | ";
				}
				else
				{
					PrivateChannel channel = (PrivateChannel) event.getMessage().getChannel().block();
					String recipientName = channel.getRecipients().iterator().next().getUsername();
					String recipientId = channel.getRecipients().iterator().next().getId().asString();

					if (recipientId.equals(userId))
					{
						msg = messageID + " | " + userName + " (" + userId + ") | PM (" + channelId + ") | ";
					}
					else
					{
						msg = messageID + " | " + userName + " (" + userId + ") | " + recipientName + " (" + recipientId + ") | PM (" + channelId + ") | ";
					}
				}

				// This doesn't seem to work with interaction response messages? Needs further investigation because it should work...
				if (!event.getMessage().getContent().isEmpty())
				{
					msg += event.getMessage().getContent();
				}

				for (Attachment attachment : event.getMessage().getAttachments())
				{
					msg += "[Attachment " + attachment.getUrl() + "]";
				}
				for (Embed embed : event.getMessage().getEmbeds())
				{
					msg += "[Embed]";
				}

				Logger.log.debug(msg);
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onReactionAdd(ReactionAddEvent event)
	{
		try
		{
			Message message;

			try
			{
				message = event.getMessage().block();
			}
			catch (ClientException e)
			{
				//this means we can't see the message reacted to because of missing READ_MESSAGE_HISTORY permission
				return;
			}

			String userName = event.getUser().block().getUsername();
			String userId = event.getUser().block().getId().asString();
			String messageID = message.getId().asString();
			String reaction = "null";

			if (event.getEmoji().asUnicodeEmoji().isPresent())
			{
				reaction = event.getEmoji().asUnicodeEmoji().get().getRaw();
			}
			else if (event.getEmoji().asCustomEmoji().isPresent())
			{
				reaction = ":" + event.getEmoji().asCustomEmoji().get().getName() + ":";
			}

			Logger.log.debug(userName + " (" + userId + ") added the reaction " + reaction + " to the message ID " + messageID);
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}

	public static void onReactionRemove(ReactionRemoveEvent event)
	{
		try
		{
			Message message;

			try
			{
				message = event.getMessage().block();
			}
			catch (ClientException e)
			{
				//this means we can't see the message reacted to because of missing READ_MESSAGE_HISTORY permission
				return;
			}

			String userName = event.getUser().block().getUsername();
			String userId = event.getUser().block().getId().asString();
			String messageID = message.getId().asString();
			String reaction = "null";

			if (event.getEmoji().asUnicodeEmoji().isPresent())
			{
				reaction = event.getEmoji().asUnicodeEmoji().get().getRaw();
			}
			else if (event.getEmoji().asCustomEmoji().isPresent())
			{
				reaction = ":" + event.getEmoji().asCustomEmoji().get().getName() + ":";
			}

			Logger.log.debug(userName + " (" + userId + ") removed the reaction " + reaction + " from the message ID " + messageID);
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
