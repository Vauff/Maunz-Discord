package com.vauff.maunzdiscord.commands.slash;

import com.github.koraktor.steamcondenser.servers.SourceServer;
import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.core.Main;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Services extends AbstractSlashCommand<Interaction>
{
	@Override
	public String exe(Interaction interaction, Guild guild, MessageChannel channel, User author) throws Exception
	{
		if (interaction.getCommandInteraction().getOption("add").isPresent())
			return exeAdd(interaction, guild);

		if (interaction.getCommandInteraction().getOption("list").isPresent())
			return exeList(interaction, guild, channel, author);

		if (interaction.getCommandInteraction().getOption("info").isPresent())
			return exeInfo(interaction, guild, channel, author);

		if (interaction.getCommandInteraction().getOption("delete").isPresent())
			return exeDelete(interaction, guild, channel, author);

		if (interaction.getCommandInteraction().getOption("edit").isPresent())
			return exeEdit(interaction, guild, channel, author);

		if (interaction.getCommandInteraction().getOption("toggle").isPresent())
			return exeToggle(interaction, guild, channel, author);

		throw new Exception("Failed to find subcommand");
	}

	private String exeAdd(Interaction interaction, Guild guild) throws Exception
	{
		ApplicationCommandInteractionOption subCmd = interaction.getCommandInteraction().getOption("add").get();
		String ip = subCmd.getOption("ip").get().getValue().get().asString();
		int port;
		Channel channel = null;
		long channelID = 0L;

		try
		{
			String[] ipSplit = ip.split(":");

			if (ipSplit.length > 2)
				throw new Exception();

			ip = ipSplit[0];
			port = Integer.parseInt(ipSplit[1]);
		}
		catch (Exception e)
		{
			return "IP argument does not follow IP:Port format!";
		}

		if (!isServerOnline(ip, port))
			return "Failed to connect to the server " + ip + ":" + port + ", ensure you typed it correctly";

		if (subCmd.getOption("channel").isPresent())
		{
			channel = subCmd.getOption("channel").get().getValue().get().asChannel().block();
			channelID = channel.getId().asLong();
		}

		ObjectId serverId = getOrCreateServer(ip, port);
		Document service = new Document("enabled", true).append("online", true).append("mapCharacterLimit", false).append("lastMap", "N/A").append("serverID", serverId).append("guildID", guild.getId().asLong())
				.append("channelID", channelID).append("notifications", new ArrayList()).append("alwaysShowName", false);

		Main.mongoDatabase.getCollection("services").insertOne(service);

		if (channelID == 0L)
			return "Successfully added a service tracking " + ip + ":" + port + "!";
		else
			return "Successfully added a service tracking " + ip + ":" + port + " in " + channel.getMention() + "!";
	}

	private String exeList(Interaction interaction, Guild guild, MessageChannel channel, User author)
	{
		return "Responding";
	}

	private String exeInfo(Interaction interaction, Guild guild, MessageChannel channel, User author)
	{
		return "Responding";
	}

	private String exeDelete(Interaction interaction, Guild guild, MessageChannel channel, User author)
	{
		return "Responding";
	}

	private String exeEdit(Interaction interaction, Guild guild, MessageChannel channel, User author)
	{
		return "Responding";
	}

	private String exeToggle(Interaction interaction, Guild guild, MessageChannel channel, User author)
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
