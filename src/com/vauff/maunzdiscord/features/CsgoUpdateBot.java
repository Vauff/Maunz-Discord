package com.vauff.maunzdiscord.features;

import java.io.File;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

import org.apache.commons.io.FileUtils;

import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;

import org.json.JSONObject;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

public class CsgoUpdateBot extends PircBot
{
	private static String lastChangelistNumber = "";
	public static String listeningNick;

	public CsgoUpdateBot()
	{
		if (Util.devMode)
		{
			this.setName("MaunzDev");
		}
		else
		{
			this.setName("Maunz");
		}
	}

	public void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		try
		{
			if (sender.equals(listeningNick))
			{
				if (message.contains("https://steamdb.info/changelist/"))
				{
					lastChangelistNumber = Colors.removeFormattingAndColors(message.split(" ")[2]);
				}

				if (Colors.removeFormattingAndColors(message).contains("App: 730 - Counter-Strike: Global Offensive"))
				{
					String consistentLastChangelistNumber = lastChangelistNumber;
					Document doc = null;
					boolean tryStatus = true;
					int attempts = 0;

					Thread.sleep(10000);

					while (tryStatus)
					{
						try
						{
							doc = Jsoup.connect("https://steamdb.info/app/730/history").userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.91 Safari/537.36").get();
							tryStatus = false;
						}
						catch (HttpStatusException | ConnectException | UnknownHostException | SocketTimeoutException e)
						{
							if (attempts < 19)
							{
								Main.log.error("Failed to connect to the CS:GO SteamDB history page, automatically retrying in 5 seconds");
								attempts++;
								Thread.sleep(5000);
							}
							else
							{
								Main.log.error("Failed to connect to the CS:GO SteamDB history page 20 times, giving up");
								tryStatus = false;
								return;
							}
						}
					}

					String html = doc.select("div[data-changeid=\"" + consistentLastChangelistNumber + "\"]").text();
					String htmlRaw = doc.select("div[data-changeid=\"" + consistentLastChangelistNumber + "\"]").html();

					for (File file : new File(Util.getJarLocation() + "data/services/csgo-updates").listFiles())
					{
						JSONObject json = new JSONObject(Util.getFileContents(file));

						try
						{
							Main.client.getGuildByID(Long.parseLong(file.getName().replace(".json", "")));
						}
						catch (NullPointerException e)
						{
							Main.log.warn("The bot has been removed from the guild belonging to the ID " + file.getName().replace(".json", "") + ", the CS:GO update service loop will move on to the next guild");
							continue;
						}

						if (json.getBoolean("enabled") && Util.isEnabled(Main.client.getGuildByID(Long.parseLong(file.getName().replace(".json", "")))))
						{
							json.put("lastGuildName", Main.client.getGuildByID(Long.parseLong(file.getName().replace(".json", ""))).getName());
							FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");

							if (html.contains("branches/public/buildid"))
							{
								Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), "SteamDB has spotted a public branch update for CS:GO on the 730 app, this means **an update was pushed to the Steam client!** <https://steamdb.info/app/730/history/>");
							}
							else if (html.contains("branches/dpr/buildid"))
							{
								if (json.getBoolean("earlyWarnings"))
								{
									Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), "SteamDB has spotted a DPR branch update for CS:GO on the 730 app, this means **an update is probably coming.** <https://steamdb.info/app/730/history/>");
								}
							}
							else if (!htmlRaw.contains("octicon octicon-diff-removed") && (html.replaceAll("\\d", "").contains("branches/./buildid") || html.replaceAll("\\d", "").contains("branches/../buildid") || html.replaceAll("\\d", "").contains("branches/.../buildid")))
							{
								if (json.getBoolean("earlyWarnings"))
								{
									Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), "SteamDB has spotted a version branch update for CS:GO on the 730 app, this means **an update might be coming.** <https://steamdb.info/app/730/history/>");
								}
							}
							else if (!htmlRaw.contains("octicon octicon-diff-removed") && (html.replaceAll("\\d", "").contains("branches/.-rc/buildid") || html.replaceAll("\\d", "").contains("branches/..-rc/buildid") || html.replaceAll("\\d", "").contains("branches/...-rc/buildid")))
							{
								Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), "SteamDB has spotted a beta branch update for CS:GO on the 730 app, this means **a beta update was pushed to the Steam client!** <https://steamdb.info/app/730/history/>");
							}
							else
							{
								if (json.getBoolean("nonImportantUpdates"))
								{
									Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), "SteamDB has spotted a non-important update for CS:GO on the 730 app, this **most likely doesn't mean anything.** <https://steamdb.info/app/730/history/>");
								}
							}
						}
					}
				}

				if (Colors.removeFormattingAndColors(message).contains("App: 741 - SteamDB Unknown App 741 (Counter-Strike Global Offensive - Valve Dedicated Server) (needs token)"))
				{
					for (File file : new File(Util.getJarLocation() + "data/services/csgo-updates").listFiles())
					{
						JSONObject json = new JSONObject(Util.getFileContents(file));

						try
						{
							Main.client.getGuildByID(Long.parseLong(file.getName().replace(".json", "")));
						}
						catch (NullPointerException e)
						{
							Main.log.warn("The bot has been removed from the guild belonging to the ID " + file.getName().replace(".json", "") + ", the CS:GO update service loop will move on to the next guild");
							continue;
						}

						if (json.getBoolean("enabled") && json.getBoolean("earlyWarnings") && Util.isEnabled(Main.client.getGuildByID(Long.parseLong(file.getName().replace(".json", "")))))
						{
							json.put("lastGuildName", Main.client.getGuildByID(Long.parseLong(file.getName().replace(".json", ""))).getName());
							FileUtils.writeStringToFile(file, json.toString(2), "UTF-8");
							Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), "SteamDB has spotted an update for CS:GO on the 741 app, this means **an update is definitely coming!** <https://steamdb.info/app/741/history/>");
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}
}
