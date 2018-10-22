package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.ReadyEventListener;
import com.vauff.maunzdiscord.core.Util;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.Color;
import java.net.JarURLConnection;

public class About extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		EmbedObject embed = new EmbedBuilder().withColor(new Color(141, 99, 68)).withThumbnail("https://i.imgur.com/Fzw48O4.jpg").withTitle("Maunz").withUrl("https://github.com/Vauff/Maunz-Discord").withDesc("Maunz is a Discord bot created by Vauff written in Java using the Discord4J library").appendField("Version", Main.version, true).appendField("Java Version", System.getProperty("java.version"), true).appendField("Uptime", getUptime(), true).appendField("Build Date", getBuildDate(), true).build();
		Util.msg(event.getChannel(), event.getAuthor(), embed);
	}

	/**
	 * Gets the time at which the JAR-File got built
	 *
	 * @return The time in the format as defined by {@link Util#getTime()}
	 */
	private String getBuildDate() throws Exception
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
	}

	/**
	 * Formats the uptime of the bot as a string
	 *
	 * @return The uptime of the bot formatted as the 2 top most values
	 */
	private static String getUptime()
	{
		ReadyEventListener.uptime.split();

		String uptimeRaw = ReadyEventListener.uptime.toSplitString().split("\\.")[0];
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
		return new String[] { "*about" };
	}
}