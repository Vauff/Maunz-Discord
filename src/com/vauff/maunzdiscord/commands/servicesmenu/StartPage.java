package com.vauff.maunzdiscord.commands.servicesmenu;

import com.vauff.maunzdiscord.commands.servicesmenu.add.AddServicePage;
import com.vauff.maunzdiscord.commands.servicesmenu.delete.DeleteServicePage;
import com.vauff.maunzdiscord.commands.servicesmenu.edit.EditServicePage;
import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class StartPage extends AbstractMenuPage
{
	public StartPage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd)
	{
		super(trigger, cmd);

		addChild(0, new AddServicePage(trigger, cmd));
		addChild(1, new EditServicePage(trigger, cmd));
		addChild(2, new DeleteServicePage(trigger, cmd));
	}

	@Override
	public String getTitle()
	{
		return ":desktop:  |  **Services Menu:**";
	}

	@Override
	public String[] getItems()
	{
		return new String[] { "Add New Service", "Edit Existing Service", "Delete Existing Service" };
	}
}
