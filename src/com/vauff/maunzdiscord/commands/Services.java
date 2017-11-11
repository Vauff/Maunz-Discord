package com.vauff.maunzdiscord.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.vauff.maunzdiscord.core.AbstractCommand;
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
		if (Util.hasPermission(event.getMessage().getAuthor(), event.getGuild()))
		{
			IMessage m = event.getChannel().sendMessage(":desktop:  |  **Services Menu:**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Add New Service" + System.lineSeparator() + "**`[2]`**  |  Edit Existing Service");

			waitForReaction(m.getStringID(), event.getMessage().getAuthor().getStringID());
			states.put(event.getMessage().getAuthor().getStringID(), "main");
			menuMessages.put(event.getMessage().getAuthor().getStringID(), m.getStringID());
			Util.addNumberedReactions(m, 2);

			Executors.newScheduledThreadPool(1).schedule(() ->
			{
				if (!m.isDeleted())
				{
					m.delete();
					states.remove(event.getMessage().getAuthor().getStringID());
					menuMessages.remove(event.getMessage().getAuthor().getStringID());
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
				List<String> fileLocationList = new ArrayList<String>();

				fileLocationList.add(Util.getJarLocation() + "services/map-tracking/");
				fileLocationList.add(Util.getJarLocation() + "services/csgo-updates/");

				if (states.get(event.getUser().getStringID()).equals("main"))
				{
					if (event.getReaction().getEmoji().toString().equals("1⃣"))
					{
						IMessage m = event.getChannel().sendMessage(":heavy_plus_sign:  |  **Add New Service:**" + System.lineSeparator() + System.lineSeparator() + "**`[1]`**  |  Map Tracking" + System.lineSeparator() + "**`[2]`**  |  CS:GO Update Notifications");

						waitForReaction(m.getStringID(), event.getUser().getStringID());
						states.put(event.getUser().getStringID(), "add");
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

					if (event.getReaction().getEmoji().toString().equals("2⃣"))
					{
						boolean guildHasService = false;
						List<String> services = new ArrayList<String>();

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

				if (states.get(event.getUser().getStringID()).equals("add"))
				{
					if (event.getReaction().getEmoji().toString().equals("1⃣"))
					{
					}

					if (event.getReaction().getEmoji().toString().equals("2⃣"))
					{
					}
				}
			}
		}
	}
}