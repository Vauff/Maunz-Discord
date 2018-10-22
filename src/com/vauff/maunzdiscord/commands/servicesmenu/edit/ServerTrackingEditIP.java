package com.vauff.maunzdiscord.commands.servicesmenu.edit;

import com.github.koraktor.steamcondenser.steam.servers.SourceServer;
import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

import java.io.File;
import java.net.InetAddress;

public class ServerTrackingEditIP extends AbstractMenuPage
{
	private boolean retry;

	public ServerTrackingEditIP(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd)
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
		return (retry ? "The bot was unable to make a connection to a source engine server running on that IP and port\n\n" : "") + "Please type the server's IP in the format of ip:port (e.g. 123.45.678.90:27015)";
	}

	@Override
	public String[] getItems()
	{
		return null;
	}

	@Override
	public void onReplied(MessageReceivedEvent event) throws Exception
	{
		boolean serverOnline = true;
		String message = event.getMessage().getContent();
		String ip = "";
		int port = 0;

		try
		{
			ip = message.split(":")[0];
			port = Integer.parseInt(message.split(":")[1]);
			SourceServer server = new SourceServer(InetAddress.getByName(ip), port);

			server.initialize();
			server.disconnect();
		}
		catch (Exception e)
		{
			serverOnline = false;
		}

		if (serverOnline)
		{
			File file = new File(Util.getJarLocation() + "data/services/server-tracking/" + event.getGuild().getStringID() + "/serverInfo.json");
			JSONObject json = new JSONObject(Util.getFileContents(file));
			JSONObject innerJson = json.getJSONObject("server0");

			innerJson.put("serverIP", ip);
			innerJson.put("serverPort", port);
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
