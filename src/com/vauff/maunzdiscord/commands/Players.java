package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.features.ServerTimer;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.json.JSONException;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Players extends AbstractCommand<MessageReceivedEvent>
{
	private static HashMap<String, List<String>> selectionServers = new HashMap<>();
	private static HashMap<String, String> selectionMessages = new HashMap<>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (!event.getChannel().isPrivate())
		{
			String guildID = event.getGuild().getStringID();
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
						runCmd(event.getAuthor(), event.getChannel(), json.getJSONObject(serverList.get(0)));
					}
					else
					{
						String object = "";

						for (String objectName : serverList)
						{
							if (json.getJSONObject(objectName).getLong("serverTrackingChannelID") == event.getChannel().getLongID())
							{
								object = objectName;
								break;
							}
						}

						if (!object.equals(""))
						{
							runCmd(event.getAuthor(), event.getChannel(), json.getJSONObject(object));
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

							IMessage m = Util.msg(event.getChannel(), event.getAuthor(), msg);
							waitForReaction(m.getStringID(), event.getAuthor().getStringID());
							selectionServers.put(event.getAuthor().getStringID(), serverList);
							selectionMessages.put(event.getAuthor().getStringID(), m.getStringID());
							Util.addNumberedReactions(m, true, serverList.size());

							ScheduledExecutorService msgDeleterPool = Executors.newScheduledThreadPool(1);

							msgDeleterPool.schedule(() ->
							{
								if (!m.isDeleted())
								{
									m.delete();
									selectionServers.remove(event.getAuthor().getStringID());
									selectionMessages.remove(event.getAuthor().getStringID());
								}

								msgDeleterPool.shutdown();
							}, 120, TimeUnit.SECONDS);
						}
					}
				}
				else
				{
					Util.msg(event.getChannel(), event.getAuthor(), "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
				}
			}
			else
			{
				Util.msg(event.getChannel(), event.getAuthor(), "A server tracking service is not enabled in this guild yet! Please have a guild administrator run ***services** to set one up");
			}
		}
		else
		{
			Util.msg(event.getChannel(), event.getAuthor(), "This command can't be done in a PM, only in a guild with the server tracking service enabled");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*players" };
	}

	private void runCmd(IUser user, IChannel channel, JSONObject object)
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

				if (Util.msg((!sizeIsSmall ? user.getOrCreatePMChannel() : channel), user, playersList.toString()) == null)
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
	public void onReactionAdd(ReactionAddEvent event) throws Exception
	{
		if (selectionMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(selectionMessages.get(event.getUser().getStringID())))
			{
				int i = Util.emojiToInt(event.getReaction().getEmoji().toString()) - 1;

				if (i != -1)
				{
					if (selectionServers.get(event.getUser().getStringID()).contains("server" + i))
					{
						runCmd(event.getUser(), event.getChannel(), new JSONObject(Util.getFileContents("data/services/server-tracking/" + event.getGuild().getStringID() + "/serverInfo.json")).getJSONObject("server" + i));
					}
				}
			}
		}
	}
}