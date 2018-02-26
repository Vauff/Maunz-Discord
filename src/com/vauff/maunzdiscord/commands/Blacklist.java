package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.MainListener;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

import java.io.File;

public class Blacklist extends AbstractCommand<MessageReceivedEvent>
{
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

					if (args.length == 2)
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
								Util.msg(event.getChannel(), event.getAuthor(), "The command **" + args[1] + "** doesn't exist!");
							}
						}
						else
						{
							Util.msg(event.getChannel(), event.getAuthor(), "You need to specify a channel to blacklist (or \"all\")! **Usage: *blacklist [all/channel] <all/command>**");
						}
					}
				}
				else
				{
					Util.msg(event.getChannel(), event.getAuthor(), "You need to specify a command to blacklist (or \"all\")! **Usage: *blacklist [all/channel] <all/command>**");
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
}