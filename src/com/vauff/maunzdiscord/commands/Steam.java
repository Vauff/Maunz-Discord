package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

public class Steam extends AbstractCommand<ChatInputInteractionEvent>
{
	private final long steam64IdIdent = 76561197960265728L;

	@Override
	public void exe(ChatInputInteractionEvent event, MessageChannel channel, User user) throws Exception
	{
		if (Main.cfg.getSteamApiKey().equals(""))
		{
			Util.editReply(event, "This command is disabled because the bot does not have a Steam API key configured!");
			return;
		}

		long argument = parseArgument(event.getInteraction().getCommandInteraction().get().getOption("steamid").get().getValue().get().asString());

		if (argument == 0L)
		{
			Util.editReply(event, "Invalid Steam ID or profile URL provided");
			return;
		}

		JSONObject json = getPlayerSummary(argument);
		JSONObject bansJson = getPlayerBans(argument);

		if (Objects.isNull(json) || Objects.isNull(bansJson))
		{
			Util.editReply(event, "There was an error connecting to the Steam API, please try again later");
			return;
		}

		if (json.isEmpty())
		{
			Util.editReply(event, "Invalid Steam ID or profile URL provided");
			return;
		}

		String nickname = json.getString("personaname");
		String link = json.getString("profileurl");
		String avatarURL = json.getString("avatarfull");
		String steam64Id = json.getString("steamid");
		String steamId = steam64IdToSteamId(argument);
		String steamId3 = steam64IdToSteamId3(argument);
		String lastLogoff = "N/A";
		String visibility = "N/A";
		String status = "N/A";
		String realName = "N/A";
		String country = "N/A";
		String creationDate = "N/A";
		String bans = "";

		if (json.has("lastlogoff"))
			lastLogoff = "<t:" + json.getLong("lastlogoff") + ">";

		if (json.has("realname"))
			realName = json.getString("realname");

		if (json.has("loccountrycode"))
			country = ":flag_" + json.getString("loccountrycode").toLowerCase() + ":";

		switch (json.getInt("communityvisibilitystate"))
		{
			case 1 -> visibility = "Private";
			case 2 -> visibility = "Friends only";
			case 3 -> visibility = "Public";
		}

		if (json.getInt("communityvisibilitystate") == 3)
		{
			creationDate = "<t:" + json.getLong("timecreated") + ":D>";

			switch (json.getInt("personastate"))
			{
				case 0 -> status = "Offline";
				case 1 -> status = "Online";
				case 2 -> status = "Busy";
				case 3 -> status = "Away";
				case 4 -> status = "Snooze";
				case 5 -> status = "Looking to Trade";
				case 6 -> status = "Looking to Play";
			}
		}

		if (bansJson.getBoolean("VACBanned"))
			bans += "VAC, ";

		if (bansJson.getInt("NumberOfGameBans") > 0)
			bans += "Game, ";

		if (bansJson.getString("EconomyBan").equals("banned"))
			bans += "Trade, ";

		if (bansJson.getBoolean("CommunityBanned"))
			bans += "Community";

		if (bans.equalsIgnoreCase(""))
			bans = "None";

		if (bans.substring(bans.length() - 2).equalsIgnoreCase(", "))
			bans = bans.substring(0, bans.length() - 2);

		EmbedCreateSpec embed = EmbedCreateSpec.builder()
			.color(Util.averageColourFromURL(avatarURL, true))
			.thumbnail(avatarURL)
			.footer("Steam", "https://i.imgur.com/GuXJIeX.png")
			.title(nickname)
			.url(link)
			.addField("Name", nickname, true)
			.addField("Real Name", realName, true)
			.addField("Country", country, true)
			.addField("Account Created", creationDate, true)
			.addField("Last Logoff", lastLogoff, true)
			.addField("Status", status, true)
			.addField("Profile Visibility", visibility, true)
			.addField("Bans", bans, true)
			.addField("Steam ID", steamId, true)
			.addField("Steam ID3", steamId3, true)
			.addField("Steam64 ID", steam64Id, true)
			.build();

		Util.editReply(event, "", embed);
	}

