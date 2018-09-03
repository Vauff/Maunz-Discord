package com.vauff.maunzdiscord.core;

import com.vdurmont.emoji.EmojiManager;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * A class holding several static utility methods
 */
public class Util
{
	/**
	 * The Discord API token of the bot, gets set in {@link Main#main(String[])}
	 */
	public static String token;

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
	 * @param arg The path of the file, relative to {@link Util#getJarLocation()}
	 * @return The content of the file
	 * @throws Exception
	 */
	public static String getFileContents(String arg) throws Exception
	{
		File file = new File(getJarLocation() + arg);

		return FileUtils.readFileToString(file, "UTF-8");
	}

	/**
	 * Gets the contents of a file as a string
	 *
	 * @param arg The path of the file
	 * @return The content of the file, or an empty string if an exception has been caught
	 * @throws Exception
	 */
	public static String getFileContents(File arg) throws Exception
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
	 * Checks if the client ID of a user is equal to the client ID of the botOwner supplied in config.json
	 *
	 * @param user The user to check
	 * @return true if the client IDs match and the given user is the botOwner supplied in config.json, false otherwise
	 * @throws Exception
	 */
	public static boolean hasPermission(IUser user) throws Exception
	{
		JSONObject json = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "config.json")));

		for (int i = 0; i < json.getJSONArray("botOwners").length(); i++)
		{
			if (user.getLongID() == json.getJSONArray("botOwners").getLong(i))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the client ID of a user is equal to the client ID of the botOwner supplied in config.json or the user is administrator in the supplied guild
	 *
	 * @param user  The user to check
	 * @param guild The guild to check for permissions in
	 * @return true if the client IDs match and the given user is the botOwner supplied in config.json or the user is a guild administrator, false otherwise
	 * @throws Exception
	 */
	public static boolean hasPermission(IUser user, IGuild guild) throws Exception
	{
		JSONObject json = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "config.json")));

		for (int i = 0; i < json.getJSONArray("botOwners").length(); i++)
		{
			if (user.getLongID() == json.getJSONArray("botOwners").getLong(i))
			{
				return true;
			}
		}

		return (user.getPermissionsForGuild(guild).contains(Permissions.ADMINISTRATOR) || user.getPermissionsForGuild(guild).contains(Permissions.MANAGE_SERVER) ? true : false);
	}

	/**
	 * Sends a message to a channel
	 *
	 * @param channel The channel
	 * @param author  The author
	 * @param message The message
	 */
	public static IMessage msg(IChannel channel, IUser author, String message)
	{
		try
		{
			return channel.sendMessage(message);
		}
		catch (MissingPermissionsException e)
		{
			if (e.getMissingPermissions().contains(Permissions.SEND_MESSAGES))
			{
				msg(author.getOrCreatePMChannel(), ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command in " + channel.mention() + " because it's lacking the **SEND_MESSAGES** permission." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
			}
			else
			{
				Logger.log.error("", e);
			}

			return null;
		}
		catch (DiscordException e)
		{
			Logger.log.error("", e);
			return null;
		}
	}

	/**
	 * Sends a message to a channel
	 *
	 * @param channel The channel
	 * @param message The message
	 */
	public static IMessage msg(IChannel channel, String message)
	{
		try
		{
			return channel.sendMessage(message);
		}
		catch (MissingPermissionsException e)
		{
			Logger.log.error("", e);
			return null;
		}
		catch (DiscordException e)
		{
			Logger.log.error("", e);
			return null;
		}
	}

	/**
	 * Sends an embed to a channel
	 *
	 * @param channel The channel
	 * @param author  The author
	 * @param message The embed
	 */
	public static IMessage msg(IChannel channel, IUser author, EmbedObject message)
	{
		try
		{
			return channel.sendMessage("", message, false);
		}
		catch (MissingPermissionsException e)
		{
			if (e.getMissingPermissions().contains(Permissions.SEND_MESSAGES))
			{
				msg(author.getOrCreatePMChannel(), ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command in " + channel.mention() + " because it's lacking the **SEND_MESSAGES** permission." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
			}
			else if (e.getMissingPermissions().contains(Permissions.EMBED_LINKS))
			{
				msg(channel, ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command because it's lacking the **EMBED_LINKS** permission." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
			}
			else
			{
				Logger.log.error("", e);
			}

			return null;
		}
		catch (DiscordException e)
		{
			Logger.log.error("", e);
			return null;
		}
	}

	/**
	 * Sends an embed to a channel
	 *
	 * @param channel The channel
	 * @param message The embed
	 */
	public static IMessage msg(IChannel channel, EmbedObject message)
	{
		try
		{
			return channel.sendMessage("", message, false);
		}
		catch (MissingPermissionsException e)
		{
			if (e.getMissingPermissions().contains(Permissions.EMBED_LINKS))
			{
				msg(channel, ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to send a message because it's lacking the **EMBED_LINKS** permission." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set.");
			}
			else
			{
				Logger.log.error("", e);
			}

			return null;
		}
		catch (DiscordException e)
		{
			Logger.log.error("", e);
			return null;
		}
	}

	/**
	 * Gets the average color from the picture found at a URL
	 *
	 * @param url The URL leading to the picture
	 * @return The average color of the picture.
	 * If the URL does not contain a picture an RGB color value of 0, 154, 255 will be returned
	 */
	public static Color averageColorFromURL(URL url, boolean handleExceptions)
	{
		BufferedImage image = null;

		try
		{
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36");
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
			if (handleExceptions)
			{
				return new Color(0, 154, 255);
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * Generic method for adding a reaction to a message, used so a no permission message can be sent if required
	 *
	 * @param m        The message to add the emojis to
	 * @param reaction A string that contains a reaction that should be added to a given IMessage
	 * @throws Exception
	 */
	public static void addReaction(IMessage m, String reaction) throws Exception
	{
		try
		{
			m.addReaction(EmojiManager.getForAlias(":" + reaction + ":"));
			Thread.sleep(250);
		}
		catch (MissingPermissionsException e)
		{
			if (e.getMissingPermissions().contains(Permissions.ADD_REACTIONS))
			{
				msg(m.getChannel(), ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to add one or more reactions because it's lacking the **ADD_REACTIONS** permission." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
			}
			else
			{
				Logger.log.error(e);
			}
		}
	}

	/**
	 * Generic method for adding reactions to a message, used so a no permission message can be sent if required
	 *
	 * @param m         The message to add the emojis to
	 * @param reactions An ArrayList<String> that contains a list of reactions that should be added to a given IMessage
	 * @throws Exception
	 */
	public static void addReactions(IMessage m, ArrayList<String> reactions) throws Exception
	{
		try
		{
			for (String reaction : reactions)
			{
				m.addReaction(EmojiManager.getForAlias(":" + reaction + ":"));
				Thread.sleep(250);
			}
		}
		catch (MissingPermissionsException e)
		{
			if (e.getMissingPermissions().contains(Permissions.ADD_REACTIONS))
			{
				msg(m.getChannel(), ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to add one or more reactions because it's lacking the **ADD_REACTIONS** permission." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
			}
			else
			{
				Logger.log.error(e);
			}
		}
	}

	/**
	 * Adds keycap emojis, increasing by value, starting at one and ending at nine. Used for menu selection
	 *
	 * @param m           The message to add the emojis to
	 * @param cancellable Whether an x emoji should be added at the end or not
	 * @param i           The amount of emojis to add, starting by one. If i is 5, all emojis from :one: to :five: will be added.
	 * @throws Exception
	 */
	public static void addNumberedReactions(IMessage m, boolean cancellable, int i) throws Exception
	{
		ArrayList<String> finalReactions = new ArrayList<>();
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
			finalReactions.add(reactions[j]);
			Thread.sleep(250);
		}

		if (cancellable)
		{
			finalReactions.add("x");
		}

		addReactions(m, finalReactions);
	}

	/**
	 * Checks whether the bot is enabled globally
	 *
	 * @return true if the bot is enabled globally, false otherwise
	 */
	public static boolean isEnabled() throws Exception
	{
		JSONObject botJson = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "config.json")));

		return botJson.getBoolean("enabled");
	}

	/**
	 * Checks whether the bot is enabled for both a specified guild and globally
	 *
	 * @param guild The guild for which to check if the bot is enabled
	 * @return true if the bot is enabled for the guild and globally, false otherwise
	 */
	public static boolean isEnabled(IGuild guild) throws Exception
	{
		JSONObject botJson = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "config.json")));
		JSONObject guildJson = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "data/guilds/" + guild.getStringID() + ".json")));

		return botJson.getBoolean("enabled") && guildJson.getBoolean("enabled");
	}

	/**
	 * Builds a modular page message for the given parameters
	 *
	 * @param entries         An ArrayList<String> that contains all the entries that should be in the page builder
	 * @param pageSize        How many entries should be in a specific page
	 * @param pageNumber      Which page the method should build and send to the provided IChannel
	 * @param numberedEntries Whether the entries in a list should be prefixed with their corresponding number in the list or not
	 * @param codeBlock       Whether to surround the entries in a code block or not
	 * @param channel         The IChannel that the page message should be sent to
	 * @param user            The IUser that triggered the command's execution in the first place
	 * @return The IMessage object for the sent page message if an exception isn't thrown, null otherwise
	 */
	public static IMessage buildPage(ArrayList<String> entries, String title, int pageSize, int pageNumber, boolean numberedEntries, boolean codeBlock, IChannel channel, IUser user)
	{
		if (pageNumber > (int) Math.ceil((float) entries.size() / (float) pageSize))
		{
			return Util.msg(channel, user, "That page doesn't exist!");
		}
		else
		{
			StringBuilder list = new StringBuilder();

			if (codeBlock)
			{
				list.append("```" + System.lineSeparator());
			}


			for (int i = (int) (entries.size() - ((((float) entries.size() / (float) pageSize) - (pageNumber - 1)) * pageSize)); entries.size() - ((((float) entries.size() / (float) pageSize) - pageNumber) * pageSize) > i; i++)
			{
				if (i > entries.size() - 1)
				{
					break;
				}
				else
				{
					if (numberedEntries)
					{
						list.append((i + 1) + " - " + entries.get(i) + System.lineSeparator());
					}
					else
					{
						list.append(entries.get(i) + System.lineSeparator());
					}
				}
			}

			if (codeBlock)
			{
				list.append("```");
			}

			IMessage m;

			if (pageNumber == 1 && (int) Math.ceil((float) entries.size() / (float) pageSize) == 1)
			{
				m = Util.msg(channel, user, "--- **" + title + "** ---" + System.lineSeparator() + list.toString());
			}
			else
			{
				m = Util.msg(channel, user, "--- **" + title + "** --- **Page " + pageNumber + "/" + (int) Math.ceil((float) entries.size() / (float) pageSize) + "** ---" + System.lineSeparator() + list.toString());
			}

			try
			{
				if (pageNumber != 1)
				{
					m.addReaction(EmojiManager.getForAlias(":arrow_backward:"));
					Thread.sleep(250);
				}

				Thread.sleep(250);

				if (pageNumber != (int) Math.ceil((float) entries.size() / (float) pageSize))
				{
					m.addReaction(EmojiManager.getForAlias(":arrow_forward:"));
					Thread.sleep(250);
				}
			}
			catch (Exception e)
			{
				Logger.log.error("", e);
			}

			return m;
		}
	}

	public static int emojiToInt(String emoji)
	{
		if (emoji.equals("1⃣"))
		{
			return 1;
		}
		else if (emoji.equals("2⃣"))
		{
			return 2;
		}
		else if (emoji.equals("3⃣"))
		{
			return 3;
		}
		else if (emoji.equals("4⃣"))
		{
			return 4;
		}
		else if (emoji.equals("5⃣"))
		{
			return 5;
		}
		else if (emoji.equals("6⃣"))
		{
			return 6;
		}
		else if (emoji.equals("7⃣"))
		{
			return 7;
		}
		else if (emoji.equals("8⃣"))
		{
			return 8;
		}
		else if (emoji.equals("9⃣"))
		{
			return 9;
		}
		else
		{
			return 0;
		}
	}
}
