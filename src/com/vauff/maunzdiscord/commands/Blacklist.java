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
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class Blacklist extends AbstractCommand<MessageCreateEvent>
{
	private static HashMap<Snowflake, Integer> listPages = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> listMessages = new HashMap<>();

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
					List<String> blacklist = Main.mongoDatabase.getCollection("guilds").find(eq("guildId", event.getGuild().block().getId().asLong())).first().getList("blacklist", String.class);

					if (args[1].equalsIgnoreCase("list"))
					{
						if (blacklist.size() != 0)
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

								ArrayList<String> blacklistFormatted = new ArrayList<>();

								for (String entry : blacklist)
								{
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

									blacklistFormatted.add(channelSelector + " **|** " + command);
								}

								Message m = Util.buildPage(blacklistFormatted, "Blacklisted Channels/Commands", 10, page, false, false, channel, author);

								listMessages.put(author.getId(), m.getId());
								waitForReaction(m.getId(), author.getId());
								listPages.put(author.getId(), page);
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

							for (String entry : blacklist)
							{
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

									Main.mongoDatabase.getCollection("guilds").updateOne(eq("guildId", event.getGuild().block().getId().asLong()), new Document("$pull", new Document("blacklist", channel.getId().asString() + ":" + input)));
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

								Main.mongoDatabase.getCollection("guilds").updateOne(eq("guildId", event.getGuild().block().getId().asLong()), new Document("$push", new Document("blacklist", channel.getId().asString() + ":" + input)));
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

								for (String entry : blacklist)
								{
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

										Main.mongoDatabase.getCollection("guilds").updateOne(eq("guildId", event.getGuild().block().getId().asLong()), new Document("$pull", new Document("blacklist", location + ":" + input)));
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

									Main.mongoDatabase.getCollection("guilds").updateOne(eq("guildId", event.getGuild().block().getId().asLong()), new Document("$push", new Document("blacklist", location + ":" + input)));
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
		if (listMessages.containsKey(event.getUser().block().getId()) && message.getId().equals(listMessages.get(event.getUser().block().getId())))
		{
			List<String> blacklist = Main.mongoDatabase.getCollection("guilds").find(eq("guildId", event.getGuild().block().getId().asLong())).first().getList("blacklist", String.class);

			if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("▶"))
			{
				ArrayList<String> blacklistArray = new ArrayList<>();

				for (String entry : blacklist)
				{
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

				Message m = Util.buildPage(blacklistArray, "Blacklisted Channels/Commands", 10, listPages.get(event.getUser().block().getId()) + 1, false, false, event.getChannel().block(), event.getUser().block());

				listMessages.put(event.getUser().block().getId(), m.getId());
				waitForReaction(m.getId(), event.getUser().block().getId());
				listPages.put(event.getUser().block().getId(), listPages.get(event.getUser().block().getId()) + 1);
			}

			else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("◀"))
			{
				ArrayList<String> blacklistArray = new ArrayList<>();

				for (String entry : blacklist)
				{
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

				Message m = Util.buildPage(blacklistArray, "Blacklisted Channels/Commands", 10, listPages.get(event.getUser().block().getId()) - 1, false, false, event.getChannel().block(), event.getUser().block());

				listMessages.put(event.getUser().block().getId(), m.getId());
				waitForReaction(m.getId(), event.getUser().block().getId());
				listPages.put(event.getUser().block().getId(), listPages.get(event.getUser().block().getId()) - 1);
			}
		}
	}
}