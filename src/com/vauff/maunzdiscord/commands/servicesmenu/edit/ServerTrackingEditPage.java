package com.vauff.maunzdiscord.commands.servicesmenu.edit;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.io.File;

public class ServerTrackingEditPage extends AbstractMenuPage
{
	protected File file;
	protected JSONObject json;
	protected JSONObject innerJson;

	public ServerTrackingEditPage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd)
	{
		super(trigger, cmd);

		addChild(0, (event) ->
		{
			innerJson.put("enabled", !innerJson.getBoolean("enabled"));

			try
			{
				FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
				show(this);
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
			}
		});
		addChild(1, (event) ->
		{
			innerJson.put("mapCharacterLimit", !innerJson.getBoolean("mapCharacterLimit"));

			try
			{
				FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
				show(this);
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
			}
		});
		addChild(2, (event) ->
		{
			AbstractMenuPage page = new ServerTrackingEditIP(trigger, cmd);

			try
			{
				show(page);
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
			}

			waitForReply(page.menu.getStringID(), trigger.getAuthor().getStringID());
		});
		addChild(3, (event) ->
		{
			AbstractMenuPage page = new ServerTrackingEditChannel(trigger, cmd);

			try
			{
				show(page);
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
			}

			waitForReply(page.menu.getStringID(), trigger.getAuthor().getStringID());
		});
	}

	@Override
	public void show() throws Exception
	{
		file = new File(Util.getJarLocation() + "data/services/server-tracking/" + trigger.getGuild().getStringID() + "/serverInfo.json");
		json = new JSONObject(Util.getFileContents(file));
		innerJson = json.getJSONObject("server0");
		super.show();
	}

	@Override
	public String getTitle()
	{
		return ":pencil:  |  **Edit Existing Service: Server Tracking**";
	}

	@Override
	public String[] getItems()
	{
		return new String[] {
				"Enabled: " + "**" + StringUtils.capitalize(Boolean.toString(innerJson.getBoolean("enabled"))) + "**",
				"Map Character Limit: " + "**" + StringUtils.capitalize(Boolean.toString(innerJson.getBoolean("mapCharacterLimit"))) + "**",
				"Server IP: " + "**" + innerJson.getString("serverIP") + ":" + innerJson.getInt("serverPort") + "**",
				"Server Tracking Channel: " + "<#" + innerJson.getLong("serverTrackingChannelID") + ">"
		};
	}
}
