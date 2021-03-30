package com.vauff.maunzdiscord.commands.slash;

import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
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
					.required(false)
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
					.required(false)
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
				.description("Edit service values (only pick what you're changing)")
				.type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("id")
					.description("The service ID to edit")
					.type(ApplicationCommandOptionType.INTEGER.getValue())
					.required(true)
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("enabled")
					.description("Whether the service is enabled")
					.type(ApplicationCommandOptionType.BOOLEAN.getValue())
					.required(false)
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("ip")
					.description("The servers IP in IP:Port format")
					.type(ApplicationCommandOptionType.STRING.getValue())
					.required(false)
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("channel")
					.description("The channel to send server tracking messages to")
					.type(ApplicationCommandOptionType.CHANNEL.getValue())
					.required(false)
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("mapcharacterlimit")
					.description("Whether map names should be trimmed to 31 characters, only enable this if you know you need it")
					.type(ApplicationCommandOptionType.BOOLEAN.getValue())
					.required(false)
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("alwaysshowname")
					.description("Whether to always show the server name")
					.type(ApplicationCommandOptionType.BOOLEAN.getValue())
					.required(false)
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
