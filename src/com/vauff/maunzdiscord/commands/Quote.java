package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class Quote extends AbstractCommand<MessageReceivedEvent>
{
	private static HashMap<String, Integer> listPages = new HashMap<>();
	private static HashMap<String, String> listMessages = new HashMap<>();
	private static Connection sqlCon;

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		if (event.getGuild().getLongID() != 252536814324154368L) // hardcoding is bad mmkay, but this is temporary
		{
			Util.msg(event.getChannel(), event.getAuthor(), "The quote command has been temporarily disabled in most guilds until a rewrite can be completed");
		}
		else
		{
			String[] args = event.getMessage().getContent().split(" ");

			if (args.length == 1)
			{
				Util.msg(event.getChannel(), event.getAuthor(), "You can view the quotes site here: http://158.69.59.239/quotes/");
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

							IMessage m = Util.buildPage(list, "Quotes List", 10, page, true, true, event.getChannel(), event.getAuthor());

							listMessages.put(event.getAuthor().getStringID(), m.getStringID());
							waitForReaction(m.getStringID(), event.getAuthor().getStringID());
							listPages.put(event.getAuthor().getStringID(), page);

							sqlCon.abort(command ->
							{
							});
						}
						else
						{
							Util.msg(event.getChannel(), event.getAuthor(), "Page numbers need to be numerical!");
						}

						break;
					case "view":
						if (args.length == 2)
						{
							Util.msg(event.getChannel(), event.getAuthor(), "You need to provide a quote ID! **Usage: \\*quote view <quoteid>**");
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
									Util.msg(event.getChannel(), event.getAuthor(), "That quote doesn't exist!");
								}
								else
								{
									if (rs.getInt("approved") == 1)
									{
										int lines = 0;
										boolean cut = false;
										StringBuilder quote = new StringBuilder();

										quote.append("```" + System.lineSeparator());
										Util.msg(event.getChannel(), event.getAuthor(), "**ID:** " + rs.getString("id") + " **Title:** " + rs.getString("title") + " **Submitter:** " + rs.getString("submitter") + " **Date:** " + Util.getTime(rs.getLong("time") * 1000));

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
										Util.msg(event.getChannel(), event.getAuthor(), quote.toString());

										if (cut)
										{
											Util.msg(event.getChannel(), event.getAuthor(), "The rest of this quote is too long for Discord. Please see the full quote at http://158.69.59.239/quotes/viewquote.php?id=" + args[2]);
										}
									}
									else
									{
										Util.msg(event.getChannel(), event.getAuthor(), "That quote hasn't been approved yet!");
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
								Util.msg(event.getChannel(), event.getAuthor(), "Quote IDs need to be numerical!");
							}
						}

						break;
					case "add":
						Util.msg(event.getChannel(), event.getAuthor(), "You can submit new quotes here: http://158.69.59.239/quotes/addquote.php");

						break;
					default:
						Util.msg(event.getChannel(), event.getAuthor(), "The argument **" + args[1] + "** was not recognized! See **\\*help quote**");

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
	public void onReactionAdd(ReactionAddEvent event) throws Exception
	{
		if (listMessages.containsKey(event.getUser().getStringID()))
		{
			if (event.getMessage().getStringID().equals(listMessages.get(event.getUser().getStringID())))
			{
				if (event.getReaction().getEmoji().toString().equals("▶"))
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

					IMessage m = Util.buildPage(list, "Quotes List", 10, listPages.get(event.getUser().getStringID()) + 1, true, true, event.getChannel(), event.getUser());

					listMessages.put(event.getUser().getStringID(), m.getStringID());
					waitForReaction(m.getStringID(), event.getUser().getStringID());
					listPages.put(event.getUser().getStringID(), listPages.get(event.getUser().getStringID()) + 1);

					sqlCon.abort(command ->
					{
					});
				}

				else if (event.getReaction().getEmoji().toString().equals("◀"))
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

					IMessage m = Util.buildPage(list, "Quotes List", 10, listPages.get(event.getUser().getStringID()) - 1, true, true, event.getChannel(), event.getUser());

					listMessages.put(event.getUser().getStringID(), m.getStringID());
					waitForReaction(m.getStringID(), event.getUser().getStringID());
					listPages.put(event.getUser().getStringID(), listPages.get(event.getUser().getStringID()) - 1);

					sqlCon.abort(command ->
					{
					});
				}
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
		JSONObject json = new JSONObject(Util.getFileContents("config.json"));

		sqlCon = DriverManager.getConnection("jdbc:mysql://" + json.getJSONObject("database").getString("hostname") + "/ircquotes?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false", json.getJSONObject("database").getString("username"), json.getJSONObject("database").getString("password"));
	}
}