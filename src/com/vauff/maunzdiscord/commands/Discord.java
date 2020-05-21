package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.CommandHelp;
import com.vauff.maunzdiscord.commands.templates.SubCommandHelp;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

public class Discord extends AbstractCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		Util.msg(channel, author, "Bot invite link: <https://discordapp.com/oauth2/authorize?&client_id=230780946142593025&scope=bot>" + System.lineSeparator() + "Maunz Hub server invite link: https://discord.gg/v55fW9b");
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {
				"*discord",
				"*invite"
		};
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public CommandHelp getHelp()
	{
		return new CommandHelp(getAliases(), new SubCommandHelp[] { new SubCommandHelp("", "Sends an invite link to add the bot to your own server and an invite link to the Maunz Hub server.") });
	}
}