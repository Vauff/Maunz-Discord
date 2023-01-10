package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Changelog extends AbstractCommand<ChatInputInteractionEvent>
{
	private static HashMap<Snowflake, Integer> listPages = new HashMap<>();
	private static HashMap<Snowflake, String> listVersions = new HashMap<>();

	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User user) throws Exception
	{
		if (Main.cfg.getGitHubToken().equals(""))
		{
			Util.editReply(event, "This command is disabled because the bot does not have a GitHub token configured!");
			return;
		}

		ApplicationCommandInteraction interaction = event.getInteraction().getCommandInteraction().get();
		String version;

		if (interaction.getOption("version").isPresent())
		{
			String versionArg = interaction.getOption("version").get().getValue().get().asString();

			if (versionArg.contains("."))
			{
				if (versionArg.startsWith("v"))
					version = versionArg;
				else
					version = "v" + versionArg;
			}
			else
			{
				version = versionArg;
			}
		}
		else
		{
			version = Main.version;
		}

		listVersions.put(user.getId(), version);
		runCmd(event, user, 1);
	}

	@Override
	public void buttonPressed(ButtonInteractionEvent event, String buttonId, Guild guild, MessageChannel channel, User user) throws Exception
	{
		int page = listPages.get(user.getId());

		if (buttonId.equals(NEXT_BTN))
			runCmd(event, user, page + 1);
		else if (buttonId.equals(PREV_BTN))
			runCmd(event, user, page - 1);
	}

	private void runCmd(DeferrableInteractionEvent event, User user, int page) throws Exception
	{
		String version = listVersions.get(user.getId());
		String changelogRaw = getReleaseDescription(version);

		if (changelogRaw.equals(""))
		{
			Util.editReply(event, "That version either doesn't exist, or the connection to GitHub failed");
			return;
		}

		ArrayList<String> changelog = new ArrayList<>(Arrays.asList(changelogRaw.split("\r\n")));
		String title = "Maunz " + version + " changelog";

		buildPage(event, changelog, title, 10, 0, page, 0, true, Button.link("https://github.com/Vauff/Maunz-Discord/releases/tag/" + version, "GitHub link"), "");

		listPages.put(user.getId(), page);
		waitForButtonPress(event.getReply().block().getId(), user.getId());
	}

	private String getReleaseDescription(String releaseTag)
	{
		try
		{
			URL url = new URL("https://api.github.com/repos/Vauff/Maunz-Discord/releases/tags/" + releaseTag);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/vnd.github+json");
			connection.setRequestProperty("Authorization", "Bearer " + Main.cfg.getGitHubToken());
			connection.connect();

			if (connection.getResponseCode() != 200)
				return "";

			String jsonString = "";
			Scanner scanner = new Scanner(connection.getInputStream());

			while (scanner.hasNext())
				jsonString += scanner.nextLine();

			scanner.close();
			return new JSONObject(jsonString).getString("body");
		}
		catch (IOException e)
		{
			return "";
		}
	}

	@Override
	public ApplicationCommandRequest getCommandRequest()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Shows the changelog of a Maunz version")
			.addOption(ApplicationCommandOptionData.builder()
				.name("version")
				.description("A specific Maunz version to view the changelog for")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "changelog";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}
