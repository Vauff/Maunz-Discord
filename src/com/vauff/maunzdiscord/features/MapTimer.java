package com.vauff.maunzdiscord.features;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;

import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.SteamPlayer;
import com.github.koraktor.steamcondenser.steam.servers.SourceServer;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

/**
 * A timer to send notifications of maps currently being played on given servers
 */
public class MapTimer
{
	/**
	 * Holds extended information about servers (for instance online players)
	 */
	public static HashMap<Long, Set<String>> serverPlayers = new HashMap<Long, Set<String>>();

	/**
	 * Checks the servers in {@link Util#getJarLocation()}/services/map-tracking for new maps being played and sends them to a channel
	 * as well as notifying users that set up a notification for that map
	 */
	public static Runnable timer = new Runnable()
	{
		public void run()
		{
			try
			{
				if (Util.isEnabled)
				{
					for (File file : new File(Util.getJarLocation() + "services/map-tracking").listFiles())
					{
						if (file.isDirectory())
						{
							JSONObject json = new JSONObject(Util.getFileContents("services/map-tracking/" + file.getName() + "/serverInfo.json"));

							if (json.getBoolean("enabled"))
							{
								SourceServer server = new SourceServer(InetAddress.getByName(json.getString("serverIP")), json.getInt("serverPort"));

								try
								{
									server.initialize();

									try
									{
										serverPlayers.put(Long.parseLong(file.getName()), server.getPlayers().keySet());
									}
									catch (NullPointerException e)
									{
										HashMap<String, SteamPlayer> players = server.getPlayers();
										Set<String> keySet = new HashSet<String>();

										for (SteamPlayer player : new ArrayList<SteamPlayer>(players.values()))
										{
											keySet.add(player.getName());
										}

										serverPlayers.put(Long.parseLong(file.getName()), keySet);
									}
								}
								catch (NullPointerException | TimeoutException | SteamCondenserException e)
								{
									Main.log.error("Failed to connect to the server " + json.getString("serverIP") + ":" + json.getInt("serverPort") + ", automatically retrying in 1 minute");
									json.put("downtimeTimer", json.getInt("downtimeTimer") + 1);

									if (json.getInt("downtimeTimer") == 3)
									{
										Util.msg(Main.client.getChannelByID(json.getLong("mapTrackingChannelID")), "The server has gone offline");
									}

									if (json.getInt("downtimeTimer") == 4320)
									{
										Util.msg(Main.client.getChannelByID(json.getLong("mapTrackingChannelID")), "The server has now been offline for over 72 hours and the map tracking service was automatically disabled, it can be re-enabled by a guild administrator using the ***services** command");
										json.put("enabled", false);
									}

									FileUtils.writeStringToFile(new File(Util.getJarLocation() + "/services/map-tracking/" + file.getName() + "/serverInfo.json"), json.toString(2), "UTF-8");
									continue;
								}

								String serverInfo = server.toString();
								long timestamp = 0;
								String map = serverInfo.split("mapName: ")[1].split("Players:")[0].replace(System.lineSeparator(), "");
								int currentPlayers = Integer.parseInt(serverInfo.split("numberOfPlayers: ")[1].split(" ")[0].replace(System.lineSeparator(), ""));
								int maxPlayers = Integer.parseInt(serverInfo.split("maxPlayers: ")[1].split(" ")[0].replace(System.lineSeparator(), ""));
								String url = "http://158.69.59.239/mapimgs/" + map + ".jpg";

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
									catch (HttpStatusException e)
									{
										url = "https://image.gametracker.com/images/maps/160x120/csgo/" + map + ".jpg";
									}
									catch (Exception e)
									{
									}

									EmbedObject embed = new EmbedBuilder().withColor(Util.averageColorFromURL(new URL(url))).withTimestamp(timestamp).withThumbnail(url).withDescription("Now Playing: **" + map.replace("_", "\\_") + "**\nPlayers Online: **" + players + "**\nQuick Join: **steam://connect/" + json.getString("serverIP") + ":" + json.getInt("serverPort") + "**").build();
									Util.msg(Main.client.getChannelByID(json.getLong("mapTrackingChannelID")), embed);

									for (File notificationFile : new File(Util.getJarLocation() + "services/map-tracking/" + file.getName()).listFiles())
									{
										if (!notificationFile.getName().equals("serverInfo.json"))
										{
											JSONObject notificationJson = new JSONObject(Util.getFileContents("services/map-tracking/" + file.getName() + "/" + notificationFile.getName()));
											IUser user = null;

											try
											{
												user = Main.client.getUserByID(Long.parseLong(notificationFile.getName().replace(".json", "")));
											}
											catch (NullPointerException e)
											{
												Main.log.error("", e);
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
														Util.msg(Main.client.getOrCreatePMChannel(user), embed);
													}
													catch (NullPointerException e)
													{
														Main.log.error("", e);
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
										String dbMap = json.getJSONArray("mapDatabase").getString(i);

										if (dbMap.toString().equals(map))
										{
											mapFound = true;
										}
									}

									if (!mapFound)
									{
										json.append("mapDatabase", map);
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

								json.put("lastGuildName", Main.client.getGuildByID(Long.parseLong(file.getName())).getName());
								json.put("downtimeTimer", 0);
								FileUtils.writeStringToFile(new File(Util.getJarLocation() + "/services/map-tracking/" + file.getName() + "/serverInfo.json"), json.toString(2), "UTF-8");
								server.disconnect();
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
	};
}