package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.MainListener;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Blacklist extends AbstractCommand<MessageCreateEvent>
{
	private static HashMap<String, Integer> listPages = new HashMap<>();
	private static HashMap<String, String> listMessages = new HashMap<>();

	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().get().split(" ");

		if (!(channel instanceof PrivateChannel))
		{
			if (Util.hasPermission(author, event.getGuild().block()))
			{
				if (args.length != 1)
				{
					File file = new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().block().getId().asString() + ".json");
					JSONObject json = new JSONObject(Util.getFileContents(file));

					if (args[1].equalsIgnoreCase("list"))
					{
						if (json.getJSONArray("blacklist").length() != 0)
						{
							if (args.length == 2 || NumberUtils.isCreatable(args[2]))
							{
								int page;

								if (args.length == 2)
								{
									page = 1;
								}
								else
								{
									page = Integer.parseInt(args[2]);
								}

								ArrayList<String> blacklistArray = new ArrayList<>();

								for (int i = 0; i < json.getJSONArray("blacklist").length(); i++)
								{
									String entry = json.getJSONArray("blacklist").getString(i);
									String channelSelector;
									String command = "\\*" + entry.split(":")[1];

									if (entry.split(":")[0].equalsIgnoreCase("all"))
									{
										channelSelector = "All Channels";
									}
									else
									{
										channelSelector = Main.client.getChannelById(Snowflake.of(entry.split(":")[0])).block().getMention();
									}

									if (entry.split(":")[1].equalsIgnoreCase("all"))
									{
										command = "All Commands";
									}

									blacklistArray.add(channelSelector + " **|** " + command);
								}

								Message m = Util.buildPage(blacklistArray, "Blacklisted Channels/Commands", 10, page, false, false, channel, author);

								listMessages.put(author.getId().asString(), m.getId().asString());
								waitForReaction(m.getId().asString(), author.getId().asString());
								listPages.put(author.getId().asString(), page);
							}
							else
							{
								Util.msg(channel, author, "Page numbers need to be numerical!");
							}
						}
						else
						{
							Util.msg(channel, author, "This guild doesn't have any commands/channels blacklisted, use **\\*blacklist [all/channel] \\<all/command>** to add one");
						}
					}

					else if (args.length == 2)
					{
						String input = args[1].replace("*", "");
						boolean commandExists = false;

						if (input.equalsIgnoreCase("all"))
						{
							commandExists = true;
						}
						else
						{
							for (AbstractCommand<MessageCreateEvent> cmd : MainListener.commands)
							{
								for (String s : cmd.getAliases())
								{
									if (s.equalsIgnoreCase("*" + input))
									{
										commandExists = true;
										break;
									}
								}
							}
						}

						if (commandExists)
						{
							boolean blacklisted = false;

							for (int i = 0; i < json.getJSONArray("blacklist").length(); i++)
							{
								String entry = json.getJSONArray("blacklist").getString(i);

								if (entry.equals(channel.getId().asString() + ":" + input))
								{
									blacklisted = true;

									if (input.equalsIgnoreCase("all"))
									{
										Util.msg(channel, author, "Removing all commands from this guilds blacklist for <#" + channel.getId().asString() + ">!");
									}
									else
									{
										Util.msg(channel, author, "Removing the ***" + input + "** command from this guilds blacklist for <#" + channel.getId().asString() + ">!");
									}

									json.getJSONArray("blacklist").remove(i);
									FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
									break;
								}
							}

							if (!blacklisted)
							{
								if (input.equalsIgnoreCase("all"))
								{
									Util.msg(channel, author, "Adding all commands to this guilds blacklist for <#" + channel.getId().asString() + ">!");
								}
								else
								{
									Util.msg(channel, author, "Adding the ***" + input + "** command to this guilds blacklist for <#" + channel.getId().asString() + ">!");
								}

								json.getJSONArray("blacklist").put(channel.getId().asString() + ":" + input);
								FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
							}
						}
						else
						{
							Util.msg(channel, author, "The command **" + args[1] + "** doesn't exist!");
						}
					}
					else
					{
						String location = "";

						if (args[1].startsWith("<#"))
						{
							if (args[1].startsWith("<#"))
							{
								Channel refChannel = Main.client.getChannelById(Snowflake.of(args[1].replaceAll("[^\\d.]", ""))).block();

								if (((GuildChannel) refChannel).getGuild().block().equals(event.getGuild().block()))
								{
									location = args[1].replaceAll("[^\\d]", "");
								}
								else
								{
									Util.msg(channel, author, "That channel is in another guild!"); //test this works
								}
							}
						}
						else if (args[1].equalsIgnoreCase("all"))
						{
							location = "all";
						}

						if (!location.equalsIgnoreCase(""))
						{
							String input = args[2].replace("*", "");
							boolean commandExists = false;

							if (input.equalsIgnoreCase("all"))
							{
								commandExists = true;
							}
							else
							{
								for (AbstractCommand<MessageCreateEvent> cmd : MainListener.commands)
								{
									for (String s : cmd.getAliases())
									{
										if (s.equalsIgnoreCase("*" + input))
										{
											commandExists = true;
											break;
										}
									}
								}
							}

							if (commandExists)
							{
								boolean blacklisted = false;

								for (int i = 0; i < json.getJSONArray("blacklist").length(); i++)
								{
									String entry = json.getJSONArray("blacklist").getString(i);

									if (entry.equals(location + ":" + input))
									{
										blacklisted = true;

										if (input.equalsIgnoreCase("all"))
										{
											if (location.equalsIgnoreCase("all"))
											{
												Util.msg(channel, author, "Removing all commands from this guilds blacklist for all channels!");
											}
											else
											{
												Util.msg(channel, author, "Removing all commands from this guilds blacklist for <#" + location + ">!");
											}
										}
										else
										{
											if (location.equalsIgnoreCase("all"))
											{
												Util.msg(channel, author, "Removing the ***" + input + "** command from this guilds blacklist for all channels!");
											}
											else
											{
												Util.msg(channel, author, "Removing the ***" + input + "** command from this guilds blacklist for <#" + location + ">!");
											}
										}

										json.getJSONArray("blacklist").remove(i);
										FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
										break;
									}
								}

								if (!blacklisted)
								{
									if (input.equalsIgnoreCase("all"))
									{
										if (location.equalsIgnoreCase("all"))
										{
											Util.msg(channel, author, "Adding all commands to this guilds blacklist for all channels!");
										}
										else
										{
											Util.msg(channel, author, "Adding all commands to this guilds blacklist for <#" + location + ">!");
										}
									}
									else
									{
										if (location.equalsIgnoreCase("all"))
										{
											Util.msg(channel, author, "Adding the ***" + input + "** command to this guilds blacklist for all channels!");
										}
										else
										{
											Util.msg(channel, author, "Adding the ***" + input + "** command to this guilds blacklist for <#" + location + ">!");
										}
									}

									json.getJSONArray("blacklist").put(location + ":" + input);
									FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
								}
							}
							else
							{
								Util.msg(channel, author, "The command **" + args[2] + "** doesn't exist!");
							}
						}
						else
						{
							Util.msg(channel, author, "The channel **" + args[1] + "** doesn't exist!");
						}
					}
				}
				else
				{
					Util.msg(channel, author, "You need to specify arguments! See **\\*help blacklist**");
				}
			}
			else
			{
				Util.msg(channel, author, "You do not have permission to use that command");
			}
		}
		else
		{
			Util.msg(channel, author, "This command can't be done in a PM, only in a guild in which you have the administrator permission in");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*blacklist" };
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event, Message message) throws Exception
	{
		if (listMessages.containsKey(event.getUser().block().getId().asString()) && message.getId().asString().equals(listMessages.get(event.getUser().block().getId().asString())))
		{
			File file = new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().block().getId().asString() + ".json");
			JSONObject json = new JSONObject(Util.getFileContents(file));

			if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("▶"))
			{
				ArrayList<String> blacklistArray = new ArrayList<>();

				for (int i = 0; i < json.getJSONArray("blacklist").length(); i++)
				{
					String entry = json.getJSONArray("blacklist").getString(i);
					String channel;
					String command = "\\*" + entry.split(":")[1];

					if (entry.split(":")[0].equalsIgnoreCase("all"))
					{
						channel = "All Channels";
					}
					else
					{
						channel = Main.client.getChannelById(Snowflake.of(entry.split(":")[0])).block().getMention();
					}

					if (entry.split(":")[1].equalsIgnoreCase("all"))
					{
						command = "All Commands";
					}

					blacklistArray.add(channel + " **|** " + command);
				}

				Message m = Util.buildPage(blacklistArray, "Blacklisted Channels/Commands", 10, listPages.get(event.getUser().block().getId().asString()) + 1, false, false, event.getChannel().block(), event.getUser().block());

				listMessages.put(event.getUser().block().getId().asString(), m.getId().asString());
				waitForReaction(m.getId().asString(), event.getUser().block().getId().asString());
				listPages.put(event.getUser().block().getId().asString(), listPages.get(event.getUser().block().getId().asString()) + 1);
			}

			else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("◀"))
			{
				ArrayList<String> blacklistArray = new ArrayList<>();

				for (int i = 0; i < json.getJSONArray("blacklist").length(); i++)
				{
					String entry = json.getJSONArray("blacklist").getString(i);
					String channel;
					String command = "\\*" + entry.split(":")[1];

					if (entry.split(":")[0].equalsIgnoreCase("all"))
					{
						channel = "All Channels";
					}
					else
					{
						channel = Main.client.getChannelById(Snowflake.of(entry.split(":")[0])).block().getMention();
					}

					if (entry.split(":")[1].equalsIgnoreCase("all"))
					{
						command = "All Commands";
					}

					blacklistArray.add(channel + " **|** " + command);
				}

				Message m = Util.buildPage(blacklistArray, "Blacklisted Channels/Commands", 10, listPages.get(event.getUser().block().getId().asString()) - 1, false, false, event.getChannel().block(), event.getUser().block());

				listMessages.put(event.getUser().block().getId().asString(), m.getId().asString());
				waitForReaction(m.getId().asString(), event.getUser().block().getId().asString());
				listPages.put(event.getUser().block().getId().asString(), listPages.get(event.getUser().block().getId().asString()) - 1);
			}
		}
	}
}