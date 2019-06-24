package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.Color;
import java.util.function.Consumer;

public class Benchmark extends AbstractCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().get().split(" ");

		if (args.length == 1)
		{
			Util.msg(channel, author, "You need to provide a GPU/CPU name to benchmark!");
		}
		else
		{
			String query = Util.addArgs(args, 1);

			if (query.contains(" "))
			{
				query = query.replace(" ", "+");
			}

			Document searchDoc = Jsoup.connect("https://www.passmark.com/search/zoomsearch.php?zoom_query=" + query + "&zoom_cat=5").get();
			String searchHtml = searchDoc.select("div[class=result_title]").html();
			String[] searchHtmlSplit = searchHtml.split(" ");
			String link = "";

			for (String l : searchHtmlSplit)
			{
				if ((l.contains("https://")) && (l.contains("gpu.php") || l.contains("cpu.php")))
				{
					link = l;
					break;
				}
			}

			if (!link.equals(""))
			{
				link = link.split("\"")[1];

				link = link.replace("amp;", "");
				Document benchDoc = Jsoup.connect(link).get();
				Color embedColor;
				String fullDesc = benchDoc.select("table[class=desc]").html().replace("<a href=\"#history\">", "");
				String name = benchDoc.select("span[class=cpuname]").text();
				String score = benchDoc.select("span[style=font-family: Arial, Helvetica, sans-serif;font-size: 35px;	font-weight: bold; color: red;]").text();
				String rank = fullDesc.split("Overall Rank:</span>&nbsp;&nbsp;")[1].split("<")[0];
				String samples = benchDoc.select("td[style=text-align: center]").text().split("Samples: ")[1].split("\\*")[0];
				String ratio = fullDesc.split("Price:</span>&nbsp;&nbsp;")[1].split("&")[0];
				String turboSpeed = "N/A";
				String singleThread = "N/A";
				String clockSpeed = "N/A";
				String tdp = "N/A";
				String socket = "N/A";
				String price;
				String date;
				String cores;

				if (ratio.equals("NA"))
				{
					ratio = "N/A";
				}

				if (Integer.parseInt(samples) <= 4)
				{
					embedColor = new Color(255, 0, 0);
				}
				else if (Integer.parseInt(samples) <= 24)
				{
					embedColor = new Color(255, 160, 0);
				}
				else
				{
					embedColor = new Color(0, 200, 0);
				}

				if (link.contains("gpu.php"))
				{
					date = fullDesc.split("Videocard First Benchmarked:</span>&nbsp;&nbsp;")[1].split("<")[0];
					price = fullDesc.split("Last Price Change:</span>&nbsp;&nbsp;")[1].split(" \\(")[0].split("<")[0];

					if (price.equals("NA"))
					{
						price = "N/A";
					}

					final String finalLink = link;
					final String finalDate = date;
					final String finalPrice = price;
					final String finalRatio = ratio;

					Consumer<EmbedCreateSpec> embed = spec ->
					{
						spec.setColor(embedColor);
						spec.setThumbnail("https://i.imgur.com/nAe3jfd.jpg");
						spec.setTitle(name);
						spec.setUrl(finalLink);
						spec.setFooter("Powered by PassMark", "");
						spec.addField("Score", score, true);
						spec.addField("Rank", rank, true);
						spec.addField("Samples", samples, true);
						spec.addField("First Benchmarked", finalDate, true);
						spec.addField("Price", finalPrice, true);
						spec.addField("Performance Per Dollar", finalRatio, true);
					};

					Util.msg(channel, author, embed);
				}
				if (link.contains("cpu.php"))
				{
					date = fullDesc.split("CPU First Seen on Charts:</span>&nbsp;&nbsp;")[1].split("<")[0];
					price = fullDesc.split("Last Price Change:</span>&nbsp;&nbsp;")[1].split("<")[0];
					cores = fullDesc.split("No of Cores:</strong> ")[1].split("<")[0];

					if (fullDesc.contains("Clockspeed:</strong> "))
					{
						clockSpeed = fullDesc.split("Clockspeed:</strong> ")[1].split("<")[0];
					}

					if (fullDesc.contains("<br><br> Single Thread Rating: "))
					{
						singleThread = fullDesc.split("<br><br> Single Thread Rating: ")[1].split("<")[0];
					}

					if (fullDesc.contains("Turbo Speed:</strong> "))
					{
						turboSpeed = fullDesc.split("Turbo Speed:</strong> ")[1].split("<")[0];
					}

					if (fullDesc.contains("Typical TDP:</strong> "))
					{
						tdp = fullDesc.split("Typical TDP:</strong> ")[1].split("<")[0];
					}

					if (fullDesc.contains("Socket:</strong> "))
					{
						socket = fullDesc.split("Socket:</strong> ")[1].split("<")[0];
					}

					if (price.equals("NA"))
					{
						price = "N/A";
					}

					if (tdp.equals("-1 W"))
					{
						tdp = "N/A";
					}

					final String finalLink = link;
					final String finalDate = date;
					final String finalPrice = price;
					final String finalRatio = ratio;
					final String finalSingleThread = singleThread;
					final String finalClockSpeed = clockSpeed;
					final String finalTurboSpeed = turboSpeed;
					final String finalSocket = socket;
					final String finalTdp = tdp;

					Consumer<EmbedCreateSpec> embed = spec ->
					{
						spec.setColor(embedColor);
						spec.setThumbnail("https://i.imgur.com/iKLrQQN.jpg");
						spec.setTitle(name);
						spec.setUrl(finalLink);
						spec.setFooter("Powered by PassMark", "");
						spec.addField("Score", score, true);
						spec.addField("Single Thread Score", finalSingleThread, true);
						spec.addField("Rank", rank, true);
						spec.addField("Samples", samples, true);
						spec.addField("First Benchmarked", finalDate, true);
						spec.addField("Cores", cores, true);
						spec.addField("Price", finalPrice, true);
						spec.addField("Performance Per Dollar", finalRatio, true);
						spec.addField("Clock Speed", finalClockSpeed, true);
						spec.addField("Turbo Speed", finalTurboSpeed, true);
						spec.addField("Socket", finalSocket, true);
						spec.addField("Typical TDP", finalTdp, true);
					};

					Util.msg(channel, author, embed);
				}
			}
			else
			{
				Util.msg(channel, author, "I couldn't find any results for \"" + Util.addArgs(args, 1) + "\"!");
			}
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*benchmark" };
	}
}