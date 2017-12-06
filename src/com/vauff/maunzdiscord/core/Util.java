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

/**
 * A class holding several static utility methods
 */
public class Util
{
	/**
	 * true if the bot is enabled, false otherwise
	 */
	public static boolean isEnabled = true;
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
	 * @return The path at which the running jar file is located.
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
	 * @return The content of the file
	 * @throws IOException If {@link FileUtils#readFileToString(File)} throws an IOException
	 */
	public static String getFileContents(File arg) throws IOException
	{
		return FileUtils.readFileToString(arg, "UTF-8");
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
	 * @param The time in milliseconds to format as a string
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
	 * @return The uptime of the bot formatted as
	 * days:hours:minutes:seconds
	 * with a leading zero if one of the time values is a single digit
	 */
	public static String getUptime()
	{
		MainListener.uptime.split();

		String[] uptimeraw = MainListener.uptime.toSplitString().split("\\.");
		int hours = Integer.parseInt(uptimeraw[0].split(":")[0]);
		int days = (hours / 24) >> 0;

		hours = hours % 24;

		return days + ":" + (hours < 10 ? "0" + hours : hours) + ":" + uptimeraw[0].split(":")[1] + ":" + uptimeraw[0].split(":")[2];
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

	public static void sqlConnect() throws Exception
	{
		try
		{
			JSONObject json = new JSONObject(Util.getFileContents("config.json"));

			sqlCon = DriverManager.getConnection("jdbc:mysql://158.69.59.239:3306/ircquotes?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false", "Vauff", json.getString("databasePassword"));
		}
		catch (SQLException e)
		{
			Main.log.error(e.getMessage(), e);
		}
	}

	/**
	 * Checks if the client ID of a user is equal to the client ID of Vauff
	 *
	 * @param user The user to check
	 * @return true if the client IDs match and the given user is Vauff, false otherwise
	 */
	public static boolean hasPermission(IUser user)
	{
		return user.getLongID() == 129448521861431296L;
	}

	/**
	 * Checks if the client ID of a user is equal to the client ID of Vauff or the user is administrator in the supplied guild
	 *
	 * @param user  The user to check
	 * @param guild The guild to check for permissions in
	 * @return true if the client IDs match and the given user is Vauff or the user is a guild administrator, false otherwise
	 */
	public static boolean hasPermission(IUser user, IGuild guild)
	{
		if (user.getLongID() == 129448521861431296L)
		{
			return true;
		}
		else
		{
			if (user.getPermissionsForGuild(guild).contains(Permissions.ADMINISTRATOR))
			{
				return true;
			}
			else
			{
				return false;
			}
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
			Main.log.error(e);
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
		catch (Exception e)
		{
			Main.log.error(e);
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

	public static void addReactions(IMessage m, boolean cancellable, int i) throws Exception
	{
		String[] reactions = { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine" };

		for (int j = 0; j < 10; j++)
		{
			if (i > j)
			{
				m.addReaction(EmojiManager.getForAlias(":" + reactions[j] + ":"));
				Thread.sleep(250);
			}
		}

		if (cancellable)
		{
			m.addReaction(EmojiManager.getForAlias(":x:"));
		}
	}
}
