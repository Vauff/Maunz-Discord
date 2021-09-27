package com.vauff.maunzdiscord.commands.legacy;

import com.vauff.maunzdiscord.commands.templates.AbstractLegacyCommand;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.objects.CommandHelp;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Benchmark extends AbstractLegacyCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

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
				String fullDesc = benchDoc.select("div[class=ov-scroll]").html().replace("<a href=\"#history\">", "");
				String name = benchDoc.select("span[class=cpuname]").text();
				String score = benchDoc.select("span[style=font-family: Arial, Helvetica, sans-serif;font-size: 44px;	font-weight: bold; color: #F48A18;]").text();
				String rank = fullDesc.split("Overall Rank:</strong>&nbsp;&nbsp;")[1].split("<")[0];
				String samples = benchDoc.select("div[class=right-desc]").text().split("Samples: ")[1].split("\\*")[0];
				String ratio = "N/A";
				String turboSpeed = "N/A";
				String singleThread = "N/A";
				String clockSpeed = "N/A";
				String tdp = "N/A";
				String socket = "N/A";
				String price = "N/A";
				String date;
				String cores;
				String threads;

				if (ratio.equals("NA"))
				{
					ratio = "N/A";
				}

				//if samples is the last entry in the right description, there will be a "+ Compare" from the button below it
				if (samples.contains("Compare"))
				{
					samples = samples.split(" ")[0];
				}

				if (Integer.parseInt(samples) <= 4)
				{
					embedColor = Color.of(255, 0, 0);
				}
				else if (Integer.parseInt(samples) <= 24)
				{
					embedColor = Color.of(255, 160, 0);
				}
				else
				{
					embedColor = Color.of(0, 200, 0);
				}

				if (link.contains("gpu.php"))
				{
					date = fullDesc.split("Videocard First Benchmarked:</strong> ")[1].split("<")[0];
					price = fullDesc.split("Last Price Change:</strong>&nbsp;&nbsp;")[1].split(" \\(")[0].split("<")[0];
					ratio = fullDesc.split("Price: </strong>")[1].split("<")[0];

					if (price.equals("NA"))
					{
						price = "N/A";
					}

					if (ratio.equals("NA"))
					{
						ratio = "N/A";
					}

					EmbedCreateSpec embed = EmbedCreateSpec.builder()
						.color(embedColor)
						.thumbnail("https://i.imgur.com/nAe3jfd.jpg")
						.title(name)
						.url(link)
						.footer("Powered by PassMark", "")
						.addField("Score", score, true)
						.addField("Rank", rank, true)
						.addField("Samples", samples, true)
						.addField("First Benchmarked", date, true)
						.addField("Price", price, true)
						.addField("Performance Per Dollar", ratio, true)
						.build();

					Util.msg(channel, author, embed);
				}
				else if (link.contains("cpu.php"))
				{
					date = fullDesc.split("CPU First Seen on Charts:</strong>&nbsp;&nbsp;")[1].split("<")[0];
					price = fullDesc.split("Last Price Change:</strong>&nbsp;&nbsp;")[1].split("<")[0];
					cores = fullDesc.split("Cores:</strong> ")[1].split("<")[0];

					if (cores.contains(" physical modules)"))
					{
						threads = cores.split(" \\(in ")[0];
						cores = cores.split(" \\(in ")[1].split(" physical modules\\)")[0];
					}
					else
					{
						threads = fullDesc.split("Threads:</strong> ")[1].split("<")[0];
					}

					ratio = fullDesc.split("Price:</strong>&nbsp;&nbsp;")[1].split("&")[0];

					if (fullDesc.contains("Clockspeed:</strong> "))
					{
						clockSpeed = fullDesc.split("Clockspeed:</strong> ")[1].split("<")[0];
					}

					if (fullDesc.contains("Single Thread Rating: </strong>"))
					{
						singleThread = fullDesc.split("Single Thread Rating: </strong>")[1].split("<")[0].replace("\n", "");
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

					if (ratio.equals("NA"))
					{
						ratio = "N/A";
					}

					if (tdp.equals("-1 W"))
					{
						tdp = "N/A";
					}

					EmbedCreateSpec embed = EmbedCreateSpec.builder()
						.color(embedColor)
						.thumbnail("https://i.imgur.com/iKLrQQN.jpg")
						.title(name)
						.url(link)
						.footer("Powered by PassMark", "")
						.addField("Score", score, true)
						.addField("Single Thread Score", singleThread, true)
						.addField("Rank", rank, true)
						.addField("Samples", samples, true)
						.addField("First Benchmarked", date, true)
						.addField("Cores", cores, true)
						.addField("Threads", threads, true)
						.addField("Price", price, true)
						.addField("Performance Per Dollar", ratio, true)
						.addField("Clock Speed", clockSpeed, true)
						.addField("Turbo Speed", turboSpeed, true)
						.addField("Socket", socket, true)
						.addField("Typical TDP", tdp, true)
						.build();

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
		return new String[] { "benchmark" };
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}

	@Override
	public CommandHelp[] getHelp()
	{
		return new CommandHelp[] { new CommandHelp("<gpu/cpu>", "Provides complete benchmark information on a GPU or CPU powered by PassMark.") };
	}
}