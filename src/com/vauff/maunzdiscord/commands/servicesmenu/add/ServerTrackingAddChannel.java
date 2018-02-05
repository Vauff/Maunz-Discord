package com.vauff.maunzdiscord.commands.servicesmenu.add;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;
import com.vauff.maunzdiscord.core.Main;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class ServerTrackingAddChannel extends AbstractMenuPage
{
	private boolean retry;

	public ServerTrackingAddChannel(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd, boolean retry)
	{
		super(trigger, cmd, new ServerTrackingSetupPageData());

		this.retry = retry;
	}

	@Override
	public String getTitle()
	{
		return ":heavy_plus_sign:  |  **Add New Service: Server Tracking**";
	}

	@Override
	public String getText()
	{
		return (retry ? "The given channel either didn't exist or was in another guild\n\n" : "") + "Please mention the channel you would like to send server tracking updates in";
	}

	@Override
	public String[] getItems()
	{
		return null;
	}

	@Override
	public void onReplied(MessageReceivedEvent event) throws Exception
	{
		String message = event.getMessage().getContent().replaceAll("[^\\d]", "");

		try
		{
			if (!Main.client.getChannelByID(Long.parseLong(message)).getGuild().equals(event.getGuild()))
			{
				message = "";
			}
		}
		catch (NullPointerException | NumberFormatException e)
		{
			message = "";
		}

		if (!message.equals(""))
		{
			AbstractMenuPage page = new ServerTrackingAddIP(trigger, cmd, handler);

			((ServerTrackingSetupPageData) handler).channel = message;
			show(page);
			waitForReply(page.menu.getStringID(), event.getAuthor().getStringID());
		}
		else
		{
			retry = true;
			show(this);
			waitForReply(menu.getStringID(), trigger.getAuthor().getStringID());
		}
	}
}
