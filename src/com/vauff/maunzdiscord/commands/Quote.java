package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import org.apache.commons.lang3.math.NumberUtils;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Quote extends AbstractCommand<MessageReceivedEvent>
{

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
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

						int secondQuoteID = 10 * page;
						int firstQuoteID = secondQuoteID + 1 - 10;

						Util.sqlConnect();

						PreparedStatement pst = Util.sqlCon.prepareStatement("SELECT * FROM quotes WHERE id>=" + firstQuoteID + " AND id<=" + secondQuoteID + ";");
						ResultSet rs = pst.executeQuery();
						PreparedStatement secondPst = Util.sqlCon.prepareStatement("SELECT COUNT(id) AS id FROM quotes;");
						ResultSet secondRs = secondPst.executeQuery();

						secondRs.next();

						if (page >= 1 && page <= (int) Math.ceil(secondRs.getDouble("id") / 10))
						{
							StringBuilder list = new StringBuilder();

							list.append("```" + System.lineSeparator());
							Util.msg(event.getChannel(), event.getAuthor(), "--- **Page " + page + "/" + (int) Math.ceil(secondRs.getDouble("id") / 10) + "** ---");

							while (rs.next())
							{
								if (rs.getInt("approved") == 1)
								{
									list.append(rs.getInt("id") + " - " + rs.getString("title") + System.lineSeparator());
								}
							}

							list.append("```");
							Util.msg(event.getChannel(), event.getAuthor(), list.toString());
						}
						else
						{
							Util.msg(event.getChannel(), event.getAuthor(), "That page doesn't exist!");
						}

						Util.sqlCon.abort(command ->
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
						Util.msg(event.getChannel(), event.getAuthor(), "You need to give me a quote ID! **Usage: *quote view <quoteid>**");
					}
					else
					{
						if (NumberUtils.isCreatable(args[2]))
						{
							Util.sqlConnect();
							PreparedStatement pst = Util.sqlCon.prepareStatement("SELECT * FROM quotes WHERE id='" + args[2] + "'");
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
							Util.sqlCon.abort(command ->
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
					Util.msg(event.getChannel(), event.getAuthor(), "The argument **" + args[1] + "** was not recognized! **Usage: *quote <view/list/add> <quoteid>/[page]**");

					break;
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
}