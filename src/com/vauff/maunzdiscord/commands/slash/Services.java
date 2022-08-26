package com.vauff.maunzdiscord.commands.slash;

import com.github.koraktor.steamcondenser.servers.SourceServer;
import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Services extends AbstractSlashCommand<ChatInputInteractionEvent>
{
	private static HashMap<Snowflake, List<Document>> listServices = new HashMap<>();
	private static HashMap<Snowflake, Integer> listPages = new HashMap<>();

	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User author) throws Exception
	{
		ApplicationCommandInteraction interaction = event.getInteraction().getCommandInteraction().get();

		if (channel instanceof PrivateChannel)
		{
			event.editReply("This command can't be done in a PM, only in a guild that you have admin permissions in").block();
			return;
		}

		if (interaction.getOption("add").isPresent())
			exeAdd(event, guild);
		else if (interaction.getOption("list").isPresent())
			exeList(event, guild, author);
		else if (interaction.getOption("info").isPresent())
			exeInfo(event, guild, channel, author);
		else if (interaction.getOption("delete").isPresent())
			exeDelete(event, author);
		else if (interaction.getOption("edit").isPresent())
			exeEdit(event, guild, channel, author);
		else if (interaction.getOption("toggle").isPresent())
			exeToggle(event, guild, channel, author);
	}

	@Override
	public void buttonExe(ButtonInteractionEvent event, String buttonId)
	{
		User user = event.getInteraction().getUser();
		int page = listPages.get(user.getId());

		if (buttonId.equals("services-nextpage"))
			runListSelection(event, user, page + 1);
		else if (buttonId.equals("services-prevpage"))
			runListSelection(event, user, page - 1);
	}

	private void exeAdd(ChatInputInteractionEvent event, Guild guild) throws Exception
	{
		ApplicationCommandInteractionOption subCmd = event.getInteraction().getCommandInteraction().get().getOption("add").get();
		String ip = subCmd.getOption("ip").get().getValue().get().asString();
		int port;
		Channel channel = subCmd.getOption("channel").get().getValue().get().asChannel().block();
		long channelID = channel.getId().asLong();

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
			event.editReply("IP argument does not follow IP:Port format!").block();
			return;
		}

		if (!isServerOnline(ip, port))
		{
			event.editReply("Failed to connect to the server " + ip + ":" + port + ", ensure you typed it correctly").block();
			return;
		}

		ObjectId serverId = getOrCreateServer(ip, port);
		Document service = new Document("enabled", true).append("online", true).append("mapCharacterLimit", false).append("lastMap", "N/A").append("serverID", serverId).append("guildID", guild.getId().asLong())
			.append("channelID", channelID).append("notifications", new ArrayList()).append("alwaysShowName", false);

		Main.mongoDatabase.getCollection("services").insertOne(service);

		event.editReply("Successfully added a service tracking " + ip + ":" + port + " in " + channel.getMention() + "!").block();
	}

	private void exeList(ChatInputInteractionEvent event, Guild guild, User author)
	{
		ApplicationCommandInteractionOption subCmd = event.getInteraction().getCommandInteraction().get().getOption("list").get();
		List<Document> services = Main.mongoDatabase.getCollection("services").find(eq("guildID", guild.getId().asLong())).projection(new Document("enabled", 1).append("serverID", 1).append("channelID", 1)).into(new ArrayList<>());

		if (services.size() == 0)
		{
			event.editReply("No services have been added yet! Use **/services add** to add one").block();
			return;
		}

		listServices.put(author.getId(), services);

		if (subCmd.getOption("page").isPresent())
			runListSelection(event, author, (int) subCmd.getOption("page").get().getValue().get().asLong());
		else
			runListSelection(event, author, 1);
	}

	private void exeInfo(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User author)
	{
		event.editReply("This feature is not yet implemented").block();
	}

	private void exeDelete(ChatInputInteractionEvent event, User author)
	{
		ApplicationCommandInteractionOption subCmd = event.getInteraction().getCommandInteraction().get().getOption("delete").get();
		List<Document> services = listServices.get(author.getId());
		int id = (int) subCmd.getOption("id").get().getValue().get().asLong();

		if (!listServices.containsKey(author.getId()) || services.size() < id)
		{
			event.editReply("That service ID doesn't exist! Have you ran **/services list** yet to generate IDs?").block();
			return;
		}

		Document service = services.get(id - 1);

		if (Objects.isNull(service))
		{
			event.editReply("That service was already deleted!").block();
			return;
		}

		Main.mongoDatabase.getCollection("services").deleteOne(eq("_id", service.getObjectId("_id")));

		Channel serviceChannel;
		Document server = Main.mongoDatabase.getCollection("servers").find(eq("_id", service.getObjectId("serverID"))).projection(new Document("ip", 1).append("port", 1)).first();
		String msg = "Successfully deleted the service tracking " + server.getString("ip") + ":" + server.getInteger("port");

		try
		{
			serviceChannel = Main.gateway.getChannelById(Snowflake.of(service.getLong("channelID"))).block();
		}
		catch (Exception e)
		{
			serviceChannel = null;
		}

		if (!Objects.isNull(serviceChannel))
			msg += " in " + serviceChannel.getMention();

		event.editReply(msg).block();
	}

	private void exeEdit(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User author)
	{
		event.editReply("This feature is not yet implemented").block();
	}

	private void exeToggle(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User author)
	{
		event.editReply("This feature is not yet implemented").block();
	}

	private void runListSelection(DeferrableInteractionEvent event, User author, int page)
	{
		ArrayList<String> servicesDisplay = new ArrayList<>();
		int id = 0;

		for (Document service : listServices.get(author.getId()))
		{
			id++;

			Document server = Main.mongoDatabase.getCollection("servers").find(eq("_id", service.getObjectId("serverID"))).projection(new Document("ip", 1).append("port", 1)).first();
			Channel serviceChannel;
			String msg = id + " - " + server.getString("ip") + ":" + server.getInteger("port");

			try
			{
				serviceChannel = Main.gateway.getChannelById(Snowflake.of(service.getLong("channelID"))).block();
			}
			catch (Exception e)
			{
				serviceChannel = null;
			}

			if (!Objects.isNull(serviceChannel))
				msg += " tracking in " + serviceChannel.getMention();

			if (!service.getBoolean("enabled"))
				msg += " (disabled)";

			servicesDisplay.add(msg);
		}

		listPages.put(author.getId(), page);
		Util.buildPage(servicesDisplay, "Services", 10, page, 0, false, false, false, event, prevBtn, nextBtn);
		waitForButtonPress(event.getReply().block().getId(), author.getId());
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
				.type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("ip")
					.description("The servers IP in IP:Port format")
					.type(ApplicationCommandOption.Type.STRING.getValue())
					.required(true)
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("channel")
					.description("The channel to send server tracking messages to")
					.type(ApplicationCommandOption.Type.CHANNEL.getValue())
					.channelTypes(Arrays.asList(Channel.Type.GUILD_TEXT.getValue(), Channel.Type.GUILD_NEWS.getValue()))
					.required(true)
					.build())
				.build())
			.addOption(ApplicationCommandOptionData.builder()
				.name("list")
				.description("List current services")
				.type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("page")
					.description("The page number to show")
					.type(ApplicationCommandOption.Type.INTEGER.getValue())
					.build())
				.build())
			/*.addOption(ApplicationCommandOptionData.builder()
				.name("info")
				.description("View full info about a specific service")
				.type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("id")
					.description("The service ID to view info for")
					.type(ApplicationCommandOption.Type.INTEGER.getValue())
					.required(true)
					.build())
				.build())*/
			.addOption(ApplicationCommandOptionData.builder()
				.name("delete")
				.description("Delete a service")
				.type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("id")
					.description("The service ID to delete")
					.type(ApplicationCommandOption.Type.INTEGER.getValue())
					.required(true)
					.build())
				.build())
			/*.addOption(ApplicationCommandOptionData.builder()
				.name("edit")
				.description("Edit a service value (only pick what you're changing)")
				.type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("id")
					.description("The service ID to edit")
					.type(ApplicationCommandOption.Type.INTEGER.getValue())
					.required(true)
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("ip")
					.description("The servers IP in IP:Port format")
					.type(ApplicationCommandOption.Type.STRING.getValue())
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("channel")
					.description("The channel to send server tracking messages to")
					.type(ApplicationCommandOption.Type.CHANNEL.getValue())
					.build())
				.build())
			.addOption(ApplicationCommandOptionData.builder()
				.name("toggle")
				.description("Toggle a service value")
				.type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("id")
					.description("The service ID to edit")
					.type(ApplicationCommandOption.Type.INTEGER.getValue())
					.required(true)
					.build())
				.addOption(ApplicationCommandOptionData.builder()
					.name("option")
					.description("The option to toggle")
					.type(ApplicationCommandOption.Type.STRING.getValue())
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
					.type(ApplicationCommandOption.Type.BOOLEAN.getValue())
					.required(true)
					.build())
				.build())*/
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
