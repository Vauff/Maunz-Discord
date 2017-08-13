package com.vauff.maunzdiscord.features;

import java.awt.Color;
import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import org.json.JSONObject;

import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import com.github.koraktor.steamcondenser.steam.servers.SourceServer;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class MapTimer
{
	public static HashMap<Long, SourceServer> serverList = new HashMap<Long, SourceServer>();

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
						JSONObject json = new JSONObject(Util.getFileContents("services/map-tracking/" + file.getName()));
						SourceServer server = new SourceServer(InetAddress.getByName(json.getString("serverIP")), json.getInt("serverPort"));
						server.initialize();
						serverList.put(Long.parseLong(file.getName().replace(".json", "")), server);
						String serverInfo = server.toString();
						long timestamp = 0;
						String map = serverInfo.split("mapName: ")[1].replace(System.lineSeparator(), "");
						int currentPlayers = Integer.parseInt(serverInfo.split("numberOfPlayers: ")[1].split(" ")[0].replace(System.lineSeparator(), ""));
						int maxPlayers = Integer.parseInt(serverInfo.split("maxPlayers: ")[1].split(" ")[0].replace(System.lineSeparator(), ""));

						if (currentPlayers > maxPlayers)
						{
							currentPlayers = maxPlayers;
						}

						String players = currentPlayers + "/" + maxPlayers;

						if (!map.equals("") && !json.getString("lastMap").equalsIgnoreCase(map))
						{
							timestamp = System.currentTimeMillis();

							EmbedObject embed = new EmbedBuilder().withColor(new Color(0, 154, 255)).withTimestamp(timestamp).withThumbnail("https://vauff.me/mapimgs/" + map + ".jpg").withDescription("Now Playing: **" + map.replace("_", "\\_") + "**\nPlayers Online: **" + players + "**\nQuick Join: **steam://connect/" + json.getString("serverIP") + ":" + json.getInt("serverPort") + "**").build();
							Util.msg(Main.client.getChannelByID(json.getLong("mapTrackingChannelID")), embed);

							for (int parentInt = 0; parentInt < json.getJSONArray("notifications").length(); parentInt++)
							{
								JSONObject object = json.getJSONArray("notifications").getJSONObject(parentInt);
								IUser user = null;

								try
								{
									user = Main.client.getUserByID(object.getLong("id"));
								}
								catch (NullPointerException e)
								{
									Main.log.error("", e);
									// This means that either a bad user ID was
									// provided by the notification file, or the
									// users account doesn't exist anymore
								}

								for (int childInt = 0; childInt < object.getJSONArray("notifications").length(); childInt++)
								{
									String mapNotification = object.getJSONArray("notifications").getString(childInt);

									if (mapNotification.equalsIgnoreCase(map))
									{
										Util.msg(Main.client.getOrCreatePMChannel(user), embed);
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

						FileUtils.writeStringToFile(new File(Util.getJarLocation() + "/services/map-tracking/" + file.getName()), json.toString(2), "UTF-8");
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