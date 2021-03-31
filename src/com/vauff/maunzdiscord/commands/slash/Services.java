package com.vauff.maunzdiscord.commands.slash;

import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;

public class Services extends AbstractSlashCommand<ApplicationCommandInteraction>
{
	@Override
	public String exe(ApplicationCommandInteraction interaction, MessageChannel channel, User author) throws Exception
	{
		return "Responding";
	}

	@Override
	public ApplicationCommandRequest getCommand()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Master command for managing services on a guild")
			.addOption(ApplicationCommandOptionData.builder()
				.name("add")
				.description("Add a new service")
				.type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("ip")
					.description("The servers IP in IP:Port format")
					.type(ApplicationCommandOptionType.STRING.getValue())
					.required(true)
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("channel")
					.description("The channel to send server tracking messages to")
					.type(ApplicationCommandOptionType.CHANNEL.getValue())
					.build())
				.build())
			.addOption(ApplicationCommandOptionData.builder()
				.name("list")
				.description("List current services")
				.type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("page")
					.description("The page number to show")
					.type(ApplicationCommandOptionType.INTEGER.getValue())
					.build())
				.build())
			.addOption(ApplicationCommandOptionData.builder()
				.name("info")
				.description("View full info about a specific service")
				.type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("id")
					.description("The service ID to view info for")
					.type(ApplicationCommandOptionType.INTEGER.getValue())
					.required(true)
					.build())
				.build())
			.addOption(ApplicationCommandOptionData.builder()
				.name("delete")
				.description("Delete a service")
				.type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("id")
					.description("The service ID to delete")
					.type(ApplicationCommandOptionType.INTEGER.getValue())
					.required(true)
					.build())
				.build())
			.addOption(ApplicationCommandOptionData.builder()
				.name("edit")
				.description("Edit a service value (only pick what you're changing)")
				.type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("id")
					.description("The service ID to edit")
					.type(ApplicationCommandOptionType.INTEGER.getValue())
					.required(true)
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("ip")
					.description("The servers IP in IP:Port format")
					.type(ApplicationCommandOptionType.STRING.getValue())
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("channel")
					.description("The channel to send server tracking messages to")
					.type(ApplicationCommandOptionType.CHANNEL.getValue())
					.build())
				.build())
			.addOption(ApplicationCommandOptionData.builder()
				.name("toggle")
				.description("Toggle a service value")
				.type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("id")
					.description("The service ID to edit")
					.type(ApplicationCommandOptionType.INTEGER.getValue())
					.required(true)
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("option")
					.description("The option to toggle")
					.type(ApplicationCommandOptionType.STRING.getValue())
					.addChoice(ApplicationCommandOptionChoiceData.builder()
						.name("Enabled")
						.value("enabled")
						.build())
					.addChoice(ApplicationCommandOptionChoiceData.builder()
						.name("Map Character Limit")
						.value("mapcharacterlimit")
						.build())
					.addChoice(ApplicationCommandOptionChoiceData.builder()
						.name("Always Show Server Name")
						.value("alwaysshowname")
						.build())
					.required(true)
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("value")
					.description("The value to use")
					.type(ApplicationCommandOptionType.BOOLEAN.getValue())
					.required(true)
					.build())
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "services";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.GUILD_ADMIN;
	}
}
