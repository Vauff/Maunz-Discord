package com.vauff.maunzdiscord.commands;

import java.awt.Color;
import java.net.JarURLConnection;

import org.apache.commons.lang3.StringUtils;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

public class About extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		EmbedObject embed = new EmbedBuilder().withColor(new Color(141, 99, 68)).withThumbnail("https://i.imgur.com/Fzw48O4.jpg").withTitle("Maunz").withUrl("https://github.com/Vauff/Maunz-Discord").withDesc("Maunz is a Discord bot created by Vauff written in Java using the Discord4J library").appendField("Version", Main.version, true).appendField("Java Version", System.getProperty("java.version"), true).appendField("Uptime", Util.getUptime(), true).appendField("Dev Mode", StringUtils.capitalize(Boolean.toString(Util.devMode)), true).appendField("Build Date", getBuildDate(), true).build();
		Util.msg(event.getChannel(), event.getAuthor(), embed);
	}

	/**
	 * Gets the time at which the JAR-File got built
	 *
	 * @return The time in the format as defined by {@link Util#getTime()}
	 */
	private String getBuildDate()
	{
		try
		{
			long unparsedTime = ((JarURLConnection) ClassLoader.getSystemResource(Main.class.getName().replace('.', '/') + ".class").openConnection()).getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();

			return Util.getTime(unparsedTime);
		}
		catch (ClassCastException e)
		{
			return "N/A";
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