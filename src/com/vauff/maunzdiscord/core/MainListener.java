package com.vauff.maunzdiscord.core;

import com.vauff.maunzdiscord.commands.About;
import com.vauff.maunzdiscord.commands.Benchmark;
import com.vauff.maunzdiscord.commands.Blacklist;
import com.vauff.maunzdiscord.commands.Changelog;
import com.vauff.maunzdiscord.commands.Disable;
import com.vauff.maunzdiscord.commands.Discord;
import com.vauff.maunzdiscord.commands.Enable;
import com.vauff.maunzdiscord.commands.Help;
import com.vauff.maunzdiscord.commands.IsItDown;
import com.vauff.maunzdiscord.commands.Map;
import com.vauff.maunzdiscord.commands.Minecraft;
import com.vauff.maunzdiscord.commands.Notify;
import com.vauff.maunzdiscord.commands.Ping;
import com.vauff.maunzdiscord.commands.Players;
import com.vauff.maunzdiscord.commands.Quote;
import com.vauff.maunzdiscord.commands.Reddit;
import com.vauff.maunzdiscord.commands.Restart;
import com.vauff.maunzdiscord.commands.Say;
import com.vauff.maunzdiscord.commands.Source;
import com.vauff.maunzdiscord.commands.Steam;
import com.vauff.maunzdiscord.commands.Stop;
import com.vauff.maunzdiscord.commands.servicesmenu.Services;
import com.vauff.maunzdiscord.features.Intelligence;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;

import java.io.File;
import java.util.LinkedList;

public class MainListener
{
	/**
	 * Holds all commands
	 */
	public static LinkedList<AbstractCommand<MessageReceivedEvent>> commands = new LinkedList<AbstractCommand<MessageReceivedEvent>>();

	/**
	 * Sets up all commands
	 */
	public MainListener()
	{
		try
		{
			JSONObject json = new JSONObject(Util.getFileContents("config.json"));

			commands.add(new About());
			commands.add(new Benchmark());
			commands.add(new Blacklist());
			commands.add(new Changelog());
			commands.add(new Disable());
			commands.add(new Discord());
			commands.add(new Enable());
			commands.add(new Help());
			commands.add(new IsItDown());
			commands.add(new Map());
			commands.add(new Minecraft());
			commands.add(new Notify());
			commands.add(new Ping());
			commands.add(new Players());
			commands.add(new Reddit());
			commands.add(new Restart());
			commands.add(new Say());
			commands.add(new Services());
			commands.add(new Source());
			commands.add(new Steam());
			commands.add(new Stop());

			if (json.getJSONObject("database").getString("hostname").equals("") || json.getJSONObject("database").getString("username").equals("") || json.getJSONObject("database").getString("password").equals(""))
			{
				Main.log.warn("The quote command is disabled due to 1 or more values in the database section of config.json not being supplied");
			}
			else
			{
				commands.add(new Quote());
			}

			if (json.getString("cleverbotAPIKey").equals(""))
			{
				Main.log.warn("Maunz intelligence is disabled due to cleverbotAPIKey not being supplied in the config.json");
			}
			else
			{
				commands.add(new Intelligence());
			}
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event)
	{
		try
		{
			String cmdName = event.getMessage().getContent().split(" ")[0];

			for (AbstractCommand<MessageReceivedEvent> cmd : commands)
			{
				if (Util.isEnabled(event.getGuild()) || cmd instanceof Enable || cmd instanceof Disable)
				{
					for (String s : cmd.getAliases())
					{
						if (cmdName.equalsIgnoreCase(s))
						{
							JSONObject json = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().getStringID() + ".json")));
							boolean blacklisted = false;

							if (!Util.hasPermission(event.getAuthor(), event.getGuild()))
							{
								for (int i = 0; i < json.getJSONArray("blacklist").length(); i++)
								{
									String entry = json.getJSONArray("blacklist").getString(i);

									if ((entry.split(":")[0].equalsIgnoreCase(event.getChannel().getStringID()) || entry.split(":")[0].equalsIgnoreCase("all")) && (entry.split(":")[1].equalsIgnoreCase(cmdName.replace("*", "")) || entry.split(":")[1].equalsIgnoreCase("all")))
									{
										blacklisted = true;
										break;
									}
								}
							}

							if (!blacklisted)
							{
								event.getChannel().setTypingStatus(true);
								Thread.sleep(250);

								try
								{
									cmd.exe(event);
								}
								catch (Exception e)
								{
									Main.log.error("", e);
								}

								event.getChannel().setTypingStatus(false);
							}
							else
							{
								Util.msg(event.getAuthor().getOrCreatePMChannel(), ":exclamation:  |  **Command/channel blacklisted**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command in " + event.getChannel().mention() + " because a guild administrator has blacklisted either the command or the channel that you ran it in");
							}
						}
					}

				}
			}

			try
			{
				if (AbstractCommand.AWAITED.containsKey(event.getAuthor().getStringID()) && event.getChannel().equals(Main.client.getMessageByID(Long.parseLong(AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).getID())).getChannel()))
				{
					Main.client.getMessageByID(Long.parseLong(AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).getID())).delete();
					AbstractCommand.AWAITED.get(event.getAuthor().getStringID()).getCommand().onMessageReceived(event);
				}
			}
			catch (NullPointerException e)
			{
				// This means that the message ID in AbstractCommand#AWAITED for the given user ID has already been deleted, we can safely just stop executing
			}
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}

	@EventSubscriber
	public void onReactionAdd(ReactionAddEvent event)
	{
		try
		{
			if (AbstractCommand.AWAITED.containsKey(event.getMessage().getStringID()) && event.getUser().getStringID().equals(AbstractCommand.AWAITED.get(event.getMessage().getStringID()).getID()))
			{
				event.getMessage().delete();
				AbstractCommand.AWAITED.get(event.getMessage().getStringID()).getCommand().onReactionAdd(event);
			}
			else if (AbstractMenuPage.ACTIVE.containsKey(event.getUser().getLongID()))
			{
				try
				{
					if (event.getMessageID() == AbstractMenuPage.ACTIVE.get(event.getUser().getLongID()).menu.getLongID())
					{
						ReactionEmoji e = event.getReaction().getEmoji();
						int index;

						switch (e.toString())
						{
							case "❌":
								index = -1;
								break;
							case "1⃣":
								index = 0;
								break;
							case "2⃣":
								index = 1;
								break;
							case "3⃣":
								index = 2;
								break;
							case "4⃣":
								index = 3;
								break;
							case "5⃣":
								index = 4;
								break;
							case "6⃣":
								index = 5;
								break;
							case "7⃣":
								index = 6;
								break;
							case "8⃣":
								index = 7;
								break;
							case "9⃣":
								index = 8;
								break;
							default:
								Main.log.warn("Emoji added that is not part of the menu. Awaiting new input.");
								return;
						}

						AbstractMenuPage.ACTIVE.get(event.getUser().getLongID()).onReacted(event, index);
					}
				}
				catch (Exception e)
				{
					Main.log.error("", e);
				}
			}
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}

	@EventSubscriber
	public void onGuildCreate(GuildCreateEvent event)
	{
		try
		{
			File file = new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().getStringID() + ".json");

			if (!file.exists())
			{
				JSONObject json = new JSONObject();

				file.createNewFile();
				json.put("enabled", true);
				json.put("lastGuildName", event.getGuild().getName());
				json.put("blacklist", new JSONArray());
				FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
			}
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}
}
