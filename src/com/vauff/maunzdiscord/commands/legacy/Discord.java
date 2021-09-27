package com.vauff.maunzdiscord.commands.legacy;

import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.objects.CommandHelp;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

public class Discord extends AbstractLegacyCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		Util.msg(channel, author, "Bot invite link: <https://discord.com/api/oauth2/authorize?client_id=230780946142593025&permissions=104193601&scope=bot%20applications.commands>" + System.lineSeparator() + "Maunz Hub server invite link: https://discord.gg/v55fW9b");
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {
			"discord",
			"invite"
		};
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		return new CommandHelp[] { new CommandHelp("", "Sends an invite link to add the bot to your own server and an invite link to the Maunz Hub server.") };
	}
}