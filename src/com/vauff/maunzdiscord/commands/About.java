package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.MainListener;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;

import java.awt.Color;
import java.net.JarURLConnection;
import java.util.function.Consumer;

public class About extends AbstractCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String buildDate = getBuildDate();

		Consumer<EmbedCreateSpec> embed = spec ->
		{
			spec.setColor(new Color(141, 99, 68));
			spec.setThumbnail("https://i.imgur.com/Fzw48O4.jpg");
			spec.setTitle("Maunz");
			spec.setUrl("https://github.com/Vauff/Maunz-Discord");
			spec.setDescription("Maunz is a Discord bot created by Vauff written in Java using the Discord4J library");
			spec.addField("Version", Main.version, true);
			spec.addField("Java Version", System.getProperty("java.version"), true);
			spec.addField("Uptime", getUptime(), true);
			spec.addField("Build Date", buildDate, true);
		};

		Util.msg(channel, author, embed);
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
		MainListener.uptime.split();

		String uptimeRaw = MainListener.uptime.toSplitString().split("\\.")[0];
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