package com.vauff.maunzdiscord.commands;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import com.github.koraktor.steamcondenser.steam.servers.SourceServer;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IMessage;

public class Services extends AbstractCommand<MessageReceivedEvent>
{
	private static HashMap<String, String> states = new HashMap<String, String>();
	private static HashMap<String, String> menuMessages = new HashMap<String, String>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (Util.hasPermission(event.getAuthor(), event.getGuild()))
		{
			IMessage m = event.getChannel().sendMessage(":desktop:  |  **Services Menu:**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Add New Service" + System.lineSeparator() + "**`[2]`**  |  Edit Existing Service" + System.lineSeparator() + "**`[3]`**  |  Delete Existing Service");

			waitForReaction(m.getStringID(), event.getAuthor().getStringID());
			states.put(event.getAuthor().getStringID(), "main");
			menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
			Util.addReactions(m, true, 3);

			Executors.newScheduledThreadPool(1).schedule(() ->
			{
				if (!m.isDeleted())
				{
					m.delete();
					states.remove(event.getAuthor().getStringID());
					menuMessages.remove(event.getAuthor().getStringID());
				}
			}, 120, TimeUnit.SECONDS);
		}
		else
		{
			Util.msg(event.getChannel(), "You do not have permission to use that command");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*services" };
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event) throws Exception
	{
		if (menuMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(menuMessages.get(event.getUser().getStringID())))
			{
				if (event.getReaction().getEmoji().toString().equals("❌"))
				{
					states.remove(event.getUser().getStringID());
					menuMessages.remove(event.getUser().getStringID());
				}

				else if (states.get(event.getUser().getStringID()).equals("main"))
				{
					List<String> fileLocationList = new ArrayList<String>(Arrays.asList(Util.getJarLocation() + "services/map-tracking/", Util.getJarLocation() + "services/csgo-updates/"));
					List<String> services = new ArrayList<String>();
					boolean guildHasService = false;

					for (String fileLocation : fileLocationList)
					{
						File file = new File(fileLocation + event.getGuild().getStringID());
						File file2 = new File(fileLocation + event.getGuild().getStringID() + ".json");

						if (file.exists() || file2.exists())
						{
							guildHasService = true;
							services.add(fileLocation.split("services/")[1].replace("/", ""));
						}
					}

					if (event.getReaction().getEmoji().toString().equals("1⃣"))
					{
						if (services.size() != 2)
						{
							String message = ":heavy_plus_sign:  |  **Add New Service:**" + System.lineSeparator();
							String state = "add";
							int i = 0;

							if (!services.contains("map-tracking"))
							{
								i++;
								message += System.lineSeparator() + "**`[" + (i) + "]`**  |  Map Tracking";
								state += ",map-tracking." + (i);
							}

							if (!services.contains("csgo-updates"))
							{
								i++;
								message += System.lineSeparator() + "**`[" + (i) + "]`**  |  CS:GO Update Notifications";
								state += ",csgo-updates." + (i);
							}

							IMessage m = event.getChannel().sendMessage(message);

							waitForReaction(m.getStringID(), event.getUser().getStringID());
							states.put(event.getUser().getStringID(), state);
							menuMessages.put(event.getUser().getStringID(), m.getStringID());
							Util.addReactions(m, true, i);

							Executors.newScheduledThreadPool(1).schedule(() ->
							{
								if (!m.isDeleted())
								{
									m.delete();
									states.remove(event.getUser().getStringID());
									menuMessages.remove(event.getUser().getStringID());
								}
							}, 120, TimeUnit.SECONDS);
						}
						else
						{
							Util.msg(event.getChannel(), "There are no more services to add!");
						}
					}

					else if (event.getReaction().getEmoji().toString().equals("2⃣"))
					{
						if (guildHasService)
						{
							String message = ":pencil:  |  **Edit Existing Service:**" + System.lineSeparator();
							String state = "edit";

							for (int i = 0; i < services.size(); i++)
							{
								if (services.get(i).equals("map-tracking"))
								{
									message += System.lineSeparator() + "**`[" + (i + 1) + "]`**  |  Map Tracking";
									state += ",map-tracking." + (i + 1);
								}

								else if (services.get(i).equals("csgo-updates"))
								{
									message += System.lineSeparator() + "**`[" + (i + 1) + "]`**  |  CS:GO Update Notifications";
									state += ",csgo-updates." + (i + 1);
								}
							}

							IMessage m = event.getChannel().sendMessage(message);

							waitForReaction(m.getStringID(), event.getUser().getStringID());
							states.put(event.getUser().getStringID(), state);
							menuMessages.put(event.getUser().getStringID(), m.getStringID());
							Util.addReactions(m, true, services.size());

							Executors.newScheduledThreadPool(1).schedule(() ->
							{
								if (!m.isDeleted())
								{
									m.delete();
									states.remove(event.getUser().getStringID());
									menuMessages.remove(event.getUser().getStringID());
								}
							}, 120, TimeUnit.SECONDS);
						}
						else
						{
							Util.msg(event.getChannel(), "There are currently no services in this guild to edit!");
						}
					}

					else if (event.getReaction().getEmoji().toString().equals("3⃣"))
					{
						if (guildHasService)
						{
							String message = ":x:  |  **Delete Existing Service:**" + System.lineSeparator();
							String state = "delete";

							for (int i = 0; i < services.size(); i++)
							{
								if (services.get(i).equals("map-tracking"))
								{
									message += System.lineSeparator() + "**`[" + (i + 1) + "]`**  |  Map Tracking";
									state += ",map-tracking." + (i + 1);
								}

								if (services.get(i).equals("csgo-updates"))
								{
									message += System.lineSeparator() + "**`[" + (i + 1) + "]`**  |  CS:GO Update Notifications";
									state += ",csgo-updates." + (i + 1);
								}
							}

							IMessage m = event.getChannel().sendMessage(message);

							waitForReaction(m.getStringID(), event.getUser().getStringID());
							states.put(event.getUser().getStringID(), state);
							menuMessages.put(event.getUser().getStringID(), m.getStringID());
							Util.addReactions(m, true, services.size());

							Executors.newScheduledThreadPool(1).schedule(() ->
							{
								if (!m.isDeleted())
								{
									m.delete();
									states.remove(event.getUser().getStringID());
									menuMessages.remove(event.getUser().getStringID());
								}
							}, 120, TimeUnit.SECONDS);
						}
						else
						{
							Util.msg(event.getChannel(), "There are currently no services in this guild to delete!");
						}
					}
				}

				else if (states.get(event.getUser().getStringID()).startsWith("add,"))
				{
					String[] statesSplit = states.get(event.getUser().getStringID()).split(",");
					String service = "";

					for (int i = 1; i < statesSplit.length; i++)
					{
						if (event.getReaction().getEmoji().toString().equals("1⃣") && statesSplit[i].split("\\.")[1].equals("1"))
						{
							service = statesSplit[i].split("\\.")[0];
						}

						else if (event.getReaction().getEmoji().toString().equals("2⃣") && statesSplit[i].split("\\.")[1].equals("2"))
						{
							service = statesSplit[i].split("\\.")[0];
						}
					}

					if (service.equals("map-tracking"))
					{
						IMessage m = event.getChannel().sendMessage(":heavy_plus_sign:  |  **Add New Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "Please mention the channel you would like to send map tracking updates in");

						waitForReply(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "maptrackingadd.1");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());

						Executors.newScheduledThreadPool(1).schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								states.remove(event.getUser().getStringID());
								menuMessages.remove(event.getUser().getStringID());
							}
						}, 120, TimeUnit.SECONDS);
					}

