package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class Quote extends AbstractCommand<MessageCreateEvent>
{
	private static HashMap<Snowflake, Integer> listPages = new HashMap<>();
	private static HashMap<Snowflake, Snowflake> listMessages = new HashMap<>();
	private static Connection sqlCon;

	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		if (event.getGuild().block().getId().asLong() != 252536814324154368L && event.getGuild().block().getId().asLong() != 381499037926293506L)
		{
			Util.msg(channel, author, "This command is disabled in most guilds until a rewrite can be completed");
		}
		else
		{
			String[] args = event.getMessage().getContent().get().split(" ");

			if (args.length == 1)
			{
				Util.msg(channel, author, "You can view the quotes site here: https://vauff.com/quotes/");
			}
			else
			{
				switch (args[1].toLowerCase())
				{
					case "list":
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

							sqlConnect();

							PreparedStatement pst = sqlCon.prepareStatement("SELECT * FROM quotes;");
							ResultSet rs = pst.executeQuery();

							ArrayList<String> list = new ArrayList<>();

							while (rs.next())
							{
								if (rs.getInt("approved") == 1)
								{
									list.add(rs.getString("title"));
								}
							}

							Message m = Util.buildPage(list, "Quotes List", 10, page, true, true, channel, author);

							listMessages.put(author.getId(), m.getId());
							waitForReaction(m.getId(), author.getId());
							listPages.put(author.getId(), page);

							sqlCon.abort(command ->
							{
							});
						}
						else
						{
							Util.msg(channel, author, "Page numbers need to be numerical!");
						}

						break;
					case "view":
						if (args.length == 2)
						{
							Util.msg(channel, author, "You need to provide a quote ID! **Usage: \\*quote view <quoteid>**");
						}
						else
						{
							if (NumberUtils.isCreatable(args[2]))
							{
								sqlConnect();
								PreparedStatement pst = sqlCon.prepareStatement("SELECT * FROM quotes WHERE id='" + args[2] + "'");
								ResultSet rs = pst.executeQuery();

								if (!rs.next())
								{
									Util.msg(channel, author, "That quote doesn't exist!");
								}
								else
								{
									if (rs.getInt("approved") == 1)
									{
										int lines = 0;
										boolean cut = false;
										StringBuilder quote = new StringBuilder();

										quote.append("```" + System.lineSeparator());
										Util.msg(channel, author, "**ID:** " + rs.getString("id") + " **Title:** " + rs.getString("title") + " **Submitter:** " + rs.getString("submitter") + " **Date:** " + Util.getTime(rs.getLong("time") * 1000));

										for (String s : rs.getString("quote").split("\n"))
										{
											if (lines < 10)
											{
												lines++;
												quote.append(s + System.lineSeparator());
											}
											else
											{
												cut = true;
												break;
											}
										}

										quote.append("```");
										Util.msg(channel, author, quote.toString());

										if (cut)
										{
											Util.msg(channel, author, "The rest of this quote is too long for Discord. Please see the full quote at https://vauff.com/quotes/viewquote.php?id=" + args[2]);
										}
									}
									else
									{
										Util.msg(channel, author, "That quote hasn't been approved yet!");
									}
								}

								rs.close();
								pst.close();
								sqlCon.abort(command ->
								{
								});
							}
							else
							{
								Util.msg(channel, author, "Quote IDs need to be numerical!");
							}
						}

						break;
					case "add":
						Util.msg(channel, author, "You can submit new quotes here: https://vauff.com/quotes/addquote.php");

						break;
					default:
						Util.msg(channel, author, "The argument **" + args[1] + "** was not recognized! See **\\*help quote**");

						break;
				}
			}
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {
				"*quote",
				"*quotes"
		};
	}

	@Override
	public void onReactionAdd(ReactionAddEvent event, Message message) throws Exception
	{
		if (listMessages.containsKey(event.getUser().block().getId()) && message.getId().equals(listMessages.get(event.getUser().block().getId())))
		{
			if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("▶"))
			{
				sqlConnect();

				PreparedStatement pst = sqlCon.prepareStatement("SELECT * FROM quotes;");
				ResultSet rs = pst.executeQuery();

				ArrayList<String> list = new ArrayList<>();

				while (rs.next())
				{
					if (rs.getInt("approved") == 1)
					{
						list.add(rs.getString("title"));
					}
				}

				Message m = Util.buildPage(list, "Quotes List", 10, listPages.get(event.getUser().block().getId()) + 1, true, true, event.getChannel().block(), event.getUser().block());

				listMessages.put(event.getUser().block().getId(), m.getId());
				waitForReaction(m.getId(), event.getUser().block().getId());
				listPages.put(event.getUser().block().getId(), listPages.get(event.getUser().block().getId()) + 1);

				sqlCon.abort(command ->
				{
				});
			}

			else if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals("◀"))
			{
				sqlConnect();

				PreparedStatement pst = sqlCon.prepareStatement("SELECT * FROM quotes;");
				ResultSet rs = pst.executeQuery();

				ArrayList<String> list = new ArrayList<>();

				while (rs.next())
				{
					if (rs.getInt("approved") == 1)
					{
						list.add(rs.getString("title"));
					}
				}

				Message m = Util.buildPage(list, "Quotes List", 10, listPages.get(event.getUser().block().getId()) - 1, true, true, event.getChannel().block(), event.getUser().block());

				listMessages.put(event.getUser().block().getId(), m.getId());
				waitForReaction(m.getId(), event.getUser().block().getId());
				listPages.put(event.getUser().block().getId(), listPages.get(event.getUser().block().getId()) - 1);

				sqlCon.abort(command ->
				{
				});
			}
		}
	}

	/**
	 * Connects to the Chat-Quotes database
	 *
	 * @throws Exception
	 */
	private static void sqlConnect() throws Exception
	{
		JSONObject json = new JSONObject(Util.getFileContents("config.json")).getJSONObject("quotesDatabase");

		sqlCon = DriverManager.getConnection("jdbc:mysql://" + json.getString("hostname") + "/" + json.getString("database") + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false", json.getString("username"), json.getString("password"));
	}
}