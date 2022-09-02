package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;

import java.net.JarURLConnection;
import java.util.ArrayList;
import java.util.List;

public class About extends AbstractCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User user) throws Exception
	{
		List<Button> buttons = new ArrayList<>();
		buttons.add(Button.link("https://github.com/Vauff/Maunz-Discord", "GitHub"));
		buttons.add(Button.link("https://discord.com/api/oauth2/authorize?client_id=230780946142593025&permissions=517647752257&scope=bot%20applications.commands", "Bot invite"));
		buttons.add(Button.link("https://discord.gg/v55fW9b", "Maunz Hub server invite"));

		EmbedCreateSpec embed = EmbedCreateSpec.builder()
			.color(Color.of(141, 99, 68))
			.thumbnail("https://i.imgur.com/Fzw48O4.jpg")
			.title("Maunz")
			.description("Maunz is a multi-purpose bot with a focus on Source server tracking, it is developed by Vauff using the Discord4J library.")
			.addField("Version", Main.version, true)
			.addField("Java Version", System.getProperty("java.version"), true)
			.addField("Uptime", getUptime(), true)
			.addField("Build Date", getBuildDate(), true)
			.build();

		event.editReply("").withEmbeds(embed).withComponents(ActionRow.of(buttons)).block();
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
	public ApplicationCommandRequest getCommand()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Gives information about Maunz such as version and uptime")
			.build();
	}

	@Override
	public String getName()
	{
		return "about";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}
