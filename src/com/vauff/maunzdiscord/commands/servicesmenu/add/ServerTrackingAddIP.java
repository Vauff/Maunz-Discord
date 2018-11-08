package com.vauff.maunzdiscord.commands.servicesmenu.add;

import com.github.koraktor.steamcondenser.steam.servers.SourceServer;
import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

import java.io.File;
import java.net.InetAddress;

public class ServerTrackingAddIP extends AbstractMenuPage
{
	private boolean retry;

	public ServerTrackingAddIP(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd, IDataHandler handler)
	{
		super(trigger, cmd, handler);
	}

	@Override
	public String getTitle()
	{
		return ":heavy_plus_sign:  |  **Add New Service: Server Tracking**";
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
			Logger.log.error("", e);
		}

		if (serverOnline)
		{
			File folder = new File(Util.getJarLocation() + "data/services/server-tracking/" + event.getGuild().getStringID() + "/");
			File file = new File(Util.getJarLocation() + "data/services/server-tracking/" + event.getGuild().getStringID() + "/serverInfo.json");
			JSONObject json = new JSONObject();

			folder.mkdir();
			file.createNewFile();
			json.put("lastGuildName", event.getGuild().getName());
			json.put("server0", new JSONObject());

			JSONObject innerJson = json.getJSONObject("server0");

			innerJson.put("mapDatabase", new JSONArray());
			innerJson.put("serverTrackingChannelID", Long.parseLong(((ServerTrackingSetupPageData) handler).channel));
			innerJson.put("downtimeTimer", 0);
			innerJson.put("serverName", "");
			innerJson.put("players", "0/0");
			innerJson.put("lastMap", "N/A");
			innerJson.put("serverIP", ip);
			innerJson.put("serverPort", port);
			innerJson.put("enabled", true);
			innerJson.put("timestamp", 1);
			innerJson.put("mapCharacterLimit", false);
			innerJson.put("failedConnectionsThreshold", 3);
			FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
			Util.msg(event.getChannel(), "Successfully added the Server Tracking service!");
			end();
		}
		else
		{
			retry = true;
			show(this);
			waitForReply(menu.getStringID(), trigger.getAuthor().getStringID());
		}
	}
}
