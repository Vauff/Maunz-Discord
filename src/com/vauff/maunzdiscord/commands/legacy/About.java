package com.vauff.maunzdiscord.commands.legacy;

import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.objects.CommandHelp;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.net.JarURLConnection;

public class About extends AbstractLegacyCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String buildDate = getBuildDate();

		EmbedCreateSpec embed = EmbedCreateSpec.builder()
			.color(Color.of(141, 99, 68))
			.thumbnail("https://i.imgur.com/Fzw48O4.jpg")
			.title("Maunz")
			.url("https://github.com/Vauff/Maunz-Discord")
			.description("Maunz is a Discord bot created by Vauff written in Java using the Discord4J library")
			.addField("Version", Main.version, true)
			.addField("Java Version", System.getProperty("java.version"), true)
			.addField("Uptime", getUptime(), true)
			.addField("Build Date", buildDate, true)
			.build();

		Util.msg(channel, embed);
	}

	/**
	 * Gets the time at which the JAR-File got built
	 *
	 * @return The time in Discord's "F" format
	 */
	private String getBuildDate() throws Exception
	{
		try
		{
			long unparsedTime = ((JarURLConnection) ClassLoader.getSystemResource(Main.class.getName().replace('.', '/') + ".class").openConnection()).getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();

			return "<t:" + (unparsedTime / 1000) + ":F>";
		}
		catch (ClassCastException e)
		{
			return "N/A";
		}
	}

	/**
	 * Formats the uptime of the bot as a string
	 *
	 * @return The uptime of the bot formatted as the 2 top most values
	 */
	private static String getUptime()
	{
		Main.uptime.split();

		String uptimeRaw = Main.uptime.toSplitString().split("\\.")[0];
		String secondText = "seconds";
		String minuteText = "minutes";
		String hourText = "hours";
		String dayText = "days";
		int seconds = Integer.parseInt(uptimeRaw.split(":")[2]);
		int minutes = Integer.parseInt(uptimeRaw.split(":")[1]);
		int hours = Integer.parseInt(uptimeRaw.split(":")[0]) % 24;
		int days = (Integer.parseInt(uptimeRaw.split(":")[0]) / 24);

		if (seconds == 1)
		{
			secondText = "second";
		}

		if (minutes == 1)
		{
			minuteText = "minute";
		}

		if (hours == 1)
		{
			hourText = "hour";
		}

		if (days == 1)
		{
			dayText = "day";
		}

		if (days >= 1)
		{
			return days + " " + dayText + ", " + hours + " " + hourText;
		}

		else if (hours >= 1)
		{
			return hours + " " + hourText + ", " + minutes + " " + minuteText;
		}

		else if (minutes >= 1)
		{
			return minutes + " " + minuteText + ", " + seconds + " " + secondText;
		}
		else
		{
			return seconds + " " + secondText;
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "about" };
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		return new CommandHelp[] { new CommandHelp("", "Gives information about Maunz such as version and uptime.") };
	}
}