	private long parseArgument(String argument)
	{
		try
		{
			// Steam ID
			if (argument.startsWith("STEAM_"))
				return steamIdToSteam64Id(argument);

			// Steam ID3
			if (argument.startsWith("[U:") || argument.startsWith("U:"))
				return steamId3ToSteam64Id(argument);

			// Steam64 ID
			if (StringUtils.isNumeric(argument) && argument.length() == 17)
				return Long.parseLong(argument);

			// Steam64 ID profile URL
			if (argument.contains("steamcommunity.com/profiles/"))
				return Long.parseLong(argument.split("steamcommunity.com/profiles/")[1].replace("/", ""));

			JSONObject json;

			// Vanity profile URL
			if (argument.contains("steamcommunity.com/id/"))
				json = resolveVanityURL(argument.split("steamcommunity.com/id/")[1].replace("/", ""));
			// Nothing else to try, assume argument is only ID portion of vanity URL
			else
				json = resolveVanityURL(argument);

			if (json.getInt("success") == 1)
				return Long.parseLong(json.getString("steamid"));
			else
				return 0L;
		}
		catch (Exception e)
		{
			return 0L;
		}
	}

	private JSONObject getPlayerSummary(long steam64Id)
	{
		JSONObject response = apiRequest("ISteamUser/GetPlayerSummaries/v2", "steamids", Long.toString(steam64Id));

		if (Objects.isNull(response))
			return null;

		JSONArray array = response.getJSONObject("response").getJSONArray("players");

		return array.isEmpty() ? new JSONObject() : array.getJSONObject(0);
	}

	private JSONObject getPlayerBans(long steam64Id)
	{
		JSONObject response = apiRequest("ISteamUser/GetPlayerBans/v1", "steamids", Long.toString(steam64Id));

		if (Objects.isNull(response))
			return null;

		JSONArray array = response.getJSONArray("players");

		return array.isEmpty() ? new JSONObject() : array.getJSONObject(0);
	}

	private JSONObject resolveVanityURL(String vanityUrl)
	{
		JSONObject response = apiRequest("ISteamUser/ResolveVanityURL/v1", "vanityurl", vanityUrl);

		return Objects.isNull(response) ? null : response.getJSONObject("response");
	}

	private JSONObject apiRequest(String endpoint, String argumentName, String argument)
	{
		try
		{
			URL url = new URL("https://api.steampowered.com/" + endpoint + "/?key=" + Main.cfg.getSteamApiKey() + "&" + argumentName + "=" + argument);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", Util.getUserAgent());
			connection.connect();

			// Not a 2xx HTTP status code
			if (connection.getResponseCode() / 100 != 2)
				return null;

			String jsonString = "";
			Scanner scanner = new Scanner(connection.getInputStream());

			while (scanner.hasNext())
				jsonString += scanner.nextLine();

			scanner.close();

			return new JSONObject(jsonString);
		}
		catch (IOException e)
		{
			Logger.log.error("", e);
			return null;
		}
	}

	private long steamIdToSteam64Id(String steamId)
	{
		String[] steamIdSplit = steamId.split(":");
		long steam64Id = (Long.parseLong(steamIdSplit[2]) * 2) + steam64IdIdent;

		return steamIdSplit[1].equals("1") ? (steam64Id + 1) : steam64Id;
	}

	private long steamId3ToSteam64Id(String steamId3)
	{
		String[] steamId3Split = steamId3.replace("[", "").replace("]", "").split(":");

		return Long.parseLong(steamId3Split[2]) + steam64IdIdent;
	}

	private String steam64IdToSteamId(long steam64Id)
	{
		long value = steam64Id - steam64IdIdent;

		return "STEAM_0:" + (value % 2 == 0 ? "0:" : "1:") + Math.floorDiv(value, 2);
	}

	private String steam64IdToSteamId3(long steam64Id)
	{
		return "[U:1:" + (steam64Id - steam64IdIdent) + "]";
	}

	@Override
	public ApplicationCommandRequest getCommandRequest()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Gives you full information about a Steam account")
			.addOption(ApplicationCommandOptionData.builder()
				.name("steamid")
				.description("Any format of Steam ID or profile URL")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.required(true)
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "steam";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}