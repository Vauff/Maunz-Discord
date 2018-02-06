package com.vauff.maunzdiscord.commands.servicesmenu.edit;

import com.vauff.maunzdiscord.commands.servicesmenu.AbstractServiceActionPage;
import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class EditServicePage extends AbstractServiceActionPage
{
	public EditServicePage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd)
	{
		super(trigger, cmd);

		if (guildHasService)
		{
			int i = 0;

			if (services.contains("server-tracking"))
			{
				addChild(i++, new ServerTrackingEditPage(trigger, cmd));
			}

			if (services.contains("csgo-updates"))
			{
				addChild(i, new CSGOUpdatesEditPage(trigger, cmd));
			}
		}
	}

	@Override
	public void show()
	{
		if (!guildHasService)
		{
			Util.msg(trigger.getChannel(), "There are currently no services in this guild to edit!");
		}

		super.show();
	}

	@Override
	public String getTitle()
	{
		return ":pencil:  |  **Edit Existing Service:**";
	}
}
