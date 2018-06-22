package com.vauff.maunzdiscord.threads;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.SteamPlayer;
import com.github.koraktor.steamcondenser.steam.servers.SourceServer;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.features.ServerTimer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.File;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class ServerTimerThread implements Runnable
{
	private File file;
	private Thread thread;
	private String name;

	public ServerTimerThread(File passedFile, String passedName)
	{
		name = passedName;
		file = passedFile;
	}

	public void start()
	{
		if (thread == null)
		{
			thread = new Thread(this, name);
			thread.start();
		}
	}

	public void run()
	{
		try
		{
			if (file.isDirectory())
			{
				JSONObject json = new JSONObject(Util.getFileContents("data/services/server-tracking/" + file.getName() + "/serverInfo.json"));

				if (json.getBoolean("enabled"))
				{
					try
					{
						Main.client.getGuildByID(Long.parseLong(file.getName())).getStringID();
					}
					catch (NullPointerException e)
					{
						Logger.log.warn("The bot has been removed from the guild belonging to the ID " + file.getName().replace(".json", "") + ", the server tracking loop will skip this guild");
						return;
					}

					if (Util.isEnabled(Main.client.getGuildByID(Long.parseLong(file.getName()))))
					{
						SourceServer server;
						int attempts = 0;

						while (true)
						{
							try
							{
								server = new SourceServer(InetAddress.getByName(json.getString("serverIP")), json.getInt("serverPort"));
								server.initialize();

								try
								{
									ServerTimer.serverPlayers.put(json.getString("serverIP") + ":" + json.getInt("serverPort"), server.getPlayers().keySet());
								}
								catch (NullPointerException e)
								{
									Set<String> keySet = new HashSet<>();

									for (SteamPlayer player : new ArrayList<>(server.getPlayers().values()))
									{
										keySet.add(player.getName());
									}

									ServerTimer.serverPlayers.put(json.getString("serverIP") + ":" + json.getInt("serverPort"), keySet);
								}

								break;
							}
							catch (NullPointerException | TimeoutException | SteamCondenserException e)
							{
								attempts++;

								if (attempts >= 5 || json.getInt("downtimeTimer") >= 1)
								{
									Logger.log.error("Failed to connect to the server " + json.getString("serverIP") + ":" + json.getInt("serverPort") + ", automatically retrying in 1 minute");
									json.put("downtimeTimer", json.getInt("downtimeTimer") + 1);

									if (json.getInt("downtimeTimer") == json.getInt("failedConnectionsThreshold"))
									{
										Util.msg(Main.client.getChannelByID(json.getLong("serverTrackingChannelID")), "The server has gone offline");
									}

									if (json.getInt("downtimeTimer") == 4320)
									{
										Util.msg(Main.client.getChannelByID(json.getLong("serverTrackingChannelID")), "The server has now been offline for over 72 hours and the map tracking service was automatically disabled, it can be re-enabled by a guild administrator using the ***services** command");
										json.put("enabled", false);
									}

									FileUtils.writeStringToFile(new File(Util.getJarLocation() + "/data/services/server-tracking/" + file.getName() + "/serverInfo.json"), json.toString(2), "UTF-8");
									return;
								}
							}
						}

						if (json.getInt("downtimeTimer") >= 3)
						{
							Util.msg(Main.client.getChannelByID(json.getLong("serverTrackingChannelID")), "The server has come back online");
						}

						String serverInfo = server.toString();
						long timestamp = 0;
						String map = serverInfo.split("mapName: ")[1].split("Players:")[0].replace("\n", "");
						String serverName = serverInfo.split("serverName: ")[1].split("  secure: ")[0].replace("\n", "");
						int currentPlayers = Integer.parseInt(serverInfo.split("numberOfPlayers: ")[1].split(" ")[0].replace("\n", ""));
						int maxPlayers = Integer.parseInt(serverInfo.split("maxPlayers: ")[1].split(" ")[0].replace("\n", ""));
						String url = "http://158.69.59.239/mapimgs/" + StringUtils.substring(map, 0, 31) + ".jpg";

						if (currentPlayers > maxPlayers)
						{
							currentPlayers = maxPlayers;
						}

						String players = currentPlayers + "/" + maxPlayers;

						if (!map.equals("") && !json.getString("lastMap").equalsIgnoreCase(map))
						{
							timestamp = System.currentTimeMillis();

							try
							{
								Jsoup.connect(url).get();
							}
							catch (HttpStatusException | ConnectException e)
							{
								url = "https://image.gametracker.com/images/maps/160x120/csgo/" + StringUtils.substring(map, 0, 31) + ".jpg";
							}
							catch (UnsupportedMimeTypeException e)
							{
								// This is to be expected normally because JSoup can't parse a URL serving only a static image
							}

							EmbedObject embed = new EmbedBuilder().withColor(Util.averageColorFromURL(new URL(url))).withTimestamp(timestamp).withThumbnail(url).withDescription("Now Playing: **" + map.replace("_", "\\_") + "**\nPlayers Online: **" + players + "**\nQuick Join: **steam://connect/" + json.getString("serverIP") + ":" + json.getInt("serverPort") + "**").build();
							EmbedObject pmEmbed = new EmbedBuilder().withColor(Util.averageColorFromURL(new URL(url))).withTimestamp(timestamp).withThumbnail(url).withTitle(serverName).withDescription("Now Playing: **" + map.replace("_", "\\_") + "**\nPlayers Online: **" + players + "**\nQuick Join: **steam://connect/" + json.getString("serverIP") + ":" + json.getInt("serverPort") + "**").build();

							Util.msg(Main.client.getChannelByID(json.getLong("serverTrackingChannelID")), embed);

							for (File notificationFile : new File(Util.getJarLocation() + "data/services/server-tracking/" + file.getName()).listFiles())
							{
								if (!notificationFile.getName().equals("serverInfo.json"))
								{
									JSONObject notificationJson = new JSONObject(Util.getFileContents("data/services/server-tracking/" + file.getName() + "/" + notificationFile.getName()));
									IUser user = null;

									try
									{
										user = Main.client.getUserByID(Long.parseLong(notificationFile.getName().replace(".json", "")));
									}
									catch (NullPointerException e)
									{
										Logger.log.error("", e);
										// This means that either a bad user ID was
										// provided by the notification file, or the
										// users account doesn't exist anymore
									}

									for (int i = 0; i < notificationJson.getJSONArray("notifications").length(); i++)
									{
										String mapNotification = notificationJson.getJSONArray("notifications").getString(i);

										if (mapNotification.equalsIgnoreCase(map))
										{
											try
											{
												Util.msg(Main.client.getOrCreatePMChannel(user), pmEmbed);
											}
											catch (NullPointerException e)
											{
												Logger.log.error("", e);
												// This means that the provided user has
												// left the guild, due to API limits bots
												// can't PM someone with no shared guilds
											}
										}
									}
								}
							}

							boolean mapFound = false;

							for (int i = 0; i < json.getJSONArray("mapDatabase").length(); i++)
							{
								String dbMap = json.getJSONArray("mapDatabase").getJSONObject(i).getString("mapName");

								if (dbMap.equalsIgnoreCase(map))
								{
									mapFound = true;
									json.getJSONArray("mapDatabase").getJSONObject(i).put("lastPlayed", timestamp);
									break;
								}
							}

							if (!mapFound)
							{
								JSONObject object = new JSONObject();
								object.put("mapName", map);
								object.put("firstPlayed", timestamp);
								object.put("lastPlayed", timestamp);
								json.append("mapDatabase", object);
							}
						}

						if (!map.equals(""))
						{
							json.put("lastMap", map);
						}

						if (!players.equals(""))
						{
							json.put("players", players);
						}

						if (timestamp != 0)
						{
							json.put("timestamp", timestamp);
						}

						if (!serverName.equals(""))
						{
							json.put("serverName", serverName);
						}

						json.put("lastGuildName", Main.client.getGuildByID(Long.parseLong(file.getName())).getName());
						json.put("downtimeTimer", 0);
						FileUtils.writeStringToFile(new File(Util.getJarLocation() + "/data/services/server-tracking/" + file.getName() + "/serverInfo.json"), json.toString(2), "UTF-8");
						server.disconnect();
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	}
}
