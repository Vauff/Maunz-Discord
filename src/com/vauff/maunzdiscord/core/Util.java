package com.vauff.maunzdiscord.core;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.vdurmont.emoji.EmojiManager;

import org.json.JSONObject;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.MissingPermissionsException;

/**
 * A class holding several static utility methods
 */
public class Util
{
	/**
	 * true if the bot is in development mode, false otherwise.
	 * Used to determine the Discord API token and handle differences in the live and dev version
	 */
	public static boolean devMode;
	/**
	 * The Discord API token of the bot, gets set in {@link Main#main(String[])}
	 */
	public static String token;

	public static Connection sqlCon;

	/**
	 * @return The path at which the running jar file is located
	 */
	public static String getJarLocation()
	{
		try
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
		catch (URISyntaxException e)
		{
			Main.log.error("", e);
			return null;
		}
	}

	/**
	 * Gets the contents of a file as a string
	 *
	 * @param arg The path of the file, relative to {@link Util#getJarLocation()}
	 * @return The content of the file
	 * @throws IOException If {@link FileUtils#readFileToString(File)} throws an IOException
	 */
	public static String getFileContents(String arg) throws IOException
	{
		File file = new File(getJarLocation() + arg);

		return FileUtils.readFileToString(file, "UTF-8");
	}

	/**
	 * Gets the contents of a file as a string
	 *
	 * @param arg The path of the file
	 * @return The content of the file, or an empty string if an exception has been caught
	 */
	public static String getFileContents(File arg)
	{
		try
		{
			return FileUtils.readFileToString(arg, "UTF-8");
		}
		catch (Exception e)
		{
			Main.log.error("", e);
			return "";
		}
	}

	/**
	 * Formats the current time into a string
	 *
	 * @return The current time as a String in the format
	 * EEEE MMMM d'st/nd/rd/th', yyyy, h:mm a z
	 * as defined in {@link SimpleDateFormat}
	 */
	public static String getTime()
	{
		return getTime(System.currentTimeMillis());
	}

	/**
	 * Formats the given time into a string
	 *
	 * @param time The time in milliseconds to format as a string
	 * @return The given time as a String in the format
	 * EEEE MMMM d'st/nd/rd/th', yyyy, h:mm a z
	 * as defined in {@link SimpleDateFormat}
	 */
	public static String getTime(long time)
	{
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE MMMM d'" + getOrdinal(date.getDate()) + "', yyyy, h:mm a z");

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}

	/**
	 * Formats the uptime of the bot as a string
	 *
	 * @return The uptime of the bot formatted as the 2 top most values
	 */
	public static String getUptime()
	{
		MainListener.uptime.split();

		String uptimeRaw = MainListener.uptime.toSplitString().split("\\.")[0];
		String secondText = "seconds";
		String minuteText = "minutes";
		String hourText = "hours";
		String dayText = "days";
		int seconds = Integer.parseInt(uptimeRaw.split(":")[2]);
		int minutes = Integer.parseInt(uptimeRaw.split(":")[1]);
		int hours = Integer.parseInt(uptimeRaw.split(":")[0]) % 24;
		int days = (Integer.parseInt(uptimeRaw.split(":")[0]) / 24);

		if (seconds == 1)
		{
			secondText = "second";
		}

		if (minutes == 1)
		{
			minuteText = "minute";
		}

		if (hours == 1)
		{
			hourText = "hour";
		}

		if (days == 1)
		{
			dayText = "day";
		}

		if (days >= 1)
		{
			return days + " " + dayText + ", " + hours + " " + hourText;
		}

		else if (hours >= 1)
		{
			return hours + " " + hourText + ", " + minutes + " " + minuteText;
		}

		else if (minutes >= 1)
		{
			return minutes + " " + minuteText + ", " + seconds + " " + secondText;
		}
		else
		{
			return seconds + " " + secondText;
		}
	}

	/**
	 * Concatenates a string array from a given start index and leavs out the part after the last space
	 *
	 * @param args       The array to concatenate
	 * @param startIndex The index to start concatenating the array
	 * @return The concatenated array with the part after the last space left out
	 */
	public static String addArgs(String[] args, int startIndex)
	{
		String s = "";

		for (int i = startIndex; i < args.length; i++)
		{
			s += args[i] + " ";
		}

		return s.substring(0, s.lastIndexOf(" "));
	}

	/**
	 * Gets the ordinal of a number
	 *
	 * @param n The number
	 * @return st for 1, 21, 31 etc; nd for 2, 22, 32, etc; rd for 3, 23, 33, etc; th for everything else
	 */
	public static String getOrdinal(int n)
	{
		if (n >= 11 && n <= 13)
		{
			return "th";
		}
		else
		{
			switch (n % 10)
			{
				case 1:
					return "st";
				case 2:
					return "nd";
				case 3:
					return "rd";
				default:
					return "th";
			}
		}
	}

	/**
	 * Connects to the Chat-Quotes database
	 */
	public static void sqlConnect() throws Exception
	{
		try
		{
			JSONObject json = new JSONObject(Util.getFileContents("config.json"));

			sqlCon = DriverManager.getConnection("jdbc:mysql://" + json.getJSONObject("database").getString("hostname") + "/ircquotes?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false", json.getJSONObject("database").getString("username"), json.getJSONObject("database").getString("password"));
		}
		catch (SQLException e)
		{
			Main.log.error(e.getMessage(), e);
		}
	}

