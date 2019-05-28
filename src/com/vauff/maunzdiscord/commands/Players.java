package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.features.ServerTimer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Players extends AbstractCommand<MessageCreateEvent>
{
	private static HashMap<Snowflake, List<String>> selectionServers = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> selectionMessages = new HashMap<>();

	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		if (!(channel instanceof PrivateChannel))
		{
			String guildID = event.getGuild().block().getId().asString();
			File file = new File(Util.getJarLocation() + "data/services/server-tracking/" + guildID + "/serverInfo.json");

			if (file.exists())
			{
				JSONObject json = new JSONObject(Util.getFileContents("data/services/server-tracking/" + guildID + "/serverInfo.json"));
				int serverNumber = 0;
				List<String> serverList = new ArrayList<>();

				while (true)
				{
					JSONObject object;

					try
					{
						object = json.getJSONObject("server" + serverNumber);
					}
					catch (JSONException e)
					{
						break;
					}

					if (object.getBoolean("enabled"))
					{
						serverList.add("server" + serverNumber);
					}

					serverNumber++;
				}

				if (serverList.size() != 0)
				{
					if (serverList.size() == 1)
					{
						runCmd(author, channel, json.getJSONObject(serverList.get(0)));
					}
					else
					{
						String object = "";

						for (String objectName : serverList)
						{
							if (json.getJSONObject(objectName).getLong("serverTrackingChannelID") == channel.getId().asLong())
							{
								object = objectName;
								break;
							}
						}

						if (!object.equals(""))
						{
							runCmd(author, channel, json.getJSONObject(object));
						}
						else
						{
							String msg = "Please select which server to retrieve player data for" + System.lineSeparator();
							int i = 1;

							for (String serverObject : serverList)
							{
								msg += System.lineSeparator() + "**`[" + i + "]`**  |  " + json.getJSONObject(serverObject).getString("serverName");
								i++;
							}

							Message m = Util.msg(channel, author, msg);

							waitForReaction(m.getId(), author.getId());
							selectionServers.put(author.getId(), serverList);
							selectionMessages.put(author.getId(), m.getId());
							Util.addNumberedReactions(m, true, serverList.size());

							ScheduledExecutorService msgDeleterPool = Executors.newScheduledThreadPool(1);

							msgDeleterPool.schedule(() ->
							{
								m.delete();
								selectionServers.remove(author.getId());
								selectionMessages.remove(author.getId());
								msgDeleterPool.shutdown();
							}, 120, TimeUnit.SECONDS);
						}
					}
				}
				else
				{
					Util.msg(channel, author, "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
				}
			}
			else
			{
				Util.msg(channel, author, "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
			}
		}
		else
		{
			Util.msg(channel, author, "This command can't be done in a PM, only in a guild with the server tracking service enabled");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*players" };
	}

	private void runCmd(User user, MessageChannel channel, JSONObject object)
	{
		StringBuilder playersList = new StringBuilder();

		if (!(object.getInt("downtimeTimer") >= object.getInt("failedConnectionsThreshold")))
		{
			if (ServerTimer.serverPlayers.containsKey(object.getString("serverIP") + ":" + object.getInt("serverPort")))
			{
				boolean sizeIsSmall = ServerTimer.serverPlayers.get(object.getString("serverIP") + ":" + object.getInt("serverPort")).size() <= 8;

				playersList.append("```-- Players Online: " + object.getString("players") + " --" + System.lineSeparator() + System.lineSeparator());

				for (String player : ServerTimer.serverPlayers.get(object.getString("serverIP") + ":" + object.getInt("serverPort")))
				{
					if (!player.equals(""))
					{
						playersList.append("- " + player + System.lineSeparator());
					}
				}

				playersList.append("```");

				if (Util.msg((!sizeIsSmall ? user.getPrivateChannel().block() : channel), user, playersList.toString()) == null)
				{
					if (!sizeIsSmall)
					{
						Util.msg(channel, user, "An error occured when trying to PM you the players list, make sure you don't have private messages disabled in any capacity or the bot blocked");
					}
				}
				else
				{
					if (!sizeIsSmall)
					{
						Util.msg(channel, user, "Sending the online player list to you in a PM!");
					}
				}
			}
			else
			{
				Util.msg(channel, user, "There doesn't appear to be any player info cached yet (was the bot just started or the service just added?), please wait a moment before trying again");
			}
		}
		else
		{
			Util.msg(channel, user, "The server currently appears to be offline");
		}
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event, Message message) throws Exception
	{
		if (selectionMessages.containsKey(event.getUser().block().getId()) && message.getId().equals(selectionMessages.get(event.getUser().block().getId())))
		{
			int i = Util.emojiToInt(event.getEmoji().asUnicodeEmoji().get().getRaw()) - 1;

			if (i != -1)
			{
				if (selectionServers.get(event.getUser().block().getId()).contains("server" + i))
				{
					runCmd(event.getUser().block(), event.getChannel().block(), new JSONObject(Util.getFileContents("data/services/server-tracking/" + event.getGuild().block().getId().asString() + "/serverInfo.json")).getJSONObject("server" + i));
				}
			}
		}
	}
}