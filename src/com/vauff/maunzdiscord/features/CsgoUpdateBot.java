package com.vauff.maunzdiscord.features;

import java.io.File;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

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
			if (sender.equals(listeningNick) && Util.isEnabled)
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

					if (html.contains("branches/public/buildid"))
					{
						String msg = "SteamDB has spotted a public branch update for CS:GO on the 730 app, this means **an update was pushed to the Steam client!** <https://steamdb.info/app/730/history/>";

						Main.log.info("Found a CS:GO 730 update with changelog number " + consistentLastChangelistNumber);

						for (File file : new File(Util.getJarLocation() + "services/csgo-updates").listFiles())
						{
							JSONObject json = new JSONObject(Util.getFileContents(file));
							Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), msg);
						}
					}
					else if (html.contains("branches/dpr/buildid"))
					{
						String msg = "SteamDB has spotted a DPR branch update for CS:GO on the 730 app, this means **an update is probably coming.** <https://steamdb.info/app/730/history/>";

						Main.log.info("Found a CS:GO 730 update with changelog number " + consistentLastChangelistNumber);

						for (File file : new File(Util.getJarLocation() + "services/csgo-updates").listFiles())
						{
							JSONObject json = new JSONObject(Util.getFileContents(file));
							Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), msg);
						}
					}
					else if (html.replaceAll("\\d", "").contains("branches/./buildid") || html.replaceAll("\\d", "").contains("branches/../buildid") || html.replaceAll("\\d", "").contains("branches/.../buildid"))
					{
						String msg = "SteamDB has spotted a version branch update for CS:GO on the 730 app, this means **an update might be coming.** <https://steamdb.info/app/730/history/>";

						Main.log.info("Found a CS:GO 730 update with changelog number " + consistentLastChangelistNumber);

						for (File file : new File(Util.getJarLocation() + "services/csgo-updates").listFiles())
						{
							JSONObject json = new JSONObject(Util.getFileContents(file));
							Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), msg);
						}
					}
					else if (html.replaceAll("\\d", "").contains("branches/.-rc/buildid") || html.replaceAll("\\d", "").contains("branches/..-rc/buildid") || html.replaceAll("\\d", "").contains("branches/...-rc/buildid"))
					{
						if (!htmlRaw.contains("octicon octicon-diff-removed"))
						{
							String msg = "SteamDB has spotted a beta branch update for CS:GO on the 730 app, this means **a beta update was pushed to the Steam client!** <https://steamdb.info/app/730/history/>";

							Main.log.info("Found a CS:GO 730 update with changelog number " + consistentLastChangelistNumber);

							for (File file : new File(Util.getJarLocation() + "services/csgo-updates").listFiles())
							{
								JSONObject json = new JSONObject(Util.getFileContents(file));
								Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), msg);
							}
						}
						else
						{
							String msg = "SteamDB has spotted a version branch update for CS:GO on the 730 app, this means **an update might be coming.** <https://steamdb.info/app/730/history/>";

							Main.log.info("Found a CS:GO 730 update with changelog number " + consistentLastChangelistNumber);

							for (File file : new File(Util.getJarLocation() + "services/csgo-updates").listFiles())
							{
								JSONObject json = new JSONObject(Util.getFileContents(file));
								Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), msg);
							}
						}
					}
					else
					{
						String msg = "SteamDB has spotted a non-important update for CS:GO on the 730 app, this **most likely doesn't mean anything.** <https://steamdb.info/app/730/history/>";

						Main.log.info("Found a CS:GO 730 update with changelog number " + consistentLastChangelistNumber);

						for (File file : new File(Util.getJarLocation() + "services/csgo-updates").listFiles())
						{
							JSONObject json = new JSONObject(Util.getFileContents(file));

							if (json.getBoolean("nonImportantUpdates"))
							{
								Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), msg);
							}
						}
					}
				}

				if (Colors.removeFormattingAndColors(message).contains("App: 741 - SteamDB Unknown App 741 (Counter-Strike Global Offensive - Valve Dedicated Server) (needs token)"))
				{
					String msg = "SteamDB has spotted an update for CS:GO on the 741 app, this means **an update is definitely coming!** <https://steamdb.info/app/741/history/>";

					for (File file : new File(Util.getJarLocation() + "services/csgo-updates").listFiles())
					{
						JSONObject json = new JSONObject(Util.getFileContents(file));
						Util.msg(Main.client.getChannelByID(json.getLong("updateNotificationChannelID")), msg);
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
