package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Disable extends AbstractCommand<MessageCreateEvent>
{
	private static HashMap<String, String> menuMessages = new HashMap<>();

	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		if (Util.hasPermission(author, event.getGuild().block()))
		{
			File guildFile = new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().block().getId().asString() + ".json");
			JSONObject guildJson = new JSONObject(Util.getFileContents(guildFile));

			if (Util.hasPermission(author))
			{
				Message m = Util.msg(channel, author, "Please select whether you'd like to disable the bot globally or only in this guild" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Disable globally" + System.lineSeparator() + "**`[2]`**  |  Disable in guild only");

				waitForReaction(m.getId().asString(), author.getId().asString());
				menuMessages.put(author.getId().asString(), m.getId().asString());
				Util.addNumberedReactions(m, true, 2);

				ScheduledExecutorService msgDeleterPool = Executors.newScheduledThreadPool(1);

				msgDeleterPool.schedule(() ->
				{
					m.delete().block();
					msgDeleterPool.shutdown();
				}, 120, TimeUnit.SECONDS);
			}
			else
			{
				if (guildJson.getBoolean("enabled"))
				{
					Util.msg(channel, author, "Maunz is now disabled in this guild");
					guildJson.put("enabled", false);
					FileUtils.writeStringToFile(guildFile, guildJson.toString(2), "UTF-8");
				}
				else
				{
					Util.msg(channel, author, "You silly, I was already disabled in this guild!");
				}
			}
		}
		else
		{
			Util.msg(channel, author, "You do not have permission to use that command");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*disable" };
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event, Message message) throws Exception
	{
		if (menuMessages.containsKey(event.getUser().block().getId().asString()))
		{
			File botFile = new File(Util.getJarLocation() + "config.json");
			File guildFile = new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().block().getId().asString() + ".json");
			JSONObject botJson = new JSONObject(Util.getFileContents(botFile));
			JSONObject guildJson = new JSONObject(Util.getFileContents(guildFile));

			if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("1⃣"))
			{
				if (botJson.getBoolean("enabled"))
				{
					Util.msg(event.getChannel().block(), event.getUser().block(), "Maunz is now disabled globally");
					botJson.put("enabled", false);
					FileUtils.writeStringToFile(botFile, botJson.toString(2), "UTF-8");
				}
				else
				{
					Util.msg(event.getChannel().block(), event.getUser().block(), "You silly, I was already disabled globally!");
				}
			}
			else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("2⃣"))
			{
				if (guildJson.getBoolean("enabled"))
				{
					Util.msg(event.getChannel().block(), event.getUser().block(), "Maunz is now disabled in this guild");
					guildJson.put("enabled", false);
					FileUtils.writeStringToFile(guildFile, guildJson.toString(2), "UTF-8");
				}
				else
				{
					Util.msg(event.getChannel().block(), event.getUser().block(), "You silly, I was already disabled in this guild!");
				}
			}
		}
	}
}