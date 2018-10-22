package com.vauff.maunzdiscord.commands.servicesmenu.edit;

import com.vauff.maunzdiscord.commands.servicesmenu.AbstractServiceActionPage;
import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class EditServicePage extends AbstractServiceActionPage
{
	public EditServicePage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd) throws Exception
	{
		super(trigger, cmd);

		if (guildHasService)
		{
			int i = 0;

			if (services.contains("server-tracking"))
			{
				addChild(i++, new ServerTrackingEditPage(trigger, cmd));
			}
		}
	}

	@Override
	public void show() throws Exception
	{
		if (!guildHasService)
		{
			Util.msg(trigger.getChannel(), "There are currently no services in this guild to edit!");
		}
		else
		{
			super.show();
		}
	}

	@Override
	public String getTitle()
	{
		return ":pencil:  |  **Edit Existing Service:**";
	}
}
