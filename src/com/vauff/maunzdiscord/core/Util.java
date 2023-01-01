package com.vauff.maunzdiscord.core;

import com.vauff.maunzdiscord.timers.MapImageTimer;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateMono;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.bson.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import static com.mongodb.client.model.Filters.eq;

/**
 * A class holding several static utility methods
 */
public class Util
{
	/**
	 * @return The path at which the running jar file is located
	 * @throws Exception
	 */
	public static String getJarLocation() throws Exception
	{
		String path = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

		if (path.endsWith(".jar"))
		{
			path = path.substring(0, path.lastIndexOf("/"));
		}

		if (!path.endsWith("/"))
		{
			path += "/";
		}

		return path;
	}

	/**
	 * Gets the contents of a file as a string
	 *
	 * @param arg The File object to read
	 * @return The content of the file, or an empty string if an exception has been caught
	 * @throws Exception
	 */
	public static String getFileContents(File arg) throws Exception
	{
		return FileUtils.readFileToString(arg, "UTF-8");
	}

	/**
	 * Checks if a user is a bot administrator
	 *
	 * @param user The user to check
	 * @return true if user is a bot administrator, false otherwise
	 * @throws Exception
	 */
	public static boolean hasPermission(User user)
	{
		for (int i = 0; i < Main.cfg.getOwners().length; i++)
		{
			if (user.getId().asLong() == Main.cfg.getOwners()[i])
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if a user is a bot or guild administrator
	 *
	 * @param user  The user to check
	 * @param guild The guild to check for permissions in
	 * @return true if the user is a bot or guild administrator, false otherwise
	 * @throws Exception
	 */
	public static boolean hasPermission(User user, Guild guild)
	{
		if (hasPermission(user))
			return true;

		// Always PM, i think?
		if (Objects.isNull(guild))
			return false;

		return (user.asMember(guild.getId()).block().getBasePermissions().block().contains(Permission.ADMINISTRATOR) || user.asMember(guild.getId()).block().getBasePermissions().block().contains(Permission.MANAGE_GUILD));
	}

	/**
	 * Edits a reply to an interaction
	 *
	 * @param event      The event that should have its reply edited
	 * @param message    String content of the reply
	 * @param embeds     Embeds the reply should contain
	 * @param components LayoutComponents the reply should contain
	 * @return The message object
	 */
	public static void editReply(DeferrableInteractionEvent event, String message, Iterable<EmbedCreateSpec> embeds, Iterable<LayoutComponent> components)
	{
		event.editReply(message).withEmbedsOrNull(embeds).withComponentsOrNull(components).block();
	}

	/**
	 * Edits a reply to an interaction
	 *
	 * @param event   The event that should have its reply edited
	 * @param message String content of the reply
	 * @param embeds  Embeds the reply should contain
	 * @return The message object
	 */
	public static void editReply(DeferrableInteractionEvent event, String message, EmbedCreateSpec... embeds)
	{
		editReply(event, message, Arrays.asList(embeds), null);
	}

	/**
	 * Edits a reply to an interaction
	 *
	 * @param event      The event that should have its reply edited
	 * @param message    String content of the reply
	 * @param components LayoutComponents the reply should contain
	 * @return The message object
	 */
	public static void editReply(DeferrableInteractionEvent event, String message, LayoutComponent... components)
	{
		editReply(event, message, null, Arrays.asList(components));
	}

	/**
	 * Edits a reply to an interaction
	 *
	 * @param event   The event that should have its reply edited
	 * @param message String content of the reply
	 * @return The message object
	 */
	public static void editReply(DeferrableInteractionEvent event, String message)
	{
		editReply(event, message, null, null);
	}

	/**
	 * Sends a message to a channel
	 *
	 * @param channel              The channel
	 * @param permissionExHandling Whether to handle missing permissions exceptions (403)
	 * @param message              The string message (null for none)
	 * @param embed                The embed (null for none)
	 * @return The message object
	 */
	public static Message msg(MessageChannel channel, boolean permissionExHandling, String message, EmbedCreateSpec embed)
	{
		try
		{
			MessageCreateMono msg;

			if (!Objects.isNull(message))
				msg = channel.createMessage(message);
			else
				msg = channel.createMessage();

			if (!Objects.isNull(embed))
				msg = msg.withEmbeds(embed);

			return msg.withAllowedMentions(AllowedMentions.suppressAll()).block();
		}
		catch (ClientException e)
		{
			if (permissionExHandling && e.getStatus().code() == 403)
			{
				if (!(channel instanceof PrivateChannel))
				{
					Logger.log.warn("Missing permissions to send message to channel #" + ((GuildChannel) channel).getName() + " (" + channel.getId().asString() + ") in guild " + ((GuildChannel) channel).getGuild().block().getName() + " (" + ((GuildChannel) channel).getGuild().block().getId().asString() + ")");
				}

				return null;
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * Sends a message to a channel
	 *
	 * @param channel              The channel
	 * @param permissionExHandling Whether to handle missing permissions exceptions (403)
	 * @param message              The string message
	 * @return The message object
	 */
	public static Message msg(MessageChannel channel, boolean permissionExHandling, String message)
	{
		return msg(channel, permissionExHandling, message, null);
	}

	/**
	 * Sends an embed to a channel
	 *
	 * @param channel              The channel
	 * @param permissionExHandling Whether to handle missing permissions exceptions (403)
	 * @param embed                The embed
	 * @return The message object
	 */
	public static Message msg(MessageChannel channel, boolean permissionExHandling, EmbedCreateSpec embed)
	{
		return msg(channel, permissionExHandling, null, embed);
	}

	/**
	 * Gets the average color from the picture found at a URL
	 *
	 * @param url              The URL leading to the picture
	 * @param handleExceptions Whether to return (0, 154, 255) when an exception happens
	 * @return The average color of the picture, null or (0, 154, 255) on error
	 */
	public static Color averageColorFromURL(String url, boolean handleExceptions) throws Exception
	{
		BufferedImage image;

		try
		{
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", Main.cfg.getUserAgent());
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			image = ImageIO.read(connection.getInputStream());

			final int pixels = image.getWidth() * image.getHeight();
			int red = 0;
			int green = 0;
			int blue = 0;

			for (int x = 0; x < image.getWidth(); x++)
			{
				for (int y = 0; y < image.getHeight(); y++)
				{
					Color pixel = Color.of(image.getRGB(x, y));

					red += pixel.getRed();
					green += pixel.getGreen();
					blue += pixel.getBlue();
				}
			}

			return Color.of(red / pixels, green / pixels, blue / pixels);
		}
		catch (Exception e)
		{
			if (handleExceptions)
			{
				return Color.of(0, 154, 255);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * Checks if a given channel has multiple servers being tracked in it
	 *
	 * @param guildID   The guild to check inside of
	 * @param channelID The channel to check
	 * @return True if channel tracking multiple servers, false otherwise
	 */
	public static boolean isMultiTrackingChannel(long guildID, long channelID)
	{
		int matches = 0;

		for (Document doc : Main.mongoDatabase.getCollection("services").find(eq("guildID", guildID)))
		{
			if (!doc.getBoolean("enabled"))
				continue;

			if (doc.getLong("channelID") == channelID)
				matches++;
		}

		return matches >= 2;
	}

	/**
	 * Finds the best map image URL for a map, if possible
	 * Can be a vauff.com/mapimgs or images.gametracker.com image
	 *
	 * @param map   The map name
	 * @param appId App ID of the game
	 * @return Image URL for the map, or "" on failure
	 */
	public static String getMapImageURL(String map, int appId)
	{
		// Force lower case since GameTracker does, and for an accurate levenshtein distance
		String mapLower = map.toLowerCase();

		if (MapImageTimer.mapImageLookupCache.containsKey(appId))
		{
			HashMap<String, String> gameCache = MapImageTimer.mapImageLookupCache.get(appId);

			if (gameCache.containsKey(mapLower))
				return gameCache.get(mapLower);
		}

		String url = getVauffMapImageURL(mapLower, appId);
		String gtName = appIdToGameTrackerName(appId);

		// If we didn't find a high quality map image at vauff.com, try to fall back to GameTracker
		if (url.equals("") && !gtName.equals(""))
			url = "https://image.gametracker.com/images/maps/160x120/" + gtName + "/" + mapLower.replace(" ", "%20") + ".jpg";

		if (MapImageTimer.mapImageLookupCache.containsKey(appId))
			MapImageTimer.mapImageLookupCache.get(appId).put(mapLower, url);

		return url;
	}

	/**
	 * Finds an image URL for a map from vauff.com/mapimgs, if possible
	 *
	 * @param map   The map name
	 * @param appId App ID of the game
	 * @return Image URL for the map, or "" on failure
	 */
	private static String getVauffMapImageURL(String map, int appId)
	{
		// As we accomodate for mapCharacterLimit, all image map names at vauff.com should never exceed 31 characters
		String trimmedMap = StringUtils.substring(map, 0, 31);
		String bestMatch = "";
		int bmLevenshteinDist = 999;

		if (!MapImageTimer.mapImages.containsKey(appId))
			return "";

		for (String arrayMap : MapImageTimer.mapImages.get(appId))
		{
			// Keep both parameters lower case for an accurate levenshtein distance
			String arrayMapLower = arrayMap.toLowerCase();
			int distance = new LevenshteinDistance().apply(trimmedMap, arrayMapLower);

			if (distance < bmLevenshteinDist && trimmedMap.startsWith(arrayMapLower))
			{
				bestMatch = arrayMap;
				bmLevenshteinDist = distance;
			}
		}

		if (bestMatch.equals(""))
			return "";
		else
			return "https://vauff.com/mapimgs/" + appId + "/" + bestMatch.replace(" ", "%20") + ".jpg";
	}

	/**
	 * Matches an app ID to the game name used by GameTracker
	 * Currently excluded: GT Games not on Steam
	 *
	 * @param appId App ID of the game
	 * @return GameTracker's directory name for the given game
	 */
	private static String appIdToGameTrackerName(int appId)
	{
		return switch (appId)
		{
			case 10 -> "cs";
			case 20 -> "tfc";
			case 30 -> "dod";
			case 70 -> "hl";
			case 80 -> "czero";
			case 240 -> "css";
			case 300 -> "dods";
			case 320 -> "hl2dm";
			case 440 -> "tf2";
			case 500 -> "l4d";
			case 550 -> "left4dead2";
			case 570 -> "dota2";
			case 630 -> "alienswarm";
			case 730 -> "csgo";
			case 1200 -> "ror";
			case 1250 -> "killingfloor";
			case 1280 -> "rordh";
			case 2200 -> "q3";
			case 2210 -> "q4";
			case 2310 -> "qw";
			case 2320 -> "q2";
			case 2620 -> "cod";
			case 2630 -> "cod2";
			case 2640 -> "uo";
			case 4000 -> "garrysmod";
			case 4920 -> "ns2";
			case 6020 -> "swjk";
			case 6060 -> "swbf2";
			case 7940 -> "cod4";
			case 9010 -> "wolf";
			case 9050 -> "doom3";
			case 9460 -> "ffow";
			case 10000 -> "etqw";
			case 10090 -> "codww";
			case 13140 -> "aa3";
			case 13210 -> "ut3";
			case 13230 -> "ut2k4";
			case 13240 -> "ut";
			case 17300 -> "crysis";
			case 17330 -> "warhead";
			case 17500 -> "hl2zp";
			case 17700 -> "ins";
			case 21090 -> "fear";
			case 22350 -> "brink";
			case 24960 -> "bc2";
			case 33900 -> "arma2";
			case 35450 -> "ro2";
			case 42700 -> "blackops";
			case 47790 -> "moh";
			case 55100 -> "homefront";
			case 63200 -> "mnc";
			case 63380 -> "sniperelite2";
			case 65780 -> "arma";
			case 96300 -> "ravaged";
			case 107410 -> "arma3";
			case 108800 -> "crysis2";
			case 115300 -> "mw3";
			case 203290 -> "aapg";
			case 211820 -> "starbound";
			case 214630 -> "blackopsmac";
			case 221100 -> "dayz";
			case 222880 -> "insurgency2014";
			case 224580 -> "dayzmod";
			case 232090 -> "kf2";
			case 238430 -> "contagion";
			case 244850 -> "spaceengi";
			case 251570 -> "7daystodie";
			case 252490 -> "rust";
			case 253530 -> "ff";
			case 259080 -> "jc2";
			case 282440 -> "ql";
			case 290080 -> "lifyo";
			case 311210 -> "codbo3";
			case 346110 -> "arkse";
			case 393420 -> "hurtworld";
			case 440900 -> "conan";
			case 489940 -> "battalion1944";
			case 529180 -> "dnl";
			case 581320 -> "ins_sandstorm";
			case 659280 -> "urbanterror";
			case 1238820 -> "bf3";
			case 1238860 -> "bf4";
			case 1238880 -> "bfhl";
			case 1873030 -> "et";
			default -> "";
		};
	}

	/**
	 * Handles user feedback & logging for exceptions thrown from command/button processing
	 *
	 * @param e     The exception to handle
	 * @param event The event the exception was thrown from
	 */
	public static void handleException(Exception e, DeferrableInteractionEvent event)
	{
		Random rnd = new Random();
		int code = 100000000 + rnd.nextInt(900000000);

		editReply(event, ":exclamation:  |  **An error has occured!**" + System.lineSeparator() + System.lineSeparator() + "If this was an unexpected error, please report it to Vauff in the #bugreports channel at http://discord.gg/MDx3sMz with the error code " + code);
		Logger.log.error(code, e);
	}
}