					if (service.equals("csgo-updates"))
					{
						IMessage m = event.getChannel().sendMessage(":heavy_plus_sign:  |  **Add New Service: CS:GO Update Notifications**" + System.lineSeparator() + System.lineSeparator() + "Please mention the channel you would like to send update notifications in");

						waitForReply(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "csgoupdatesadd.1");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());

						Executors.newScheduledThreadPool(1).schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								states.remove(event.getUser().getStringID());
								menuMessages.remove(event.getUser().getStringID());
							}
						}, 120, TimeUnit.SECONDS);
					}
				}

				else if (states.get(event.getUser().getStringID()).startsWith("csgoupdatesadd.2"))
				{
					String[] statesSplit = states.get(event.getUser().getStringID()).split(",");
					File file = new File(Util.getJarLocation() + "services/csgo-updates/" + event.getGuild().getStringID() + ".json");
					JSONObject json = new JSONObject();
					boolean nonImportantUpdates = false;

					if (event.getReaction().getEmoji().toString().equals("1⃣"))
					{
						nonImportantUpdates = true;
					}

					file.createNewFile();
					json.put("enabled", true);
					json.put("updateNotificationChannelID", Long.parseLong(statesSplit[1]));
					json.put("nonImportantUpdates", nonImportantUpdates);
					FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					Util.msg(event.getChannel(), "Successfully added the CS:GO Update Notifications service!");
				}

				else if (states.get(event.getUser().getStringID()).startsWith("edit,"))
				{
					String service = "";

					if (event.getReaction().getEmoji().toString().equals("1⃣") && states.get(event.getUser().getStringID()).contains("1"))
					{
						if (states.get(event.getUser().getStringID()).contains("map-tracking.1"))
						{
							service = "map-tracking";
						}

						else if (states.get(event.getUser().getStringID()).contains("csgo-updates.1"))
						{
							service = "csgo-updates";
						}
					}

					else if (event.getReaction().getEmoji().toString().equals("2⃣") && states.get(event.getUser().getStringID()).contains("2"))
					{
						if (states.get(event.getUser().getStringID()).contains("map-tracking.2"))
						{
							service = "map-tracking";
						}

						else if (states.get(event.getUser().getStringID()).contains("csgo-updates.2"))
						{
							service = "csgo-updates";
						}
					}

					if (service.equals("map-tracking"))
					{
						JSONObject json = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "services/map-tracking/" + event.getGuild().getStringID() + "/serverInfo.json")));
						IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Enabled: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("enabled"))) + "**" + System.lineSeparator() + "**`[2]`**  |  Server IP: " + "**" + json.getString("serverIP") + ":" + json.getInt("serverPort") + "**" + System.lineSeparator() + "**`[3]`**  |  Map Tracking Channel: " + "<#" + json.getLong("mapTrackingChannelID") + ">");

						waitForReaction(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "maptrackingedit");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());
						Util.addReactions(m, true, 3);

						Executors.newScheduledThreadPool(1).schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								states.remove(event.getUser().getStringID());
								menuMessages.remove(event.getUser().getStringID());
							}
						}, 120, TimeUnit.SECONDS);
					}

					else if (service.equals("csgo-updates"))
					{
						JSONObject json = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "services/csgo-updates/" + event.getGuild().getStringID() + ".json")));
						IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: CS:GO Update Notifications**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Enabled: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("enabled"))) + "**" + System.lineSeparator() + "**`[2]`**  |  Non Important Updates: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("nonImportantUpdates"))) + "**" + System.lineSeparator() + "**`[3]`**  |  Update Notification Channel: " + "<#" + json.getLong("updateNotificationChannelID") + ">");

						waitForReaction(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "csgoupdatesedit");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());
						Util.addReactions(m, true, 3);

						Executors.newScheduledThreadPool(1).schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								states.remove(event.getUser().getStringID());
								menuMessages.remove(event.getUser().getStringID());
							}
						}, 120, TimeUnit.SECONDS);
					}
				}

				else if (states.get(event.getUser().getStringID()).equals("maptrackingedit"))
				{
					File file = new File(Util.getJarLocation() + "services/map-tracking/" + event.getGuild().getStringID() + "/serverInfo.json");
					JSONObject json = new JSONObject(Util.getFileContents(file));

					if (event.getReaction().getEmoji().toString().equals("1⃣"))
					{
						json.put("enabled", !json.getBoolean("enabled"));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");

						IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Enabled: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("enabled"))) + "**" + System.lineSeparator() + "**`[2]`**  |  Server IP: " + "**" + json.getString("serverIP") + ":" + json.getInt("serverPort") + "**" + System.lineSeparator() + "**`[3]`**  |  Map Tracking Channel: " + "<#" + json.getLong("mapTrackingChannelID") + ">");

						waitForReaction(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "maptrackingedit");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());
						Util.addReactions(m, true, 3);

						Executors.newScheduledThreadPool(1).schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								states.remove(event.getUser().getStringID());
								menuMessages.remove(event.getUser().getStringID());
							}
						}, 120, TimeUnit.SECONDS);
					}

					else if (event.getReaction().getEmoji().toString().equals("2⃣"))
					{
						IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "Please type the server's IP in the format of ip:port (e.g. 123.45.678.90:27015)");

						waitForReply(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "maptrackingedit.1");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());

						Executors.newScheduledThreadPool(1).schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								states.remove(event.getUser().getStringID());
								menuMessages.remove(event.getUser().getStringID());
							}
						}, 120, TimeUnit.SECONDS);
					}

					else if (event.getReaction().getEmoji().toString().equals("3⃣"))
					{
						IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "Please mention the channel you would like to send update notifications in");

						waitForReply(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "maptrackingedit.2");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());

						Executors.newScheduledThreadPool(1).schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								states.remove(event.getUser().getStringID());
								menuMessages.remove(event.getUser().getStringID());
							}
						}, 120, TimeUnit.SECONDS);
					}
				}

				else if (states.get(event.getUser().getStringID()).equals("csgoupdatesedit"))
				{
					File file = new File(Util.getJarLocation() + "services/csgo-updates/" + event.getGuild().getStringID() + ".json");
					JSONObject json = new JSONObject(Util.getFileContents(file));

					if (event.getReaction().getEmoji().toString().equals("1⃣"))
					{
						json.put("enabled", !json.getBoolean("enabled"));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");

						IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: CS:GO Update Notifications**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Enabled: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("enabled"))) + "**" + System.lineSeparator() + "**`[2]`**  |  Non Important Updates: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("nonImportantUpdates"))) + "**" + System.lineSeparator() + "**`[3]`**  |  Update Notification Channel: " + "<#" + json.getLong("updateNotificationChannelID") + ">");

						waitForReaction(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "csgoupdatesedit");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());
						Util.addReactions(m, true, 3);

						Executors.newScheduledThreadPool(1).schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								states.remove(event.getUser().getStringID());
								menuMessages.remove(event.getUser().getStringID());
							}
						}, 120, TimeUnit.SECONDS);
					}

					else if (event.getReaction().getEmoji().toString().equals("2⃣"))
					{
						json.put("nonImportantUpdates", !json.getBoolean("nonImportantUpdates"));
						FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");

						IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: CS:GO Update Notifications**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Enabled: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("enabled"))) + "**" + System.lineSeparator() + "**`[2]`**  |  Non Important Updates: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("nonImportantUpdates"))) + "**" + System.lineSeparator() + "**`[3]`**  |  Update Notification Channel: " + "<#" + json.getLong("updateNotificationChannelID") + ">");

						waitForReaction(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "csgoupdatesedit");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());
						Util.addReactions(m, true, 3);

						Executors.newScheduledThreadPool(1).schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								states.remove(event.getUser().getStringID());
								menuMessages.remove(event.getUser().getStringID());
							}
						}, 120, TimeUnit.SECONDS);
					}

					else if (event.getReaction().getEmoji().toString().equals("3⃣"))
					{
						IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: CS:GO Update Notifications**" + System.lineSeparator() + System.lineSeparator() + "Please mention the channel you would like to send update notifications in");

						waitForReply(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "csgoupdatesedit.1");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());

						Executors.newScheduledThreadPool(1).schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								states.remove(event.getUser().getStringID());
								menuMessages.remove(event.getUser().getStringID());
							}
						}, 120, TimeUnit.SECONDS);
					}
				}

				else if (states.get(event.getUser().getStringID()).startsWith("delete,"))
				{
					String service = "";

					if (event.getReaction().getEmoji().toString().equals("1⃣") && states.get(event.getUser().getStringID()).contains("1"))
					{
						if (states.get(event.getUser().getStringID()).contains("map-tracking.1"))
						{
							service = "map-tracking";
						}

						else if (states.get(event.getUser().getStringID()).contains("csgo-updates.1"))
						{
							service = "csgo-updates";
						}
					}

					else if (event.getReaction().getEmoji().toString().equals("2⃣") && states.get(event.getUser().getStringID()).contains("2"))
					{
						if (states.get(event.getUser().getStringID()).contains("map-tracking.2"))
						{
							service = "map-tracking";
						}

						else if (states.get(event.getUser().getStringID()).contains("csgo-updates.2"))
						{
							service = "csgo-updates";
						}
					}

					if (service.equals("map-tracking"))
					{
						IMessage m = event.getChannel().sendMessage(":x:  |  **Delete Existing Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "Are you sure you would like to delete this service?" + System.lineSeparator() + System.lineSeparator() + "**WARNING:** This will delete your service data **permanently**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Yes, delete the service permanently" + System.lineSeparator() + "**`[2]`**  |  No, keep the service");

						waitForReaction(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "maptrackingdelete");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());
						Util.addReactions(m, false, 2);

						Executors.newScheduledThreadPool(1).schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								states.remove(event.getUser().getStringID());
								menuMessages.remove(event.getUser().getStringID());
							}
						}, 120, TimeUnit.SECONDS);
					}

					else if (service.equals("csgo-updates"))
					{
						IMessage m = event.getChannel().sendMessage(":x:  |  **Delete Existing Service: CS:GO Update Notifications**" + System.lineSeparator() + System.lineSeparator() + "Are you sure you would like to delete this service?" + System.lineSeparator() + System.lineSeparator() + "**WARNING:** This will delete your service data **permanently**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Yes, delete the service permanently" + System.lineSeparator() + "**`[2]`**  |  No, keep the service");

						waitForReaction(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "csgoupdatesdelete");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());
						Util.addReactions(m, false, 2);

						Executors.newScheduledThreadPool(1).schedule(() ->
						{
							if (!m.isDeleted())
							{
								m.delete();
								states.remove(event.getUser().getStringID());
								menuMessages.remove(event.getUser().getStringID());
							}
						}, 120, TimeUnit.SECONDS);
					}
				}

				else if (states.get(event.getUser().getStringID()).equals("maptrackingdelete"))
				{
					if (event.getReaction().getEmoji().toString().equals("1⃣"))
					{
						File folder = new File(Util.getJarLocation() + "services/map-tracking/" + event.getGuild().getStringID() + "/");
						File file = new File(Util.getJarLocation() + "services/map-tracking/" + event.getGuild().getStringID() + "/serverInfo.json");

						file.delete();
						folder.delete();
						Util.msg(event.getChannel(), "Successfully deleted the map tracking service!");
					}

					else if (event.getReaction().getEmoji().toString().equals("2⃣"))
					{
						Util.msg(event.getChannel(), "No problem, I won't delete the map tracking service");
					}
				}

				else if (states.get(event.getUser().getStringID()).equals("csgoupdatesdelete"))
				{
					if (event.getReaction().getEmoji().toString().equals("1⃣"))
					{
						File file = new File(Util.getJarLocation() + "services/csgo-updates/" + event.getGuild().getStringID() + ".json");

						file.delete();
						Util.msg(event.getChannel(), "Successfully deleted the CS:GO updates service!");
					}

					else if (event.getReaction().getEmoji().toString().equals("2⃣"))
					{
						Util.msg(event.getChannel(), "No problem, I won't delete the CS:GO updates service");
					}
				}
			}
		}
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) throws Exception
	{
		if (menuMessages.containsKey(event.getAuthor().getStringID()))
		{
			if (states.get(event.getAuthor().getStringID()).equals("maptrackingadd.1"))
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
					IMessage m = event.getChannel().sendMessage(":heavy_plus_sign:  |  **Add New Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "Please type the server's IP in the format of ip:port (e.g. 123.45.678.90:27015)");

					waitForReply(m.getStringID(), event.getAuthor().getStringID());
					states.put(event.getAuthor().getStringID(), "maptrackingadd.2," + message);
					menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
					AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).dontRemove();

					Executors.newScheduledThreadPool(1).schedule(() ->
					{
						if (!m.isDeleted())
						{
							m.delete();
							states.remove(event.getAuthor().getStringID());
							menuMessages.remove(event.getAuthor().getStringID());
						}
					}, 120, TimeUnit.SECONDS);
				}
				else
				{
					IMessage m = event.getChannel().sendMessage(":heavy_plus_sign:  |  **Add New Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "The given channel either didn't exist or was in another guild" + System.lineSeparator() + System.lineSeparator() + "Please mention the channel you would like to send map tracking updates in");

					waitForReply(m.getStringID(), event.getAuthor().getStringID());
					menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
					AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).dontRemove();

					Executors.newScheduledThreadPool(1).schedule(() ->
					{
						if (!m.isDeleted())
						{
							m.delete();
							states.remove(event.getAuthor().getStringID());
							menuMessages.remove(event.getAuthor().getStringID());
						}

						AbstractCommand.AWAITED.remove(event.getAuthor().getStringID()); //removing the author as he hasn't been removed because of the line above calling AbstractCommand#dontRemove
					}, 120, TimeUnit.SECONDS);
				}
			}

			else if (states.get(event.getAuthor().getStringID()).startsWith("maptrackingadd.2"))
			{
				String[] statesSplit = states.get(event.getAuthor().getStringID()).split(",");
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
					File folder = new File(Util.getJarLocation() + "services/map-tracking/" + event.getGuild().getStringID() + "/");
					File file = new File(Util.getJarLocation() + "services/map-tracking/" + event.getGuild().getStringID() + "/serverInfo.json");
					JSONObject json = new JSONObject();

					folder.mkdir();
					file.createNewFile();
					json.put("mapDatabase", new JSONArray());
					json.put("mapTrackingChannelID", Long.parseLong(statesSplit[1]));
					json.put("downtimeTimer", 0);
					json.put("players", "0/0");
					json.put("lastGuildName", event.getGuild().getName());
					json.put("lastMap", "N/A");
					json.put("serverIP", ip);
					json.put("serverPort", port);
					json.put("enabled", true);
					json.put("timestamp", 1);
					FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
					Util.msg(event.getChannel(), "Successfully added the Map Tracking service!");
				}
				else
				{
					IMessage m = event.getChannel().sendMessage(":heavy_plus_sign:  |  **Add New Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "The bot was unable to make a connection to a source engine server running on that IP and port" + System.lineSeparator() + System.lineSeparator() + "Please type the server's IP in the format of ip:port (e.g. 123.45.678.90:27015)");

					waitForReply(m.getStringID(), event.getAuthor().getStringID());
					menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
					AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).dontRemove();

					Executors.newScheduledThreadPool(1).schedule(() ->
					{
						if (!m.isDeleted())
						{
							m.delete();
							states.remove(event.getAuthor().getStringID());
							menuMessages.remove(event.getAuthor().getStringID());
						}

						AbstractCommand.AWAITED.remove(event.getAuthor().getStringID()); //removing the author as he hasn't been removed because of the line above calling AbstractCommand#dontRemove
					}, 120, TimeUnit.SECONDS);
				}
			}

			else if (states.get(event.getAuthor().getStringID()).equals("csgoupdatesadd.1"))
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
					IMessage m = event.getChannel().sendMessage(":heavy_plus_sign:  |  **Add New Service: CS:GO Update Notifications**" + System.lineSeparator() + System.lineSeparator() + "Would you like to send notifications for non-important updates? (SteamDB updates that don't really mean anything)" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Yes" + System.lineSeparator() + "**`[2]`**  |  No");

					waitForReaction(m.getStringID(), event.getAuthor().getStringID());
					states.put(event.getAuthor().getStringID(), "csgoupdatesadd.2," + message);
					menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
					Util.addReactions(m, true, 2);

					Executors.newScheduledThreadPool(1).schedule(() ->
					{
						if (!m.isDeleted())
						{
							m.delete();
							states.remove(event.getAuthor().getStringID());
							menuMessages.remove(event.getAuthor().getStringID());
						}
					}, 120, TimeUnit.SECONDS);
				}
				else
				{
					IMessage m = event.getChannel().sendMessage(":heavy_plus_sign:  |  **Add New Service: CS:GO Update Notifications**" + System.lineSeparator() + System.lineSeparator() + "The given channel either didn't exist or was in another guild" + System.lineSeparator() + System.lineSeparator() + "Please mention the channel you would like to send update notifications in");

					waitForReply(m.getStringID(), event.getAuthor().getStringID());
					menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
					AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).dontRemove();

					Executors.newScheduledThreadPool(1).schedule(() ->
					{
						if (!m.isDeleted())
						{
							m.delete();
							states.remove(event.getAuthor().getStringID());
							menuMessages.remove(event.getAuthor().getStringID());
						}

						AbstractCommand.AWAITED.remove(event.getAuthor().getStringID()); //removing the author as he hasn't been removed because of the line above calling AbstractCommand#dontRemove
					}, 120, TimeUnit.SECONDS);
				}
			}

			else if (states.get(event.getAuthor().getStringID()).equals("csgoupdatesedit.1"))
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
					File file = new File(Util.getJarLocation() + "services/csgo-updates/" + event.getGuild().getStringID() + ".json");
					JSONObject json = new JSONObject(Util.getFileContents(file));

					json.put("updateNotificationChannelID", Long.parseLong(message));
					FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");

					IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: CS:GO Update Notifications**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Enabled: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("enabled"))) + "**" + System.lineSeparator() + "**`[2]`**  |  Non Important Updates: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("nonImportantUpdates"))) + "**" + System.lineSeparator() + "**`[3]`**  |  Update Notification Channel: " + "<#" + json.getLong("updateNotificationChannelID") + ">");

					waitForReaction(m.getStringID(), event.getAuthor().getStringID());
					states.put(event.getAuthor().getStringID(), "csgoupdatesedit");
					menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
					Util.addReactions(m, true, 3);

					Executors.newScheduledThreadPool(1).schedule(() ->
					{
						if (!m.isDeleted())
						{
							m.delete();
							states.remove(event.getAuthor().getStringID());
							menuMessages.remove(event.getAuthor().getStringID());
						}
					}, 120, TimeUnit.SECONDS);
				}
				else
				{
					IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: CS:GO Update Notifications**" + System.lineSeparator() + System.lineSeparator() + "The given channel either didn't exist or was in another guild" + System.lineSeparator() + System.lineSeparator() + "Please mention the channel you would like to send update notifications in");

					waitForReply(m.getStringID(), event.getAuthor().getStringID());
					menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
					AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).dontRemove();

					Executors.newScheduledThreadPool(1).schedule(() ->
					{
						if (!m.isDeleted())
						{
							m.delete();
							states.remove(event.getAuthor().getStringID());
							menuMessages.remove(event.getAuthor().getStringID());
						}

						AbstractCommand.AWAITED.remove(event.getAuthor().getStringID()); //removing the author as he hasn't been removed because of the line above calling AbstractCommand#dontRemove
					}, 120, TimeUnit.SECONDS);
				}
			}

			else if (states.get(event.getAuthor().getStringID()).equals("maptrackingedit.1"))
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
					File file = new File(Util.getJarLocation() + "services/map-tracking/" + event.getGuild().getStringID() + "/serverInfo.json");
					JSONObject json = new JSONObject(Util.getFileContents(file));

					json.put("serverIP", ip);
					json.put("serverPort", port);
					FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");

					IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Enabled: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("enabled"))) + "**" + System.lineSeparator() + "**`[2]`**  |  Server IP: " + "**" + json.getString("serverIP") + ":" + json.getInt("serverPort") + "**" + System.lineSeparator() + "**`[3]`**  |  Map Tracking Channel: " + "<#" + json.getLong("mapTrackingChannelID") + ">");

					waitForReaction(m.getStringID(), event.getAuthor().getStringID());
					states.put(event.getAuthor().getStringID(), "maptrackingedit");
					menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
					Util.addReactions(m, true, 3);

					Executors.newScheduledThreadPool(1).schedule(() ->
					{
						if (!m.isDeleted())
						{
							m.delete();
							states.remove(event.getAuthor().getStringID());
							menuMessages.remove(event.getAuthor().getStringID());
						}
					}, 120, TimeUnit.SECONDS);
				}
				else
				{
					IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "The bot was unable to make a connection to a source engine server running on that IP and port" + System.lineSeparator() + System.lineSeparator() + "Please type the server's IP in the format of ip:port (e.g. 123.45.678.90:27015)");

					waitForReply(m.getStringID(), event.getAuthor().getStringID());
					menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
					AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).dontRemove();

					Executors.newScheduledThreadPool(1).schedule(() ->
					{
						if (!m.isDeleted())
						{
							m.delete();
							states.remove(event.getAuthor().getStringID());
							menuMessages.remove(event.getAuthor().getStringID());
						}

						AbstractCommand.AWAITED.remove(event.getAuthor().getStringID()); //removing the author as he hasn't been removed because of the line above calling AbstractCommand#dontRemove
					}, 120, TimeUnit.SECONDS);
				}
			}

			else if (states.get(event.getAuthor().getStringID()).equals("maptrackingedit.2"))
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
					File file = new File(Util.getJarLocation() + "services/map-tracking/" + event.getGuild().getStringID() + "/serverInfo.json");
					JSONObject json = new JSONObject(Util.getFileContents(file));

					json.put("mapTrackingChannelID", Long.parseLong(message));
					FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");

					IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Enabled: " + "**" + StringUtils.capitalize(Boolean.toString(json.getBoolean("enabled"))) + "**" + System.lineSeparator() + "**`[2]`**  |  Server IP: " + "**" + json.getString("serverIP") + ":" + json.getInt("serverPort") + "**" + System.lineSeparator() + "**`[3]`**  |  Map Tracking Channel: " + "<#" + json.getLong("mapTrackingChannelID") + ">");

					waitForReaction(m.getStringID(), event.getAuthor().getStringID());
					states.put(event.getAuthor().getStringID(), "maptrackingedit");
					menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
					Util.addReactions(m, true, 3);

					Executors.newScheduledThreadPool(1).schedule(() ->
					{
						if (!m.isDeleted())
						{
							m.delete();
							states.remove(event.getAuthor().getStringID());
							menuMessages.remove(event.getAuthor().getStringID());
						}
					}, 120, TimeUnit.SECONDS);
				}
				else
				{
					IMessage m = event.getChannel().sendMessage(":pencil:  |  **Edit Existing Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "The given channel either didn't exist or was in another guild" + System.lineSeparator() + System.lineSeparator() + "Please mention the channel you would like to send map tracking updates in");

					waitForReply(m.getStringID(), event.getAuthor().getStringID());
					menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
					AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).dontRemove();

					Executors.newScheduledThreadPool(1).schedule(() ->
					{
						if (!m.isDeleted())
						{
							m.delete();
							states.remove(event.getAuthor().getStringID());
							menuMessages.remove(event.getAuthor().getStringID());
						}

						AbstractCommand.AWAITED.remove(event.getAuthor().getStringID()); //removing the author as he hasn't been removed because of the line above calling AbstractCommand#dontRemove
					}, 120, TimeUnit.SECONDS);
				}
			}
		}
	}
}