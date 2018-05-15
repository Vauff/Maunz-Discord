package com.vauff.maunzdiscord.core;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEditEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageSendEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent;
import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IMessage;

public class Logger
{
	public static org.apache.logging.log4j.Logger log;

	@EventSubscriber
	public void onMessageReceived(MessageEvent event)
	{
		if (event.getClass().equals(MessageReceivedEvent.class) || event.getClass().equals(MessageSendEvent.class))
		{
			String authorName = event.getAuthor().getName();
			String authorId = event.getAuthor().getStringID();
			String channelName = event.getChannel().getName();
			String channelId = event.getChannel().getStringID();
			String messageID = event.getMessage().getStringID();
			String msg;

			if (!event.getChannel().isPrivate())
			{
				String guildName = event.getGuild().getName();
				String guildId = event.getGuild().getStringID();

				msg = messageID + " | " + authorName + " (" + authorId + ") | " + guildName + " (" + guildId + ") | #" + channelName + " (" + channelId + ") |";
			}
			else
			{
				String recipientName = event.getChannel().getUsersHere().get(0).getName();
				String recipientId = event.getChannel().getUsersHere().get(0).getStringID();

				if (recipientId.equals(authorId))
				{
					msg = messageID + " | " + authorName + " (" + authorId + ") | PM (" + channelId + ") |";
				}
				else
				{
					msg = messageID + " | " + authorName + " (" + authorId + ") | " + recipientName + " (" + recipientId + ") | PM (" + channelId + ") |";
				}
			}

			if (event.getMessage().getContent() != "")
			{
				msg += " " + event.getMessage().getContent();
			}

			for (IMessage.Attachment attachment : event.getMessage().getAttachments())
			{
				msg += " [Attachment " + attachment.getUrl() + "]";
			}
			for (IEmbed embed : event.getMessage().getEmbeds())
			{
				msg += " [Embed]";
			}

			Logger.log.debug(msg);
		}
	}

	@EventSubscriber
	public void onMessageEdit(MessageEditEvent event)
	{
		String userName = event.getAuthor().getName();
		String userId = event.getAuthor().getStringID();
		String messageID = event.getMessage().getStringID();
		String message = event.getMessage().getContent();

		Logger.log.debug(userName + " (" + userId + ") edited the message ID " + messageID + " to \"" + message + "\"");
	}

	@EventSubscriber
	public void onMessageDelete(MessageDeleteEvent event)
	{
		String userName = event.getAuthor().getName();
		String userId = event.getAuthor().getStringID();
		String messageID = event.getMessage().getStringID();

		Logger.log.debug(userName + " (" + userId + ") deleted the message ID " + messageID);
	}

	@EventSubscriber
	public void onReactionAdd(ReactionAddEvent event)
	{
		String userName = event.getAuthor().getName();
		String userId = event.getAuthor().getStringID();
		String messageID = event.getMessage().getStringID();
		String reaction;

		if (event.getReaction().getEmoji().isUnicode())
		{
			reaction = event.getReaction().getEmoji().getName();
		}
		else
		{
			reaction = ":" + event.getReaction().getEmoji().getName() + ":";
		}

		Logger.log.debug(userName + " (" + userId + ") added the reaction " + reaction + " to the message ID " + messageID);
	}

	@EventSubscriber
	public void onReactionRemove(ReactionRemoveEvent event)
	{
		String userName = event.getAuthor().getName();
		String userId = event.getAuthor().getStringID();
		String messageID = event.getMessage().getStringID();
		String reaction;

		if (event.getReaction().getEmoji().isUnicode())
		{
			reaction = event.getReaction().getEmoji().getName();
		}
		else
		{
			reaction = ":" + event.getReaction().getEmoji().getName() + ":";
		}

		Logger.log.debug(userName + " (" + userId + ") removed the reaction " + reaction + " from the message ID " + messageID);
	}

	@EventSubscriber
	public void onGuildCreate(GuildCreateEvent event)
	{
		Logger.log.debug("Joined guild " + event.getGuild().getName() + " (" + event.getGuild().getStringID() + ")");
	}

	@EventSubscriber
	public void onGuildLeave(GuildLeaveEvent event)
	{
		Logger.log.debug("Left guild " + event.getGuild().getName() + " (" + event.getGuild().getStringID() + ")");
	}
}
