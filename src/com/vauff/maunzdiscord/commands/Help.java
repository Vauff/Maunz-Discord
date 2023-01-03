package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.objects.CommandHelp;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Help extends AbstractCommand<ChatInputInteractionEvent>
{
	private static HashMap<Snowflake, Integer> listPages = new HashMap<>();

	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User user) throws Exception
	{
		ApplicationCommandInteraction interaction = event.getInteraction().getCommandInteraction().get();

		if (interaction.getOption("view").isPresent())
		{
			String arg = interaction.getOption("view").get().getOption("command").get().getValue().get().asString();
			String list = "";
			boolean matchFound = false;

			for (AbstractCommand command : Main.commands)
			{
				String cleanArg = "/".equals(arg.substring(0, 1)) ? arg.substring(1) : arg;

				if (command.getPermissionLevel() == BotPermission.GUILD_ADMIN && !Util.hasPermission(user, guild))
					continue;

				if (command.getPermissionLevel() == BotPermission.BOT_ADMIN && !Util.hasPermission(user))
					continue;

				if (cleanArg.equalsIgnoreCase(command.getName()))
				{
					matchFound = true;

					for (CommandHelp commandHelp : getHelp(command))
						list += "**/" + command.getName() + (commandHelp.getArguments().equals("") ? "" : " " + commandHelp.getArguments()) + "** - " + commandHelp.getDescription() + System.lineSeparator();

					list = StringUtils.removeEnd(list, System.lineSeparator());
					break;
				}
			}

			if (matchFound)
				Util.editReply(event, list);
			else
				Util.editReply(event, "The command **" + arg + "** either doesn't exist, or you don't have access to it.");
		}
		else if (interaction.getOption("list").isPresent())
		{
			int page = 1;

			if (interaction.getOption("list").get().getOption("page").isPresent())
				page = (int) interaction.getOption("list").get().getOption("page").get().getValue().get().asLong();

			exeList(event, guild, user, page);
		}
	}

	@Override
	public void buttonPressed(ButtonInteractionEvent event, String buttonId, Guild guild, MessageChannel channel, User user) throws Exception
	{
		int page = listPages.get(user.getId());

		if (buttonId.equals(NEXT_BTN))
			exeList(event, guild, user, page + 1);
		else if (buttonId.equals(PREV_BTN))
			exeList(event, guild, user, page - 1);
	}

	private void exeList(DeferrableInteractionEvent event, Guild guild, User user, int page) throws Exception
	{
		ArrayList<String> helpEntries = new ArrayList<>();

		for (AbstractCommand command : Main.commands)
		{
			if (command.getPermissionLevel() == BotPermission.GUILD_ADMIN && !Util.hasPermission(user, guild))
				continue;

			if (command.getPermissionLevel() == BotPermission.BOT_ADMIN && !Util.hasPermission(user))
				continue;

			for (CommandHelp commandHelp : getHelp(command))
				helpEntries.add("**/" + command.getName() + (commandHelp.getArguments().equals("") ? "" : " " + commandHelp.getArguments()) + "** - " + commandHelp.getDescription());
		}

		buildPage(event, helpEntries, "Command List", 10, 0, page, 0, false, null, "");
		listPages.put(user.getId(), page);
		waitForButtonPress(event.getReply().block().getId(), user.getId());
	}

	private CommandHelp[] getHelp(AbstractCommand command)
	{
		if (command.getCommand().options().isAbsent())
			return new CommandHelp[] { new CommandHelp("", command.getCommand().description().get()) };

		List<CommandHelp> commandHelps = new ArrayList<>();
		CommandHelp commandHelp = new CommandHelp("", command.getCommand().description().get());
		boolean noSubCmds = true;

		for (ApplicationCommandOptionData option : command.getCommand().options().get())
		{
			if (option.type() == ApplicationCommandOption.Type.SUB_COMMAND.getValue())
			{
				noSubCmds = false;

				commandHelps.add(parseHelpSubCommand(option, null));
			}
			else if (option.type() == ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
			{
				noSubCmds = false;

				if (option.options().isAbsent())
					continue;

				for (ApplicationCommandOptionData subCmd : option.options().get())
					commandHelps.add(parseHelpSubCommand(subCmd, option.name()));
			}
			else
			{
				if (option.required().isAbsent() || !option.required().get())
					commandHelp.setArguments(commandHelp.getArguments() + "[" + option.name() + "] ");
				else
					commandHelp.setArguments(commandHelp.getArguments() + "<" + option.name() + "> ");
			}
		}

		if (noSubCmds)
		{
			commandHelp.setArguments(commandHelp.getArguments().trim());
			commandHelps.add(commandHelp);
		}

		return commandHelps.toArray(new CommandHelp[commandHelps.size()]);
	}

	private CommandHelp parseHelpSubCommand(ApplicationCommandOptionData subCommand, String rootName)
	{
		CommandHelp commandHelp = new CommandHelp("", subCommand.description());

		if (!Objects.isNull(rootName))
			commandHelp.setArguments(rootName + " ");

		commandHelp.setArguments(commandHelp.getArguments() + subCommand.name() + " ");

		if (!subCommand.options().isAbsent())
		{
			for (ApplicationCommandOptionData option : subCommand.options().get())
			{
				if (option.required().isAbsent() || !option.required().get())
					commandHelp.setArguments(commandHelp.getArguments() + "[" + option.name() + "] ");
				else
					commandHelp.setArguments(commandHelp.getArguments() + "<" + option.name() + "> ");
			}
		}

		commandHelp.setArguments(commandHelp.getArguments().trim());

		return commandHelp;
	}

	@Override
	public ApplicationCommandRequest getCommand()
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
