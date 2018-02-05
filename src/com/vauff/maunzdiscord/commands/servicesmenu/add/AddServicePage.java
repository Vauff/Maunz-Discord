package com.vauff.maunzdiscord.commands.servicesmenu.add;

import com.vauff.maunzdiscord.commands.servicesmenu.AbstractServiceActionPage;
import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class AddServicePage extends AbstractServiceActionPage
{
	public AddServicePage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd)
	{
		super(trigger, cmd);

		if(services.size() != 2)
		{
			int i = 0;

			if (!services.contains("server-tracking"))
			{
				addChild(i++, (event) -> {
					AbstractMenuPage page = new ServerTrackingAddChannel(trigger, cmd, false);

					show(page);
					waitForReply(page.menu.getStringID(), trigger.getAuthor().getStringID());
				});
			}

			if (!services.contains("csgo-updates"))
			{
				addChild(i, (event) -> {
					AbstractMenuPage page = new CSGOUpdatesAddChannel(trigger, cmd, false);

					show(page);
					waitForReply(page.menu.getStringID(), trigger.getAuthor().getStringID());
				});
			}
		}
	}

	@Override
	public void show()
	{
		if(services.size() == 2)
		{
			Util.msg(trigger.getChannel(), "There are no more services to add!");
			end();
		}

		super.show();
	}

	@Override
	public String getTitle()
	{
		return ":heavy_plus_sign:  |  **Add New Service:**";
	}

	@Override
	public String[] getItems()
	{
		String add = "";

		if (!services.contains("server-tracking"))
		{
			add += "Server Tracking,";
		}

		if (!services.contains("csgo-updates"))
		{
			add += "CS:GO Update Notifications";
		}

		return add.split(",");
	}
}
