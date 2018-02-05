package com.vauff.maunzdiscord.commands.servicesmenu.add;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class CSGOUpdatesEarlyWarnings extends AbstractMenuPage
{
	public CSGOUpdatesEarlyWarnings(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd, IDataHandler handler)
	{
		super(trigger, cmd, handler);

		addChild(0, (event) ->
		{
			((CSGOUpdatesSetupPageData) handler).earlyWarnings = true;
			save();
		});

		addChild(0, (event) ->
		{
			((CSGOUpdatesSetupPageData) handler).earlyWarnings = false;
			save();
		});
	}

	/**
	 * Saves this service (everything from {@link CSGOUpdatesSetupPageData}) into a JSON file
	 */
	private void save()
	{
		try
		{
			File file = new File(Util.getJarLocation() + "data/services/csgo-updates/" + trigger.getGuild().getStringID() + ".json");
			JSONObject json = new JSONObject();

			file.createNewFile();
			json.put("enabled", true);
			json.put("lastGuildName", trigger.getGuild().getName());
			json.put("updateNotificationChannelID", Long.parseLong(((CSGOUpdatesSetupPageData) handler).channel));
			json.put("nonImportantUpdates", Boolean.valueOf(((CSGOUpdatesSetupPageData) handler).showNonImportant));
			json.put("earlyWarnings", ((CSGOUpdatesSetupPageData) handler).earlyWarnings);
			FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
			Util.msg(trigger.getChannel(), "Successfully added the CS:GO Update Notifications service!");
			end();
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}

	@Override
	public String getTitle()
	{
		return ":heavy_plus_sign:  |  **Add New Service: CS:GO Update Notifications**";
	}

	@Override
	public String getText()
	{
		return "Would you like to send notifications for early warnings? (SteamDB updates that might indicate an imminent CS:GO update)";
	}

	@Override
	public String[] getItems()
	{
		return new String[] {
				"Yes",
				"No"
		};
	}
}
