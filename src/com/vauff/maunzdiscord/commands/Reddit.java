package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
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
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User user) throws Exception
	{
		if (accessToken.equals("") || Instant.now().isAfter(accessTokenExpires))
			refreshAccessToken();

		String subreddit = event.getInteraction().getCommandInteraction().get().getOption("subreddit").get().getValue().get().asString();

		if (subreddit.startsWith("/r/"))
			subreddit = subreddit.substring(3);
		else if (subreddit.startsWith("r/"))
			subreddit = subreddit.substring(2);

		JSONObject subredditAbout = getSubredditAbout(subreddit);

		if (accessToken.equals("") || subredditAbout.isEmpty())
		{
			Util.editReply(event, "There was an error connecting to the Reddit API, please try again later");
			return;
		}

		Util.editReply(event, subredditAbout.getString("display_name_prefixed") + " - " + subredditAbout.getString("title"));

		/*Document reddit = Jsoup.connect("https://old.reddit.com/r/" + url).ignoreHttpErrors(true).get();

		if (reddit.title().contains(": page not found") || reddit.title().equals("search results"))
			Util.editReply(event, "That subreddit doesn't exist!");
		else if (reddit.title().contains(": banned"))
			Util.editReply(event, "That subreddit is banned!");
		else
			Util.editReply(event, "https://reddit.com/r/" + url);*/
	}

	private JSONObject getSubredditAbout(String subreddit)
	{
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

			if (connection.getResponseCode() != 200)
				return new JSONObject();

			String jsonString = "";
			Scanner scanner = new Scanner(connection.getInputStream());

			while (scanner.hasNext())
				jsonString += scanner.nextLine();

			scanner.close();
			return new JSONObject(jsonString).getJSONObject("data");
		}
		catch (IOException e)
		{
			return new JSONObject();
		}
	}

	private static void refreshAccessToken() throws Exception
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

	@Override
	public ApplicationCommandRequest getCommandRequest()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Links you to the subreddit name that you provide")
			.addOption(ApplicationCommandOptionData.builder()
				.name("subreddit")
				.description("The subreddit name")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.required(true)
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