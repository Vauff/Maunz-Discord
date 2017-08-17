package com.vauff.maunzdiscord.commands;

import java.awt.Color;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

public class Benchmark extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length == 1)
		{
			Util.msg(event.getChannel(), "You need to provide a GPU/CPU name to benchmark!");
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

				if (link.contains("gpu.php"))
				{
					date = fullDesc.split("Videocard First Benchmarked:</span>&nbsp;&nbsp;")[1].split("<")[0];
					price = fullDesc.split("Last Price Change:</span>&nbsp;&nbsp;")[1].split(" \\(")[0].split("<")[0];

					if (price.equals("NA"))
					{
						price = "N/A";
					}

					EmbedObject embed = new EmbedBuilder().withColor(new Color(0, 154, 255)).withThumbnail("https://i.imgur.com/nAe3jfd.jpg").withTitle(name).withUrl(link).withFooterText("Powered by PassMark").appendField("Score", score, true).appendField("Rank", rank, true).appendField("Samples", samples, true).appendField("First Benchmarked", date, true).appendField("Price", price, true).appendField("Performance Per Dollar", ratio, true).build();
					Util.msg(event.getChannel(), embed);
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

					EmbedObject embed = new EmbedBuilder().withColor(new Color(0, 154, 255)).withThumbnail("https://i.imgur.com/iKLrQQN.jpg").withTitle(name).withUrl(link).withFooterText("Powered by PassMark").appendField("Score", score, true).appendField("Single Thread Score", singleThread, true).appendField("Rank", rank, true).appendField("Samples", samples, true).appendField("First Benchmarked", date, true).appendField("Cores", cores, true).appendField("Price", price, true).appendField("Performance Per Dollar", ratio, true).appendField("Clock Speed", clockSpeed, true).appendField("Turbo Speed", turboSpeed, true).appendField("Socket", socket, true).appendField("Typical TDP", tdp, true).build();
					Util.msg(event.getChannel(), embed);
				}
			}
			else
			{
				Util.msg(event.getChannel(), "I couldn't find any results for \"" + Util.addArgs(args, 1) + "\"!");
			}

		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*benchmark" };
	}
}