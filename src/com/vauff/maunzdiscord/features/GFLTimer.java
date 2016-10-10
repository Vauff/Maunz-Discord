package com.vauff.maunzdiscord.features;

import java.io.File;
import java.net.SocketTimeoutException;

import org.apache.commons.io.FileUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

public class GFLTimer
{
	private static File file = new File(Util.getJarLocation() + "lastmap.txt");

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

				if (!map.equals("") && !Util.getFileContents("lastmap.txt").equals(map))
				{
					Util.msg(Util.mapChannel, "GFL Zombie Escape is now playing: **" + map.replace("_", "\\_") + "**");
				}

				if (map.equals(""))
				{
					FileUtils.writeStringToFile(file, " ", "UTF-8");
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
				Main.log.error(e);
			}
		}
	};
}