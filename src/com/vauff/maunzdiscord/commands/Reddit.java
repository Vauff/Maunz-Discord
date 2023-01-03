package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Reddit extends AbstractCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User user) throws Exception
	{
		String subreddit = event.getInteraction().getCommandInteraction().get().getOption("subreddit").get().getValue().get().asString();
		String[] splitSub = subreddit.split("/");
		String url;

		if (subreddit.startsWith("/r/"))
			url = splitSub[2] + "/";
		else if (subreddit.startsWith("r/"))
			url = splitSub[1] + "/";
		else
			url = subreddit + "/";

		Document reddit = Jsoup.connect("https://old.reddit.com/r/" + url).ignoreHttpErrors(true).get();

		if (reddit.title().contains(": page not found") || reddit.title().equals("search results"))
			Util.editReply(event, "That subreddit doesn't exist!");
		else if (reddit.title().contains(": banned"))
			Util.editReply(event, "That subreddit is banned!");
		else
			Util.editReply(event, "https://reddit.com/r/" + url);
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