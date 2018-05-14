package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.MainListener;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Blacklist extends AbstractCommand<MessageReceivedEvent>
{
	private static HashMap<String, Integer> listPages = new HashMap<>();
	private static HashMap<String, String> listMessages = new HashMap<>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (!event.getChannel().isPrivate())
		{
			if (Util.hasPermission(event.getAuthor(), event.getGuild()))
			{
				if (args.length != 1)
				{
					File file = new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().getStringID() + ".json");
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
									String channel;
									String command = "\\*" + entry.split(":")[1];

									if (entry.split(":")[0].equalsIgnoreCase("all"))
									{
										channel = "All Channels";
									}
									else
									{
										channel = Main.client.getChannelByID(Long.parseLong(entry.split(":")[0])).mention();
									}

									if (entry.split(":")[1].equalsIgnoreCase("all"))
									{
										command = "All Commands";
									}

									blacklistArray.add(channel + " **|** " + command);
								}

								IMessage m = Util.buildPage(blacklistArray, "Blacklisted Channels/Commands", 10, page, false, false, event.getChannel(), event.getAuthor());

								listMessages.put(event.getAuthor().getStringID(), m.getStringID());
								waitForReaction(m.getStringID(), event.getAuthor().getStringID());
								listPages.put(event.getAuthor().getStringID(), page);
							}
							else
							{
								Util.msg(event.getChannel(), event.getAuthor(), "Page numbers need to be numerical!");
							}
						}
						else
						{
							Util.msg(event.getChannel(), event.getAuthor(), "This guild doesn't have any commands/channels blacklisted, use **\\*blacklist [all/channel]/<list> <all/command>/[page]** to create or remove one");
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
							for (AbstractCommand<MessageReceivedEvent> cmd : MainListener.commands)
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

								if (entry.equals(event.getChannel().getStringID() + ":" + input))
								{
									blacklisted = true;

									if (input.equalsIgnoreCase("all"))
									{
										Util.msg(event.getChannel(), event.getAuthor(), "Removing all commands from this guilds blacklist for <#" + event.getChannel().getStringID() + ">!");
									}
									else
									{
										Util.msg(event.getChannel(), event.getAuthor(), "Removing the ***" + input + "** command from this guilds blacklist for <#" + event.getChannel().getStringID() + ">!");
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
									Util.msg(event.getChannel(), event.getAuthor(), "Adding all commands to this guilds blacklist for <#" + event.getChannel().getStringID() + ">!");
								}
								else
								{
									Util.msg(event.getChannel(), event.getAuthor(), "Adding the ***" + input + "** command to this guilds blacklist for <#" + event.getChannel().getStringID() + ">!");
								}

								json.getJSONArray("blacklist").put(event.getChannel().getStringID() + ":" + input);
								FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
							}
						}
						else
						{
							Util.msg(event.getChannel(), event.getAuthor(), "The command **" + args[1] + "** doesn't exist!");
						}
					}
					else
					{
						String location = "";

						if (args[1].startsWith("<#"))
						{
							if (args[1].startsWith("<#"))
							{
								IChannel channel = Main.client.getChannelByID(Long.parseLong(args[1].replaceAll("[^\\d.]", "")));

								if (channel.getGuild().equals(event.getGuild()))
								{
									location = args[1].replaceAll("[^\\d]", "");
									;
								}
								else
								{
									Util.msg(event.getChannel(), event.getAuthor(), "That channel is in another guild!");
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
								for (AbstractCommand<MessageReceivedEvent> cmd : MainListener.commands)
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
												Util.msg(event.getChannel(), event.getAuthor(), "Removing all commands from this guilds blacklist for all channels!");
											}
											else
											{
												Util.msg(event.getChannel(), event.getAuthor(), "Removing all commands from this guilds blacklist for <#" + location + ">!");
											}
										}
										else
										{
											if (location.equalsIgnoreCase("all"))
											{
												Util.msg(event.getChannel(), event.getAuthor(), "Removing the ***" + input + "** command from this guilds blacklist for all channels!");
											}
											else
											{
												Util.msg(event.getChannel(), event.getAuthor(), "Removing the ***" + input + "** command from this guilds blacklist for <#" + location + ">!");
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
											Util.msg(event.getChannel(), event.getAuthor(), "Adding all commands to this guilds blacklist for all channels!");
										}
										else
										{
											Util.msg(event.getChannel(), event.getAuthor(), "Adding all commands to this guilds blacklist for <#" + location + ">!");
										}
									}
									else
									{
										if (location.equalsIgnoreCase("all"))
										{
											Util.msg(event.getChannel(), event.getAuthor(), "Adding the ***" + input + "** command to this guilds blacklist for all channels!");
										}
										else
										{
											Util.msg(event.getChannel(), event.getAuthor(), "Adding the ***" + input + "** command to this guilds blacklist for <#" + location + ">!");
										}
									}

									json.getJSONArray("blacklist").put(location + ":" + input);
									FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
								}
							}
							else
							{
								Util.msg(event.getChannel(), event.getAuthor(), "The command **" + args[2] + "** doesn't exist!");
							}
						}
						else
						{
							Util.msg(event.getChannel(), event.getAuthor(), "The channel **" + args[1] + "** doesn't exist!");
						}
					}
				}
				else
				{
					Util.msg(event.getChannel(), event.getAuthor(), "You need to specify a command to blacklist (or \"all\")! **Usage: *blacklist [all/channel]/<list> <all/command>/[page]**");
				}
			}
			else
			{
				Util.msg(event.getChannel(), event.getAuthor(), "You do not have permission to use that command");
			}
		}
		else
		{
			Util.msg(event.getChannel(), event.getAuthor(), "This command can't be done in a PM, only in a guild in which you have the administrator permission in");
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*blacklist" };
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event) throws Exception
	{
		if (listMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(listMessages.get(event.getUser().getStringID())))
			{
				File file = new File(Util.getJarLocation() + "data/guilds/" + event.getGuild().getStringID() + ".json");
				JSONObject json = new JSONObject(Util.getFileContents(file));

				if (event.getReaction().getEmoji().toString().equals("▶"))
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
							channel = Main.client.getChannelByID(Long.parseLong(entry.split(":")[0])).mention();
						}

						if (entry.split(":")[1].equalsIgnoreCase("all"))
						{
							command = "All Commands";
						}

						blacklistArray.add(channel + " **|** " + command);
					}

					IMessage m = Util.buildPage(blacklistArray, "Blacklisted Channels/Commands", 10, listPages.get(event.getUser().getStringID()) + 1, false, false, event.getChannel(), event.getUser());

					listMessages.put(event.getUser().getStringID(), m.getStringID());
					waitForReaction(m.getStringID(), event.getUser().getStringID());
					listPages.put(event.getUser().getStringID(), listPages.get(event.getUser().getStringID()) + 1);
				}

				else if (event.getReaction().getEmoji().toString().equals("◀"))
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
							channel = Main.client.getChannelByID(Long.parseLong(entry.split(":")[0])).mention();
						}

						if (entry.split(":")[1].equalsIgnoreCase("all"))
						{
							command = "All Commands";
						}

						blacklistArray.add(channel + " **|** " + command);
					}

					IMessage m = Util.buildPage(blacklistArray, "Blacklisted Channels/Commands", 10, listPages.get(event.getUser().getStringID()) - 1, false, false, event.getChannel(), event.getUser());

					listMessages.put(event.getUser().getStringID(), m.getStringID());
					waitForReaction(m.getStringID(), event.getUser().getStringID());
					listPages.put(event.getUser().getStringID(), listPages.get(event.getUser().getStringID()) - 1);
				}
			}
		}
	}
}