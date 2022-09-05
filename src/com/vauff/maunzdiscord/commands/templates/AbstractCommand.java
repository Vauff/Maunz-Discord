package com.vauff.maunzdiscord.commands.templates;

import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.objects.Await;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class AbstractCommand<M extends ChatInputInteractionEvent>
{
	/**
	 * Enum holding the different bot permissions commands may require to use
	 *
	 * EVERYONE - No permission required
	 * GUILD_ADMIN - ADMINISTRATOR or MANAGE_GUILD permission required
	 * BOT_ADMIN - User must be listed in config.json botOwners
	 */
	public enum BotPermission
	{
		EVERYONE,
		GUILD_ADMIN,
		BOT_ADMIN
	}

	/**
	 * Button names used for buildPage
	 */
	public final String PREV_BTN = "prevpage";
	public final String NEXT_BTN = "nextpage";

	/**
	 * Holds messages as keys which await a button press by a specific user.
	 * The values hold an instance of {@link Await}
	 */
	public static final HashMap<Snowflake, Await<AbstractCommand>> AWAITED = new HashMap<>();

	/**
	 * Executes this command
	 *
	 * @param interaction The interaction that executing this command creates
	 * @throws Exception If an exception gets thrown by any implementing methods
	 */
	public abstract void exe(M interaction, Guild guild, MessageChannel channel, User user) throws Exception;

	/**
	 * Executes a button attached to this command
	 *
	 * @param event    The ButtonInteractionEvent triggered from pressing a button
	 * @param buttonId The button ID that was pressed
	 * @param guild    Guild the button was pressed in
	 * @param channel  Channel the button was pressed in
	 * @param user     User who pressed the button
	 */
	public void buttonPressed(ButtonInteractionEvent event, String buttonId, Guild guild, MessageChannel channel, User user) throws Exception
	{
	}

	/**
	 * Returns the ApplicationCommandRequest object for this command
	 */
	public abstract ApplicationCommandRequest getCommand();

	/**
	 * Defines the name used to trigger the command.
	 *
	 * @return A string containing the name of the command
	 */
	public abstract String getName();

	/**
	 * Permission level required to use this command
	 *
	 * @return The permission level
	 */
	public abstract BotPermission getPermissionLevel();

	/**
	 * Sets up this command to await a button press by the user who triggered this command
	 *
	 * @param messageID The message which has buttons attached
	 * @param userID    The user who triggered this command
	 */
	public final void waitForButtonPress(Snowflake messageID, Snowflake userID)
	{
		AWAITED.put(messageID, new Await(userID, this));
	}

	/**
	 * Builds a modular page message in response to an interaction for the given parameters
	 *
	 * @param event         The DeferrableInteractionEvent that is being responded to
	 * @param elements      A List that contains the elements making up all the pages
	 * @param title         The title to give all the pages
	 * @param pageSize      How many elements (strings) or rows (buttons) should be in a specific page
	 * @param buttonsPerRow How many buttons to display on each row, ignored for string elements
	 * @param pageNumber    Which page the method should build and send
	 * @param numberStyle   Which style to use for entries, ignored for button elements. 0 = none 1 = numbered list in code ticks
	 * @param codeBlock     Whether to surround all the entries in a code block or not, ignored for button elements
	 * @param button        A button that should be displayed on every page
	 * @param pageBtnSuffix Suffix that should be appended to clickable page button names, if a command needs to differentiate between different page types
	 */
	public final void buildPage(DeferrableInteractionEvent event, List<?> elements, String title, int pageSize, int buttonsPerRow, int pageNumber, int numberStyle, boolean codeBlock, Button button, String pageBtnSuffix) throws Exception
	{
		float rawPages = (float) elements.size() / pageSize;
		int pages = (int) Math.ceil(rawPages);

		if (pageNumber > pages)
		{
			Util.editReply(event, "That page doesn't exist!");
			return;
		}

		boolean buttonElements = elements.get(0) instanceof Button;
		int firstElementIndex = (int) (elements.size() - ((rawPages - (pageNumber - 1)) * pageSize));
		int lastElementIndex = firstElementIndex + pageSize;
		String elementsString = "";
		List<LayoutComponent> buttonRows = new ArrayList<>();
		List<Button> buttons = new ArrayList<>();

		if (codeBlock && !buttonElements)
			elementsString += "```" + System.lineSeparator();

		for (int i = firstElementIndex; i < lastElementIndex; i++)
		{
			if (i > elements.size() - 1)
				break;

			if (buttonElements)
			{
				buttons.add((Button) elements.get(i));

				if (buttons.size() == buttonsPerRow || i == lastElementIndex - 1 || i == elements.size() - 1)
				{
					buttonRows.add(ActionRow.of(buttons));
					buttons.clear();
				}
			}
			else
			{
				switch (numberStyle)
				{
					case 0 -> elementsString += elements.get(i) + System.lineSeparator();
					case 1 -> elementsString += "`[" + (i + 1) + "]` | " + elements.get(i) + System.lineSeparator();
					default -> throw new Exception("Bad numberStyle " + numberStyle + " passed into buildPage");
				}
			}
		}

		if (codeBlock && !buttonElements)
			elementsString += "```";

		String formattedTitle = "**--- " + title + " ---**" + System.lineSeparator();
		List<Button> pageButtons = new ArrayList<>();

		if (pages > 1)
		{
			// Using U+2800 BRAILLE PATTERN BLANK to get spacing in button text
			pageButtons.add(Button.secondary(PREV_BTN + pageBtnSuffix, "◀⠀Previous").disabled(pageNumber == 1));
			pageButtons.add(Button.secondary("pagenumber", "Page " + pageNumber + "/" + pages).disabled());
			pageButtons.add(Button.secondary(NEXT_BTN + pageBtnSuffix, "Next⠀▶").disabled(pageNumber == pages));
		}

		if (!Objects.isNull(button))
			buttonRows.add(ActionRow.of(button));

		if (pageButtons.size() > 0)
			buttonRows.add(ActionRow.of(pageButtons));

		Util.editReply(event, formattedTitle + elementsString, null, buttonRows);
	}
}
