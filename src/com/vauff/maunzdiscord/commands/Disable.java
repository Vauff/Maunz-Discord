package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Disable extends AbstractCommand<MessageReceivedEvent>
{
	private static HashMap<String, String> menuMessages = new HashMap<String, String>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (Util.hasPermission(event.getAuthor(), event.getGuild()))
		{
			File guildFile = new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().getStringID() + ".json");
			JSONObject guildJson = new JSONObject(Util.getFileContents(guildFile));

			if (Util.hasPermission(event.getAuthor()))
			{
				IMessage m = Util.msg(event.getChannel(), event.getAuthor(), "Please select whether you'd like to disable the bot globally or only in this guild" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Disable globally" + System.lineSeparator() + "**`[2]`**  |  Disable in guild only");

				waitForReaction(m.getStringID(), event.getAuthor().getStringID());
				menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
				Util.addNumberedReactions(m, true, 2);

				Executors.newScheduledThreadPool(1).schedule(() ->
				{
					if (!m.isDeleted())
					{
						m.delete();
					}
				}, 120, TimeUnit.SECONDS);
			}
			else
			{
				if (guildJson.getBoolean("enabled"))
				{
					Util.msg(event.getChannel(), event.getAuthor(), "Maunz is now disabled in this guild");
					guildJson.put("enabled", false);
					FileUtils.writeStringToFile(guildFile, guildJson.toString(2), "UTF-8");
				}
				else
				{
					Util.msg(event.getChannel(), event.getAuthor(), "You silly, I was already disabled in this guild!");
				}
			}
		}
		else
		{
			Util.msg(event.getChannel(), event.getAuthor(), "You do not have permission to use that command");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*disable" };
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event) throws Exception
	{
		if (menuMessages.containsKey(event.getUser().getStringID()))
		{
			File botFile = new File(Util.getJarLocation() + "config.json");
			File guildFile = new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().getStringID() + ".json");
			JSONObject botJson = new JSONObject(Util.getFileContents(botFile));
			JSONObject guildJson = new JSONObject(Util.getFileContents(guildFile));

			if (event.getReaction().getEmoji().toString().equals("1⃣"))
			{
				if (botJson.getBoolean("enabled"))
				{
					Util.msg(event.getChannel(), event.getUser(), "Maunz is now disabled globally");
					botJson.put("enabled", false);
					FileUtils.writeStringToFile(botFile, botJson.toString(2), "UTF-8");
				}
				else
				{
					Util.msg(event.getChannel(), event.getUser(), "You silly, I was already disabled globally!");
				}
			}
			else if (event.getReaction().getEmoji().toString().equals("2⃣"))
			{
				if (guildJson.getBoolean("enabled"))
				{
					Util.msg(event.getChannel(), event.getUser(), "Maunz is now disabled in this guild");
					guildJson.put("enabled", false);
					FileUtils.writeStringToFile(guildFile, guildJson.toString(2), "UTF-8");
				}
				else
				{
					Util.msg(event.getChannel(), event.getUser(), "You silly, I was already disabled in this guild!");
				}
			}
		}
	}
}