package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class Minecraft extends AbstractCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, MessageChannel channel, User user) throws Exception
	{
		String argument = event.getInteraction().getCommandInteraction().get().getOption("account").get().getValue().get().asString();
		JSONObject usernameProfile = usernameToProfile(argument);
		JSONObject UUIDProfile = UUIDToProfile(argument);
		String username;
		String uuid;

		if (UUIDProfile.isEmpty() || UUIDProfile.has("errorMessage"))
		{
			if (argument.length() > 16)
			{
				Util.editReply(event, "The Minecraft account name **" + argument + "** cannot exceed 16 characters");
				return;
			}

			if (!argument.matches("^\\w+$"))
			{
				Util.editReply(event, "The Minecraft account name **" + argument + "** must be alphanumeric with underscores");
				return;
			}

			if (usernameProfile.isEmpty())
			{
				Util.editReply(event, "There was an error connecting to the Mojang API, please try again later");
				return;
			}

			if (usernameProfile.has("errorMessage"))
			{
				if (usernameProfile.getString("errorMessage").contains("Couldn't find any profile with name"))
					Util.editReply(event, "The Minecraft account name **" + argument + "** is free and does not belong to any account!");
				else
					Util.editReply(event, "Unexpected API error: " + usernameProfile.getString("errorMessage"));

				return;
			}

			username = usernameProfile.getString("name");
			uuid = usernameProfile.getString("id");
		}
		else
		{
			username = UUIDProfile.getString("name");
			uuid = UUIDProfile.getString("id");
		}

		String headURL = "https://minotar.net/helm/" + username + "/100.png";

		EmbedCreateSpec embed = EmbedCreateSpec.builder()
			.color(Util.averageColourFromURL(headURL, true))
			.thumbnail(headURL)
			.footer("Minecraft", "https://i.imgur.com/4o6K42Z.png")
			.addField("Name", username, true)
			.addField("Account Status", "Premium", true)
			.addField("UUID", uuid, true)
			.addField("Skin", "https://minotar.net/body/" + username + "/500.png", true)
			.addField("Raw Skin", "https://minotar.net/skin/" + username, true)
			.build();

		Util.editReply(event, "", embed);
	}

	private JSONObject usernameToProfile(String username)
	{
		return apiRequest("https://api.mojang.com/users/profiles/minecraft/" + username);
	}

	private JSONObject UUIDToProfile(String uuid)
	{
		return apiRequest("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
	}

	private JSONObject apiRequest(String inputUrl)
	{
		try
		{
			URL url = new URL(inputUrl);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", Util.getUserAgent());
			connection.connect();

			int divCode = connection.getResponseCode() / 100;
			String jsonString = "";
			Scanner scanner;

			// 2xx HTTP status codes
			if (divCode == 2)
				scanner = new Scanner(connection.getInputStream());
			// 4xx or 5xx HTTP status codes
			else if (divCode == 4 || divCode == 5)
				scanner = new Scanner(connection.getErrorStream());
			else
				return new JSONObject();

			while (scanner.hasNext())
				jsonString += scanner.nextLine();

			scanner.close();

			return new JSONObject(jsonString);
		}
		catch (IOException e)
		{
			Logger.log.error("", e);
			return new JSONObject();
		}
	}

	@Override
	public ApplicationCommandRequest getCommandRequest()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Gives you full information about a Minecraft account")
			.addOption(ApplicationCommandOptionData.builder()
				.name("account")
				.description("Account username or UUID")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.required(true)
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "minecraft";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}
