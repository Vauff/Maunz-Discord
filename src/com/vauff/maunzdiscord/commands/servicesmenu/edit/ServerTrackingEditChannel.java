package com.vauff.maunzdiscord.commands.servicesmenu.edit;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

import java.io.File;

public class ServerTrackingEditChannel extends AbstractMenuPage
{
	private boolean retry;

	public ServerTrackingEditChannel(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd)
	{
		super(trigger, cmd);
	}

	@Override
	public String getTitle()
	{
		return ":pencil:  |  **Edit Existing Service: Server Tracking**";
	}

	@Override
	public String getText(IChannel channel)
	{
		return (retry ? "The given channel either didn't exist or was in another guild\n\n" : "") + "Please mention the channel (e.g. " + channel.mention() + ") you would like to send server tracking updates in";
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
			File file = new File(Util.getJarLocation() + "data/services/server-tracking/" + event.getGuild().getStringID() + "/serverInfo.json");
			JSONObject json = new JSONObject(Util.getFileContents(file));
			JSONObject innerJson = json.getJSONObject("server0");

			innerJson.put("serverTrackingChannelID", Long.parseLong(message));
			FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
			show(new ServerTrackingEditPage(trigger, cmd));
		}
		else
		{
			retry = true;
			show(this);
			waitForReply(menu.getStringID(), trigger.getAuthor().getStringID());
		}
	}
}
