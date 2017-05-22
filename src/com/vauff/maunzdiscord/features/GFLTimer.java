package com.vauff.maunzdiscord.features;

import java.awt.Color;
import java.io.File;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class GFLTimer
{
	public static String players = "Error";
	public static long timestamp = 0;
	private static File file = new File(Util.getJarLocation() + "lastmap.txt");
	private static File mapsList = new File(Util.getJarLocation() + "maps.txt");

	public static Runnable timer = new Runnable()
	{
		public void run()
		{
			try
			{
				if (Util.isEnabled)
				{
					Document mapDoc = Jsoup.connect("https://stats.gflclan.com/hlstats.php?game=csgoze").timeout(10000).get();
					String mapHtml = mapDoc.select("td[class=game-table-cell]").text();
					String[] mapHtmlSplit = mapHtml.split(" ");
					String map = "";
					Document playersDoc = Jsoup.connect("https://gflclan.com/index.php?app=gflcore&module=servers&controller=information&id=1").timeout(10000).get();
					String playersHtml = playersDoc.select("li[class=ipsResponsive_hidePhone]").text();
					String[] playersHtmlSplit = playersHtml.split(" ");

					if (!playersHtmlSplit[2].equals(""))
					{
						players = playersHtmlSplit[2];
					}

					for (String m : mapHtmlSplit)
					{
						if (m.contains("_"))
						{
							map = m;
							break;
						}
					}

					List<String> cutMapNames = Arrays.asList("ze_paranoid_rezurrection_v11_9_", "ze_industrial_dejavu_v3_3_3_e2_", "ze_dangerous_waters_in_christma", "ze_destruction_of_exorath_v4_li", "ze_insensible_sr8gm6yj12hs7_rg_", "ze_ffxii_westersand_v7_2_e2_fix");
					List<String> fixedMapNames = Arrays.asList("ze_paranoid_rezurrection_v11_9_ps12", "ze_industrial_dejavu_v3_3_3_e2_d", "ze_dangerous_waters_in_christmas_day", "ze_destruction_of_exorath_v4_lite", "ze_insensible_sr8gm6yj12hs7_rg_v9_3_1", "ze_ffxii_westersand_v7_2_e2_fix3");
					int iteration = -1;

					for (String cutMap : cutMapNames)
					{
						iteration++;

						if (cutMap.equalsIgnoreCase(map))
						{
							map = fixedMapNames.get(iteration);
						}
					}

					if (!map.equals("") && !Util.getFileContents("lastmap.txt").equalsIgnoreCase(map) && !Util.getFileContents("lastmap.txt").equalsIgnoreCase(map + "_OLD-DATA"))
					{
						timestamp = System.currentTimeMillis();
						File[] directoryListing = new File(Util.getJarLocation() + "map-notification-data/").listFiles();

						EmbedObject embed = new EmbedBuilder().withColor(new Color(0, 154, 255)).withTimestamp(timestamp).withThumbnail("https://vauff.me/mapimgs/" + map + ".jpg").withDescription("Now Playing: **" + map.replace("_", "\\_") + "**\nPlayers Online: **" + players + "**\nQuick Join: **steam://connect/216.52.148.47:27015**").build();
						Util.msg(Util.mapChannel, embed);

						for (File dataFile : directoryListing)
						{
							if (Util.getFileContents(dataFile).contains(System.getProperty("line.separator")) || Util.getFileContents(dataFile).contains("﻿"))
							{
								FileUtils.writeStringToFile(dataFile, Util.getFileContents(dataFile).replace(System.getProperty("line.separator"), "").replace("﻿", ""), "UTF-8");
							}

							String[] mapNotifications = FileUtils.readFileToString(dataFile, "UTF-8").split(",");

							for (String mapNotification : mapNotifications)
							{
								if (mapNotification.equalsIgnoreCase(map))
								{
									try
									{
										IUser user = Main.client.getUserByID(Long.parseLong(dataFile.getName().replace(".txt", "")));
										Util.msg(Main.client.getOrCreatePMChannel(user), embed);
									}
									catch (NullPointerException e)
									{
										Main.log.error("", e);
										// This means that either a bad user ID was
										// provided by the notification file, or the
										// users account doesn't exist anymore
									}
								}
							}
						}

						if (!StringUtils.containsIgnoreCase(Util.getFileContents("maps.txt"), map))
						{
							if (Util.getFileContents("maps.txt").equals(" "))
							{
								FileUtils.writeStringToFile(mapsList, map, "UTF-8");
							}
							else
							{
								FileUtils.writeStringToFile(mapsList, Util.getFileContents("maps.txt") + "," + map, "UTF-8");
							}
						}
					}

					if (map.equals(""))
					{
						FileUtils.writeStringToFile(file, Util.getFileContents("lastmap.txt") + "_OLD-DATA", "UTF-8");
					}
					else
					{
						FileUtils.writeStringToFile(file, map, "UTF-8");
					}
				}
			}
			catch (SocketTimeoutException e)
			{
				Main.log.error("Failed to connect to the GFL HLStatsX page, automatically retrying in 1 minute");
			}
			catch (Exception e)
			{
				Main.log.error("", e);
			}
		}
	};
}