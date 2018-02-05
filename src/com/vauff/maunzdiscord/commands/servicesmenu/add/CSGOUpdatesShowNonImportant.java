package com.vauff.maunzdiscord.commands.servicesmenu.add;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class CSGOUpdatesShowNonImportant extends AbstractMenuPage
{
	public CSGOUpdatesShowNonImportant(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd, IDataHandler handler)
	{
		super(trigger, cmd, handler);

		addChild(0, (event) ->
		{
			((CSGOUpdatesSetupPageData) handler).showNonImportant = true;
			show(new CSGOUpdatesEarlyWarnings(trigger, cmd, handler));
		});

		addChild(0, (event) ->
		{
			((CSGOUpdatesSetupPageData) handler).showNonImportant = false;
			show(new CSGOUpdatesEarlyWarnings(trigger, cmd, handler));
		});
	}

	@Override
	public String getTitle()
	{
		return ":heavy_plus_sign:  |  **Add New Service: CS:GO Update Notifications**";
	}

	@Override
	public String getText()
	{
		return "Would you like to send notifications for non-important updates? (SteamDB updates that don't really mean anything)";
	}

	@Override
	public String[] getItems()
	{
		return new String[] { "Yes", "No" };
	}
}
