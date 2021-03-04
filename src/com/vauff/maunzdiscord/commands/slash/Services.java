package com.vauff.maunzdiscord.commands.slash;

import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;

public class Services extends AbstractSlashCommand
{
	@Override
	public String getName()
	{
		return "services";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.GUILD_ADMIN;
	}
}