	/**
	 * Checks if the client ID of a user is equal to the client ID of the botOwner supplied in config.json
	 *
	 * @param user The user to check
	 * @return true if the client IDs match and the given user is the botOwner supplied in config.json, false otherwise
	 */
	public static boolean hasPermission(IUser user)
	{
		JSONObject json = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "config.json")));

		return user.getLongID() == json.getLong("botOwnerID");
	}

	/**
	 * Checks if the client ID of a user is equal to the client ID of the botOwner supplied in config.json or the user is administrator in the supplied guild
	 *
	 * @param user  The user to check
	 * @param guild The guild to check for permissions in
	 * @return true if the client IDs match and the given user is the botOwner supplied in config.json or the user is a guild administrator, false otherwise
	 */
	public static boolean hasPermission(IUser user, IGuild guild)
	{
		JSONObject json = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "config.json")));

		return user.getLongID() == json.getLong("botOwnerID") ? true : (user.getPermissionsForGuild(guild).contains(Permissions.ADMINISTRATOR) || user.getPermissionsForGuild(guild).contains(Permissions.MANAGE_SERVER) ? true : false);
	}

	/**
	 * Sends a message to a channel
	 *
	 * @param channel The channel
	 * @param author  The author
	 * @param message The message
	 */
	public static void msg(IChannel channel, IUser author, String message)
	{
		try
		{
			channel.sendMessage(message);
		}
		catch (MissingPermissionsException e)
		{
			if (e.getMessage().split("Missing permissions: ")[1].equalsIgnoreCase("SEND_MESSAGES!"))
			{
				msg(author.getOrCreatePMChannel(), ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command in " + channel.mention() + " because it's lacking the **SEND_MESSAGES** permission." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
			}
			else
			{
				Main.log.error(e);
			}
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}

	/**
	 * Sends a message to a channel
	 *
	 * @param channel The channel
	 * @param message The message
	 */
	public static void msg(IChannel channel, String message)
	{
		try
		{
			channel.sendMessage(message);
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}

	/**
	 * Sends an embed to a channel
	 *
	 * @param channel The channel
	 * @param author  The author
	 * @param message The embed
	 */
	public static void msg(IChannel channel, IUser author, EmbedObject message)
	{
		try
		{
			channel.sendMessage("", message, false);
		}
		catch (MissingPermissionsException e)
		{
			if (e.getMessage().split("Missing permissions: ")[1].equalsIgnoreCase("EMBED_LINKS!"))
			{
				msg(channel, ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command because it's lacking the **EMBED_LINKS** permission." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
			}

			else
			{
				Main.log.error(e);
			}
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}

	/**
	 * Sends an embed to a channel
	 *
	 * @param channel The channel
	 * @param message The embed
	 */
	public static void msg(IChannel channel, EmbedObject message)
	{
		try
		{
			channel.sendMessage("", message, false);
		}
		catch (MissingPermissionsException e)
		{
			if (e.getMessage().split("Missing permissions: ")[1].equalsIgnoreCase("EMBED_LINKS!"))
			{
				msg(channel, ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to send a message because it's lacking the **EMBED_LINKS** permission." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set.");
			}
			else
			{
				Main.log.error(e);
			}
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}

	/**
	 * Gets the average color from the picture found at a URL
	 *
	 * @param url The URL leading to the picture
	 * @return The average color of the picture.
	 * If the URL does not contain a picture an RGB color value of 0, 154, 255 will be returned
	 */
	public static Color averageColorFromURL(URL url)
	{
		BufferedImage image = null;

		try
		{
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.91 Safari/537.36");
			image = ImageIO.read(connection.getInputStream());

			final int pixels = image.getWidth() * image.getHeight();
			int red = 0;
			int green = 0;
			int blue = 0;

			for (int x = 0; x < image.getWidth(); x++)
			{
				for (int y = 0; y < image.getHeight(); y++)
				{
					Color pixel = new Color(image.getRGB(x, y));

					red += pixel.getRed();
					green += pixel.getGreen();
					blue += pixel.getBlue();
				}
			}

			return new Color(red / pixels, green / pixels, blue / pixels);
		}
		catch (Exception e)
		{
			return new Color(0, 154, 255);
		}
	}

	/**
	 * Adds keycap emojis, increasing by value, starting at one and ending at nine. Used for menu selection
	 *
	 * @param m           The message to add the emojis to
	 * @param cancellable Whether an x emoji should be added at the end or not
	 * @param i           The amount of emojis to add, starting by one. If i is 5, all emojis from :one: to :five: will be added.
	 */
	public static void addNumberedReactions(IMessage m, boolean cancellable, int i)
	{
		try
		{
			String[] reactions = {
					"one",
					"two",
					"three",
					"four",
					"five",
					"six",
					"seven",
					"eight",
					"nine"
			};

			for (int j = 0; j < i; j++)
			{
				m.addReaction(EmojiManager.getForAlias(":" + reactions[j] + ":"));
				Thread.sleep(250);
			}

			if (cancellable)
			{
				m.addReaction(EmojiManager.getForAlias(":x:"));
			}
		}
		catch (MissingPermissionsException e)
		{
			if (e.getMessage().split("Missing permissions: ")[1].equalsIgnoreCase("ADD_REACTIONS!"))
			{
				msg(m.getChannel(), ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to add one or more reactions because it's lacking the **ADD_REACTIONS** permission." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
			}
			else
			{
				Main.log.error(e);
			}
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}

	/**
	 * Checks whether the bot is enabled for a specified guild
	 *
	 * @param guild The guild for which to check if the bot is enabled
	 * @return true if the bot is enabled for the guild, false otherwise
	 */
	public static boolean isEnabled(IGuild guild)
	{
		JSONObject botJson = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "config.json")));
		JSONObject guildJson = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "data/guilds/" + guild.getStringID() + ".json")));

		return botJson.getBoolean("enabled") && guildJson.getBoolean("enabled");
	}
}
