package com.vauff.maunzdiscord.commands.servicesmenu.edit;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class CSGOUpdatesEditPage extends AbstractMenuPage
{
	protected File file;
	protected JSONObject json;

	public CSGOUpdatesEditPage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd)
	{
		super(trigger, cmd);

		addChild(0, (event) -> {
			try
			{
				json.put("enabled", !json.getBoolean("enabled"));
				FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
				show(this);
			}
			catch(Exception e)
			{
				Main.log.error("", e);
			}
		});
		addChild(1, (event) -> {
			try
			{
				json.put("nonImportantUpdates", !json.getBoolean("nonImportantUpdates"));
				FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
				show(this);
			}
			catch(Exception e)
			{
				Main.log.error("", e);
			}
		});
		addChild(2, (event) -> {
			try
			{
				json.put("earlyWarnings", !json.getBoolean("earlyWarnings"));
				FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
				show(this);
			}
			catch(Exception e)
			{
				Main.log.error("", e);
			}
		});
		addChild(3, (event) -> {
			AbstractMenuPage page = new CSGOUpdatesEditChannel(trigger, cmd);

			show(page);
			waitForReply(page.menu.getStringID(), trigger.getAuthor().getStringID());
		});
	}

	@Override
	public void show()
	{
		file = new File(Util.getJarLocation() + "data/services/csgo-updates/" + trigger.getGuild().getStringID() + ".json");
		json = new JSONObject(Util.getFileContents(file));
		super.show();
	}

	@Override
	public String getTitle()
	{
		return ":pencil:  |  **Edit Existing Service: CS:GO Update Notifications**";
	}

	@Override
	public String[] getItems()
	{
		return new String[] {
				"Enabled: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("enabled"))) + "**",
				"Non Important Updates: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("nonImportantUpdates"))) + "**",
				"Early Warnings: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("earlyWarnings"))) + "**",
				"Update Notification Channel: " + "<#" + json.getLong("updateNotificationChannelID") + ">"
		};
	}
}
