package com.vauff.maunzdiscord.commands;

import java.net.JarURLConnection;

import org.apache.commons.lang3.StringUtils;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class About implements ICommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		Util.msg(event.getChannel(), "**About: **Maunz is a Discord bot created by Vauff | **Version: **" + Main.version + " | " + "**Build Date: **" + getBuildDate() + " | " + "**Dev Mode: **" + StringUtils.capitalize(Boolean.toString(Util.devMode)) + " | " + "**Uptime: **" + Util.getUptime());
	}

	private String getBuildDate()
	{
		try
		{
			long unparsedTime = ((JarURLConnection) ClassLoader.getSystemResource(Main.class.getName().replace('.', '/') + ".class").openConnection()).getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();

			return Util.getTime(unparsedTime);
		}
		catch (ClassCastException e)
		{
			return "Not available in debug mode";
		}
		catch (Exception e)
		{
			Main.log.error("", e);
			return "Error";
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*about" };
	}
}