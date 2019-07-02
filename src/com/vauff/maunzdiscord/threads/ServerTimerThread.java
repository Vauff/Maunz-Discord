package com.vauff.maunzdiscord.threads;

import com.github.koraktor.steamcondenser.steam.SteamPlayer;
import com.github.koraktor.steamcondenser.steam.servers.SourceServer;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import com.vauff.maunzdiscord.features.ServerTimer;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.http.client.ClientException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;

import java.io.File;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

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
				int serverNumber = 0;

				try
				{
					Main.client.getGuildById(Snowflake.of(file.getName())).block();
				}
				catch (ClientException e)
				{
					// bot is no longer in this guild, stop execution
					return;
				}

				parentloop:
				while (true)
				{
					JSONObject object;
					String objectName = "server" + serverNumber;

					try
					{
						object = json.getJSONObject(objectName);
					}
					catch (JSONException e)
					{
						FileUtils.writeStringToFile(new File(Util.getJarLocation() + "/data/services/server-tracking/" + file.getName() + "/serverInfo.json"), json.toString(2), "UTF-8");
						break;
					}

					serverNumber++;

					if (object.getBoolean("enabled"))
					{
						boolean channelExists = true;

						try
						{
							Main.client.getChannelById(Snowflake.of(object.getLong("serverTrackingChannelID"))).block();
						}
						catch (Exception e)
						{
							channelExists = false;
						}

						SourceServer server;
						int attempts = 0;

						while (true)
						{
							try
							{
								server = new SourceServer(InetAddress.getByName(object.getString("serverIP")), object.getInt("serverPort"));
								server.initialize();

								try
								{
									ServerTimer.serverPlayers.put(object.getString("serverIP") + ":" + object.getInt("serverPort"), server.getPlayers().keySet());
								}
								catch (NullPointerException e)
								{
									Set<String> keySet = new HashSet<>();

									for (SteamPlayer player : new ArrayList<>(server.getPlayers().values()))
									{
										keySet.add(player.getName());
									}

									ServerTimer.serverPlayers.put(object.getString("serverIP") + ":" + object.getInt("serverPort"), keySet);
								}

								break;
							}
							catch (Exception e)
							{
								attempts++;

								if (attempts >= 5 || object.getInt("downtimeTimer") >= 1)
								{
									Logger.log.warn("Failed to connect to the server " + object.getString("serverIP") + ":" + object.getInt("serverPort") + ", automatically retrying in 1 minute");
									object.put("downtimeTimer", object.getInt("downtimeTimer") + 1);

									if (object.getInt("downtimeTimer") == object.getInt("failedConnectionsThreshold") && channelExists)
									{
										Util.msg((MessageChannel) Main.client.getChannelById(Snowflake.of(object.getLong("serverTrackingChannelID"))).block(), "The server has gone offline");
									}

									if (object.getInt("downtimeTimer") >= 10080)
									{
										if (channelExists)
										{
											Util.msg((MessageChannel) Main.client.getChannelById(Snowflake.of(object.getLong("serverTrackingChannelID"))).block(), "The server has now been offline for a week and the server tracking service was automatically disabled, it can be re-enabled by a guild administrator using the ***services** command");
										}

										object.put("enabled", false);
									}

									continue parentloop;
								}
								else
								{
									Thread.sleep(1000);
								}
							}
						}

						if (object.getInt("downtimeTimer") >= object.getInt("failedConnectionsThreshold") && channelExists)
						{
							Util.msg((MessageChannel) Main.client.getChannelById(Snowflake.of(object.getLong("serverTrackingChannelID"))).block(), "The server has come back online");
						}

						String serverInfo = server.toString();
						long timestamp = 0;
						String map = serverInfo.split("mapName: ")[1].split("Players:")[0].replace("\n", "");
						String serverName = serverInfo.split("serverName: ")[1].split("  secure: ")[0].replace("\n", "");
						int currentPlayers = Integer.parseInt(serverInfo.split("numberOfPlayers: ")[1].split(" ")[0].replace("\n", ""));
						int maxPlayers = Integer.parseInt(serverInfo.split("maxPlayers: ")[1].split(" ")[0].replace("\n", ""));
						String url = "https://vauff.com/mapimgs/" + StringUtils.substring(map, 0, 31) + ".jpg";

						if (currentPlayers > maxPlayers)
						{
							currentPlayers = maxPlayers;
						}

						String players = currentPlayers + "/" + maxPlayers;

						if (!map.equals("") && !object.getString("lastMap").equalsIgnoreCase(map))
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

							final String finalUrl = url;
							final long finalTimestamp = timestamp;
							final URL constructedUrl = new URL(url);

							Consumer<EmbedCreateSpec> embed = spec ->
							{
								spec.setColor(Util.averageColorFromURL(constructedUrl, true));
								spec.setTimestamp(Instant.ofEpochMilli(finalTimestamp));
								spec.setThumbnail(finalUrl);
								spec.setDescription("Now Playing: **" + map.replace("_", "\\_") + "**\nPlayers Online: **" + players + "**\nQuick Join: **steam://connect/" + object.getString("serverIP") + ":" + object.getInt("serverPort") + "**");
							};

							if (channelExists)
							{
								Util.msg((MessageChannel) Main.client.getChannelById(Snowflake.of(object.getLong("serverTrackingChannelID"))).block(), embed);
							}

							for (File notificationFile : new File(Util.getJarLocation() + "data/services/server-tracking/" + file.getName()).listFiles())
							{
								if (!notificationFile.getName().equals("serverInfo.json"))
								{
									JSONObject notificationJson = new JSONObject(Util.getFileContents("data/services/server-tracking/" + file.getName() + "/" + notificationFile.getName()));
									Member member = null;

									try
									{
										member = Main.client.getMemberById(Snowflake.of(file.getName()), Snowflake.of(notificationFile.getName().replace(".json", ""))).block();
									}
									catch (ClientException e)
									{
										continue;
										// This means that a bad user ID was provided,
										// the given account doesn't exist anymore, or
										// this user is no longer a member of the guild
									}

									try
									{
										for (int i = 0; i < notificationJson.getJSONObject("notifications").getJSONArray(objectName).length(); i++)
										{
											String mapNotification = notificationJson.getJSONObject("notifications").getJSONArray(objectName).getString(i);

											if (mapNotification.equalsIgnoreCase(map))
											{
												Util.msg(member.getPrivateChannel().block(), embed.andThen(spec -> spec.setTitle(serverName)));
											}
										}
									}
									catch (JSONException e)
									{
										// This means that the user being processed
										// doesn't have any notifications set for the
										// given server, it can be safely ignored
									}
								}
							}

							boolean mapFound = false;

							for (int i = 0; i < object.getJSONArray("mapDatabase").length(); i++)
							{
								String dbMap = object.getJSONArray("mapDatabase").getJSONObject(i).getString("mapName");

								if (dbMap.equalsIgnoreCase(map))
								{
									mapFound = true;
									object.getJSONArray("mapDatabase").getJSONObject(i).put("lastPlayed", timestamp);
									break;
								}
							}

							if (!mapFound)
							{
								JSONObject mapObject = new JSONObject();
								mapObject.put("mapName", map);
								mapObject.put("firstPlayed", timestamp);
								mapObject.put("lastPlayed", timestamp);
								object.append("mapDatabase", mapObject);
							}
						}

						if (!map.equals(""))
						{
							object.put("lastMap", map);
						}

						if (!players.equals(""))
						{
							object.put("players", players);
						}

						if (timestamp != 0)
						{
							object.put("timestamp", timestamp);
						}

						if (!serverName.equals(""))
						{
							object.put("serverName", serverName);
						}

						json.put("lastGuildName", Main.client.getGuildById(Snowflake.of(Long.parseLong(file.getName()))).block().getName());
						object.put("downtimeTimer", 0);
						server.disconnect();
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
		finally
		{
			ServerTimer.trackingThreadRunning.put(file.getName(), false);
		}
	}
}
