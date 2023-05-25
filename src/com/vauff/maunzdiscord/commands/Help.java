package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Help extends AbstractCommand<ChatInputInteractionEvent>
{
	private static HashMap<Snowflake, Integer> listPages = new HashMap<>();

	@Override
	public void exe(ChatInputInteractionEvent event, MessageChannel channel, User user) throws Exception
	{
		ApplicationCommandInteraction interaction = event.getInteraction().getCommandInteraction().get();
		long guildId = channel instanceof PrivateChannel ? 0L : event.getInteraction().getGuildId().get().asLong();

		if (interaction.getOption("view").isPresent())
		{
			String arg = interaction.getOption("view").get().getOption("command").get().getValue().get().asString();

			for (AbstractCommand command : Main.commands)
			{
				String cleanArg = "/".equals(arg.substring(0, 1)) ? arg.substring(1) : arg;

				if (command.getPermissionLevel() == BotPermission.GUILD_ADMIN && !Util.hasPermission(user, event.getInteraction().getGuild().block()))
					continue;

				if (command.getPermissionLevel() == BotPermission.BOT_ADMIN && !Util.hasPermission(user))
					continue;

				if (cleanArg.equalsIgnoreCase(command.getName()))
				{
					List<String> helpEntries = getHelp(command, guildId);
					String response = "";

					for (String entry : helpEntries)
						response += entry + System.lineSeparator();

					response = StringUtils.removeEnd(response, System.lineSeparator());
					Util.editReply(event, response);
					return;
				}
			}

			Util.editReply(event, "The command **" + arg + "** either doesn't exist, or you don't have access to it.");
		}
		else if (interaction.getOption("list").isPresent())
		{
			int page = 1;

			if (interaction.getOption("list").get().getOption("page").isPresent())
				page = (int) interaction.getOption("list").get().getOption("page").get().getValue().get().asLong();

			exeList(event, user, guildId, page);
		}
	}

	@Override
	public void buttonPressed(ButtonInteractionEvent event, String buttonId, MessageChannel channel, User user) throws Exception
	{
		int page = listPages.get(user.getId());
		long guildId = channel instanceof PrivateChannel ? 0L : event.getInteraction().getGuildId().get().asLong();

		if (buttonId.equals(NEXT_BTN))
			exeList(event, user, guildId, page + 1);
		else if (buttonId.equals(PREV_BTN))
			exeList(event, user, guildId, page - 1);
	}

	private void exeList(DeferrableInteractionEvent event, User user, long guildId, int page) throws Exception
	{
		List<String> helpEntries = new ArrayList<>();

		for (AbstractCommand<ChatInputInteractionEvent> command : Main.commands)
		{
			if (command.getPermissionLevel() == BotPermission.GUILD_ADMIN && !Util.hasPermission(user, event.getInteraction().getGuild().block()))
				continue;

			if (command.getPermissionLevel() == BotPermission.BOT_ADMIN && !Util.hasPermission(user))
				continue;

			helpEntries.addAll(getHelp(command, guildId));
		}

		buildPage(event, helpEntries, "Command List", 10, 0, page, 0, false, null, "");
		listPages.put(user.getId(), page);
		waitForButtonPress(event.getReply().block().getId(), user.getId());
	}


	private List<String> getHelp(AbstractCommand<ChatInputInteractionEvent> command, long guildId)
	{
		List<String> helpEntries = new ArrayList<>();
		String cmdId = command.getCommandData(guildId).id().asString();

		if (!command.getCommandRequest().options().isAbsent())
		{
			for (ApplicationCommandOptionData option : command.getCommandRequest().options().get())
			{
				if (option.type() == ApplicationCommandOption.Type.SUB_COMMAND.getValue())
				{
					helpEntries.add("</" + command.getCommandRequest().name() + " " + option.name() + ":" + cmdId + "> - " + option.description());
				}
				else if (option.type() == ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
				{
					if (option.options().isAbsent())
						continue;

					for (ApplicationCommandOptionData subCmd : option.options().get())
						helpEntries.add("</" + command.getCommandRequest().name() + " " + option.name() + " " + subCmd.name() + ":" + cmdId + "> - " + subCmd.description());
				}
			}
		}

		if (helpEntries.size() == 0)
			helpEntries.add("</" + command.getCommandRequest().name() + ":" + cmdId + "> - " + command.getCommandRequest().description().get());

		return helpEntries;
	}

	@Override
	public ApplicationCommandRequest getCommandRequest()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Shows available bot commands and the syntax for using each")
			.addOption(ApplicationCommandOptionData.builder()
				.name("list")
				.description("Lists all the available bot commands and the syntax for using each")
				.type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("page")
					.description("The page number to show")
					.type(ApplicationCommandOption.Type.INTEGER.getValue())
					.build())
				.build())
			.addOption(ApplicationCommandOptionData.builder()
				.name("view")
				.description("View a specific commands syntax")
				.type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
				.addOption(ApplicationCommandOptionData.builder()
					.name("command")
					.description("The command to check")
					.type(ApplicationCommandOption.Type.STRING.getValue())
					.required(true)
					.build())
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "help";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}
