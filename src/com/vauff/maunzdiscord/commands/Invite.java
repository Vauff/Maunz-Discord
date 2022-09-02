package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionComponent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.ArrayList;
import java.util.List;

public class Invite extends AbstractCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User user) throws Exception
	{
		List<ActionComponent> components = new ArrayList<>();
		components.add(Button.link("https://discord.com/api/oauth2/authorize?client_id=230780946142593025&permissions=517647752257&scope=bot%20applications.commands", "Bot invite"));
		components.add(Button.link("https://discord.gg/v55fW9b", "Maunz Hub server invite"));

		event.editReply("").withComponents(ActionRow.of(components)).block();
	}

	@Override
	public ApplicationCommandRequest getCommand()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Provides invite links to the Maunz Hub server, and to add Maunz to your own server")
			.build();
	}

	@Override
	public String getName()
	{
		return "invite";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}
