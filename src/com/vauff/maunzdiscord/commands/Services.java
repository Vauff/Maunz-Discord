package com.vauff.maunzdiscord.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

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
			IMessage m = event.getChannel().sendMessage(":desktop:  |  **Services Menu:**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Add New Service" + System.lineSeparator() + "**`[2]`**  |  Edit Existing Service");

			waitForReaction(m.getStringID(), event.getAuthor().getStringID());
			states.put(event.getAuthor().getStringID(), "main");
			menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
			Util.addNumberedReactions(m, 2);

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

				if (states.get(event.getUser().getStringID()).equals("main"))
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
								message = message + System.lineSeparator() + "**`[" + (i) + "]`**  |  Map Tracking";
								state = state + ",map-tracking." + (i);
							}

							if (!services.contains("csgo-updates"))
							{
								i++;
								message = message + System.lineSeparator() + "**`[" + (i) + "]`**  |  CS:GO Update Notifications";
								state = state + ",csgo-updates." + (i);
							}

							IMessage m = event.getChannel().sendMessage(message);

							waitForReaction(m.getStringID(), event.getUser().getStringID());
							states.put(event.getUser().getStringID(), state);
							menuMessages.put(event.getUser().getStringID(), m.getStringID());
							Util.addNumberedReactions(m, 2);

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

					if (event.getReaction().getEmoji().toString().equals("2⃣"))
					{
						if (guildHasService)
						{
							String message = ":pencil:  |  **Edit Existing Service:**" + System.lineSeparator();
							String state = "edit";

							for (int i = 0; i < services.size(); i++)
							{
								if (services.get(i).equals("map-tracking"))
								{
									message = message + System.lineSeparator() + "**`[" + (i + 1) + "]`**  |  Map Tracking";
									state = state + ",map-tracking." + (i + 1);
								}

								if (services.get(i).equals("csgo-updates"))
								{
									message = message + System.lineSeparator() + "**`[" + (i + 1) + "]`**  |  CS:GO Update Notifications";
									state = state + ",csgo-updates." + (i + 1);
								}
							}

							IMessage m = event.getChannel().sendMessage(message);

							waitForReaction(m.getStringID(), event.getUser().getStringID());
							states.put(event.getUser().getStringID(), state);
							menuMessages.put(event.getUser().getStringID(), m.getStringID());
							Util.addNumberedReactions(m, services.size());

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
						IMessage m = event.getChannel().sendMessage(":heavy_plus_sign:  |  **Add New Service: Map Tracking**" + System.lineSeparator() + System.lineSeparator() + "Please type the servers IP in the format of ip:port (e.g. 123.45.678.90:27015)");

						waitForReply(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "maptrackingadd.1");
						menuMessages.put(event.getUser().getStringID(), m.getStringID());
						Util.addNumberedReactions(m, 1);

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
					states.put(event.getAuthor().getStringID(), "csgoupdatesadd.1," + message);
					menuMessages.put(event.getAuthor().getStringID(), m.getStringID());
					Util.addNumberedReactions(m, 2);

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
					states.put(event.getAuthor().getStringID(), "csgoupdatesadd.1");
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
						
						AbstractCommand.AWAITED.remove(event.getAuthor().getStringID()); //removing the author as he hasn't been removed because of line 287
					}, 120, TimeUnit.SECONDS);
				}
			}
		}
	}
}