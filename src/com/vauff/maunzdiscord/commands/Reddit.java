package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class Reddit extends AbstractCommand<ChatInputInteractionEvent>
{
	private static String accessToken = "";
	private static Instant accessTokenExpires = Instant.ofEpochMilli(0L);

	@Override
	public void exe(ChatInputInteractionEvent event, MessageChannel channel, User user) throws Exception
	{
		if (Main.cfg.getRedditId().equals("") || Main.cfg.getRedditSecret().equals(""))
		{
			Util.editReply(event, "This command is disabled because the bot does not have Reddit API credentials configured!");
			return;
		}

		String subreddit = event.getInteraction().getCommandInteraction().get().getOption("subreddit").get().getValue().get().asString();

		if (subreddit.startsWith("/r/"))
			subreddit = subreddit.substring(3);
		else if (subreddit.startsWith("r/"))
			subreddit = subreddit.substring(2);

		JSONObject about = getSubredditAbout(subreddit);

		if (about.has("error"))
		{
			switch (about.getString("reason"))
			{
				case "banned" -> Util.editReply(event, "**r/" + subreddit + "** is banned!");
				case "private" -> Util.editReply(event, "**r/" + subreddit + "** is private!");
				case "gold_only" -> Util.editReply(event, "**r/" + subreddit + "** is private to Reddit Premium members only!");
				default -> Util.editReply(event, "There was an error connecting to the Reddit API, please try again later");
			}

			return;
		}

		if (about.has("kind") && about.getString("kind").equals("Listing"))
		{
			Util.editReply(event, "**r/" + subreddit + "** does not exist!");
			return;
		}

		if (about.isEmpty() || !about.getString("kind").equals("t5"))
		{
			Util.editReply(event, "There was an error connecting to the Reddit API, please try again later");
			return;
		}

		JSONObject data = about.getJSONObject("data");

		if (data.getBoolean("over18") && channel.getType().equals(Channel.Type.GUILD_TEXT) && !((TextChannel) channel).isNsfw())
		{
			Util.editReply(event, "You cannot view NSFW subreddits in a non-NSFW channel");
			return;
		}

		String headerImg = "";

		// Dumb workaround for Reddit RANDOMLY returning null header_img, even though it will return an empty string other times (for the same subreddits even!)
		if (!data.isNull("header_img"))
			headerImg = data.getString("header_img");

		String imgUrl = data.getString("icon_img").equals("") ? headerImg : data.getString("icon_img");

		EmbedCreateSpec embed = EmbedCreateSpec.builder()
			.color(Util.averageColourFromURL(imgUrl, true))
			.thumbnail(imgUrl)
			.title(data.getString("display_name_prefixed") + " - " + data.getString("title"))
			.url("https://reddit.com" + data.getString("url"))
			.description(data.getString("public_description"))
			.footer("Reddit", "https://i.imgur.com/tHtZmQA.png")
			.addField("Subscribers", String.valueOf(data.getInt("subscribers")), true)
			.addField("Users Online", String.valueOf(data.getInt("accounts_active")), true)
			.build();

		Util.editReply(event, "", embed);
	}

	private JSONObject getSubredditAbout(String subreddit)
	{
		if (accessToken.equals("") || Instant.now().isAfter(accessTokenExpires))
			refreshAccessToken();

		if (accessToken.equals(""))
			return new JSONObject();

		try
		{
			URL url = new URL("https://oauth.reddit.com/r/" + subreddit + "/about");
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Maunz-Discord " + Main.version);
			connection.setRequestProperty("Authorization", "Bearer " + accessToken);
			connection.connect();

			String jsonString = "";
			Scanner scanner;

			if (connection.getResponseCode() == 200)
				scanner = new Scanner(connection.getInputStream());
			else
				scanner = new Scanner(connection.getErrorStream());

			while (scanner.hasNext())
				jsonString += scanner.nextLine();

			scanner.close();

			return new JSONObject(jsonString);
		}
		catch (JSONException e)
		{
			return new JSONObject();
		}
		catch (IOException e)
		{
			Logger.log.error("", e);
			return new JSONObject();
		}
	}

	private static void refreshAccessToken()
	{
		try
		{
			HttpClient client = HttpClient.newBuilder().authenticator(new Authenticator()
			{
				@Override
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(Main.cfg.getRedditId(), Main.cfg.getRedditSecret().toCharArray());
				}
			}).build();

			HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials")).uri(new URI("https://www.reddit.com/api/v1/access_token")).build();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

			if (response.statusCode() == 200)
			{
				JSONObject jsonResponse = new JSONObject(response.body());

				accessTokenExpires = Instant.now().plus(jsonResponse.getLong("expires_in"), ChronoUnit.SECONDS);
				accessToken = jsonResponse.getString("access_token");
			}
			else
			{
				accessToken = "";
			}
		}
		catch (IOException | InterruptedException | URISyntaxException e)
		{
			Logger.log.error("", e);
			accessToken = "";
		}
	}

	@Override
	public ApplicationCommandRequest getCommandRequest()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Provides information about a subreddit")
			.addOption(ApplicationCommandOptionData.builder()
				.name("subreddit")
				.description("The subreddit name")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.required(true)
				.minLength(3)
				.maxLength(21)
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "reddit";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}