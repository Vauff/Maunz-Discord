package com.vauff.maunzdiscord.commands.slash;

import com.github.koraktor.steamcondenser.servers.SourceServer;
import com.github.koraktor.steamcondenser.servers.SteamPlayer;
import com.vauff.maunzdiscord.commands.legacy.Source;
import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Services extends AbstractSlashCommand<Interaction>
{
	@Override
	public String exe(Interaction interaction, MessageChannel channel, User author) throws Exception
	{
		if (interaction.getCommandInteraction().getOption("add").isPresent())
			return exeAdd(interaction, channel, author);

		if (interaction.getCommandInteraction().getOption("list").isPresent())
			return exeList(interaction, channel, author);

		if (interaction.getCommandInteraction().getOption("info").isPresent())
			return exeInfo(interaction, channel, author);

		if (interaction.getCommandInteraction().getOption("delete").isPresent())
			return exeDelete(interaction, channel, author);

		if (interaction.getCommandInteraction().getOption("edit").isPresent())
			return exeEdit(interaction, channel, author);

		if (interaction.getCommandInteraction().getOption("toggle").isPresent())
			return exeToggle(interaction, channel, author);

		throw new Exception("Failed to find subcommand");
	}

	private String exeAdd(Interaction interaction, MessageChannel channel, User author)
	{
		return "Responding";
	}

	private String exeList(Interaction interaction, MessageChannel channel, User author)
	{
		return "Responding";
	}

	private String exeInfo(Interaction interaction, MessageChannel channel, User author)
	{
		return "Responding";
	}

	private String exeDelete(Interaction interaction, MessageChannel channel, User author)
	{
		return "Responding";
	}

	private String exeEdit(Interaction interaction, MessageChannel channel, User author)
	{
		return "Responding";
	}

	private String exeToggle(Interaction interaction, MessageChannel channel, User author)
	{
		return "Responding";
	}

	private boolean isServerOnline(String ip, int port) throws Exception
	{
		SourceServer server = null;
		int attempts = 0;

		while (true)
		{
			try
			{
				server = new SourceServer(InetAddress.getByName(ip), port);
				server.initialize();

				return true;
			}
			catch (Exception e)
			{
				attempts++;

				if (attempts >= 3)
				{
					if (!Objects.isNull(server))
						server.disconnect();

					return false;
				}
				else
				{
					Thread.sleep(1000);
				}
			}
		}
	}

	private ObjectId getOrCreateServer(String ip, int port)
	{
		List<Document> servers = Main.mongoDatabase.getCollection("servers").find(and(eq("ip", ip), eq("port", port))).projection(new Document("enabled", 1)).into(new ArrayList<>());

		if (servers.size() > 0)
		{
			ObjectId id = servers.get(0).getObjectId("_id");

			Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("enabled", true).append("downtimeTimer", 0)));
			return id;
		}

		Document server = new Document("enabled", true).append("ip", ip).append("port", port).append("name", "N/A").append("map", "N/A").append("timestamp", 0L).append("playerCount", "0/0").append("players", new ArrayList())
				.append("downtimeTimer", 0).append("failedConnectionsThreshold", 3).append("mapDatabase", new ArrayList());

		return Main.mongoDatabase.getCollection("servers").insertOne(server).getInsertedId().asObjectId().getValue();
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
