package com.vauff.maunzdiscord.features;

import java.io.File;
import java.net.SocketTimeoutException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.obj.IUser;

public class GFLTimer
{
	private static File file = new File(Util.getJarLocation() + "lastmap.txt");
	private static File mapsList = new File(Util.getJarLocation() + "maps.txt");

	public static Runnable timer = new Runnable()
	{
		public void run()
		{
			try
			{
				Document doc = Jsoup.connect("https://stats.gflclan.com/hlstats.php?game=csgoze").timeout(10000).get();
				String html = doc.select("td[class=game-table-cell]").text();
				String[] htmlSplit = html.split(" ");
				String map = "";

				for (String m : htmlSplit)
				{
					if (m.contains("_"))
					{
						map = m;
						break;
					}
				}

				if (map.equalsIgnoreCase("ze_Paranoid_Rezurrection_v11_9_") || map.equalsIgnoreCase("ze_industrial_dejavu_v3_3_3_e2_"))
				{
					map = StringUtils.replaceIgnoreCase(map, "ze_Paranoid_Rezurrection_v11_9_", "ze_Paranoid_Rezurrection_v11_9_th10");
					map = StringUtils.replaceIgnoreCase(map, "ze_industrial_dejavu_v3_3_3_e2_", "ze_industrial_dejavu_v3_3_3_e2_d");
				}

				if (!map.equals("") && !Util.getFileContents("lastmap.txt").equalsIgnoreCase(map) && !Util.getFileContents("lastmap.txt").equalsIgnoreCase(map + "_OLD-DATA"))
				{
					String mentions = "";
					File[] directoryListing = new File(Util.getJarLocation() + "map-notification-data/").listFiles();

					for (File dataFile : directoryListing)
					{
						if (StringUtils.containsIgnoreCase(FileUtils.readFileToString(dataFile, "UTF-8"), map))
						{
							IUser user = Main.client.getUserByID(dataFile.getName().replace(".txt", ""));

							mentions = mentions + user.mention() + " ";
						}
					}

					Util.msg(Util.mapChannel, mentions + "GFL Zombie Escape is now playing: **" + map.replace("_", "\\_") + "**");

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