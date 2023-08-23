package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.SocketException;

public class Benchmark extends AbstractCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, MessageChannel channel, User user) throws Exception
	{
		String origQuery = event.getInteraction().getCommandInteraction().get().getOption("query").get().getValue().get().asString();
		String query = origQuery;
		Document searchDoc;

		if (query.contains(" "))
			query = query.replace(" ", "+");

		try
		{
			searchDoc = Jsoup.connect("https://www.passmark.com/search/zoomsearch.php?zoom_query=" + query + "&zoom_cat=5").userAgent(Util.getUserAgent()).get();
		}
		catch (SocketException e)
		{
			Util.editReply(event, "A connection error occured with PassMark, please try again later.");
			return;
		}

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
			link = link.split("\"")[1].replace("amp;", "");
			Document benchDoc;

			try
			{
				benchDoc = Jsoup.connect(link).userAgent(Util.getUserAgent()).get();
			}
			catch (SocketException e)
			{
				Util.editReply(event, "A connection error occured with PassMark, please try again later.");
				return;
			}

			Color embedColor;
			String fullDesc = benchDoc.select("div[class=ov-scroll]").html().replace("<a href=\"#history\">", "");
			String name = benchDoc.select("span[class=cpuname]").text();
			String score = benchDoc.select("span[style=font-family: Arial, Helvetica, sans-serif;font-size: 44px;	font-weight: bold; color: #F48A18;]").text();
			String rank;
			String samples = benchDoc.select("div[class=right-desc]").text().split("Samples: ")[1].split("\\*")[0];
			String ratio;
			String turboSpeed = "N/A";
			String singleThread = "N/A";
			String clockSpeed = "N/A";
			String tdp = "N/A";
			String socket = "N/A";
			String price;
			String date;
			String cores;
			String threads;

			//if samples is the last entry in the right description, there will be a "+ Compare" from the button below it
			if (samples.contains("Compare"))
				samples = samples.split(" ")[0];

			if (Integer.parseInt(samples) <= 4)
				embedColor = Color.of(255, 0, 0);
			else if (Integer.parseInt(samples) <= 24)
				embedColor = Color.of(255, 160, 0);
			else
				embedColor = Color.of(0, 200, 0);

			if (link.contains("gpu.php"))
			{
				date = fullDesc.split("Videocard First Benchmarked:</strong> ")[1].split("<")[0];
				price = fullDesc.split("Last Price Change:</strong>&nbsp;&nbsp;")[1].split(" \\(")[0].split("<")[0];
				ratio = fullDesc.split("G3DMark/Price: </strong>")[1].split("<")[0];
				rank = fullDesc.split("Overall Rank:</strong>&nbsp;&nbsp;")[1].split("<")[0];

				if (price.equals("NA"))
					price = "N/A";

				if (ratio.equals("NA"))
					ratio = "N/A";

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

				Util.editReply(event, "", embed);
			}
			else if (link.contains("cpu.php"))
			{
				date = fullDesc.split("CPU First Seen on Charts:</strong> ")[1].split("<")[0];
				price = fullDesc.split("Last Price Change:</strong> ")[1].split("<")[0];
				rank = fullDesc.split("Overall Rank:</strong> ")[1].split("<")[0];
				ratio = fullDesc.split("CPUmark/\\$Price:</strong> ")[1].split("<")[0];

				// New cores format, Intel 12-13th gen & beyond
				if (fullDesc.contains("Total Cores:</strong> "))
				{
					String totalCores = fullDesc.split("Total Cores:</strong> ")[1].split("<")[0];
					cores = totalCores.split(" ")[0];
					threads = totalCores.split(" Cores, ")[1].split(" Threads")[0];

					String perfCores = fullDesc.split("Performance Cores:</strong> ")[1].split("<")[0];

					if (perfCores.contains("Base"))
						clockSpeed = perfCores.split(" Threads, ")[1].split(" Base, ")[0];

					if (perfCores.contains("Turbo"))
						turboSpeed = perfCores.split(" Base, ")[1].split(" Turbo")[0];
				}
				// Standard cores format
				else
				{
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

					if (fullDesc.contains("Clockspeed:</strong> "))
						clockSpeed = fullDesc.split("Clockspeed:</strong> ")[1].split("<")[0];

					if (fullDesc.contains("Turbo Speed:</strong> "))
						turboSpeed = fullDesc.split("Turbo Speed:</strong> ")[1].split("<")[0];
				}

				if (fullDesc.contains("Single Thread Rating:</strong> "))
					singleThread = fullDesc.split("Single Thread Rating:</strong> ")[1].split("<")[0].replace("\n", "");

				if (fullDesc.contains("Typical TDP:</strong> "))
					tdp = fullDesc.split("Typical TDP:</strong> ")[1].split("<")[0];

				if (fullDesc.contains("Socket:</strong> "))
				{
					// this heading is sometimes present with no value??
					String verify = fullDesc.split("Socket:</strong> ")[1].split("<")[0];

					if (!verify.equals(""))
						socket = verify;
				}

				if (price.equals("NA"))
					price = "N/A";

				if (ratio.equals("NA"))
					ratio = "N/A";

				if (tdp.equals("-1 W"))
					tdp = "N/A";

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

				Util.editReply(event, "", embed);
			}
		}
		else
		{
			Util.editReply(event, "I couldn't find any results for \"" + origQuery + "\"!");
		}
	}

	@Override
	public ApplicationCommandRequest getCommandRequest()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Provides complete benchmark information on a GPU or CPU powered by PassMark")
			.addOption(ApplicationCommandOptionData.builder()
				.name("query")
				.description("Search query for GPU or CPU")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.required(true)
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "benchmark";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}