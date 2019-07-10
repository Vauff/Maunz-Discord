package com.vauff.maunzdiscord.core;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Permission;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.http.client.ClientException;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;

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
	public static boolean hasPermission(User user) throws Exception
	{
		JSONObject json = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "config.json")));

		for (int i = 0; i < json.getJSONArray("botOwners").length(); i++)
		{
			if (user.getId().asLong() == json.getJSONArray("botOwners").getLong(i))
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
	public static boolean hasPermission(User user, Guild guild) throws Exception
	{
		JSONObject json = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "config.json")));

		for (int i = 0; i < json.getJSONArray("botOwners").length(); i++)
		{
			if (user.getId().asLong() == json.getJSONArray("botOwners").getLong(i))
			{
				return true;
			}
		}

		return (user.asMember(guild.getId()).block().getBasePermissions().block().contains(Permission.ADMINISTRATOR) || user.asMember(guild.getId()).block().getBasePermissions().block().contains(Permission.MANAGE_GUILD) ? true : false);
	}

	/**
	 * Sends a message to a channel
	 *
	 * @param channel The channel
	 * @param author  The author
	 * @param message The message
	 */
	public static Message msg(MessageChannel channel, User author, String message)
	{
		try
		{
			return channel.createMessage(message).block();
		}
		catch (ClientException e)
		{
			if (e.getStatus().code() == 403)
			{
				msg(author.getPrivateChannel().block(), ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command in " + channel.getMention() + " because it's lacking permissions." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
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
	 * @param channel The channel
	 * @param message The message
	 */
	public static Message msg(MessageChannel channel, String message)
	{
		try
		{
			return channel.createMessage(message).block();
		}
		catch (ClientException e)
		{
			if (e.getStatus().code() == 403)
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
	 * Sends an embed to a channel
	 *
	 * @param channel The channel
	 * @param author  The author
	 * @param embed   The embed
	 */
	public static Message msg(MessageChannel channel, User author, Consumer<EmbedCreateSpec> embed)
	{
		try
		{
			return channel.createEmbed(embed).block();
		}
		catch (ClientException e)
		{
			if (e.getStatus().code() == 403)
			{
				msg(author.getPrivateChannel().block(), ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to reply to your command in " + channel.getMention() + " because it's lacking permissions." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
				return null;
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * Sends an embed to a channel
	 *
	 * @param channel The channel
	 * @param embed   The embed
	 */
	public static Message msg(MessageChannel channel, Consumer<EmbedCreateSpec> embed)
	{
		try
		{
			return channel.createEmbed(embed).block();
		}
		catch (ClientException e)
		{
			if (e.getStatus().code() == 403)
			{
				if (!(channel instanceof PrivateChannel))
				{
					Logger.log.warn("Missing permissions to send embed to channel #" + ((GuildChannel) channel).getName() + " (" + channel.getId().asString() + ") in guild " + ((GuildChannel) channel).getGuild().block().getName() + " (" + ((GuildChannel) channel).getGuild().block().getId().asString() + ")");
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
	 * Gets the average color from the picture found at a URL
	 *
	 * @param url The URL leading to the picture
	 * @return The average color of the picture.
	 * If the URL does not contain a picture an RGB color value of 0, 154, 255 will be returned
	 */
	public static Color averageColorFromURL(URL url, boolean handleExceptions)
	{
		BufferedImage image;

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
	 */
	public static boolean addReaction(Message m, String reaction)
	{
		try
		{
			m.addReaction(ReactionEmoji.unicode(reaction)).block();
			return true;
		}
		catch (ClientException e)
		{
			if (e.getStatus().code() == 403)
			{
				msg(m.getChannel().block(), ":exclamation:  |  **Missing permissions!**" + System.lineSeparator() + System.lineSeparator() + "The bot wasn't able to add one or more reactions because it's lacking permissions." + System.lineSeparator() + System.lineSeparator() + "Please have a guild administrator confirm role/channel permissions are correctly set and try again.");
				return false;
			}
			else if (e.getStatus().code() == 404)
			{
				//means m was deleted before this reaction could be added, likely because the user selected an earlier reaction in a menu before all reactions had been added
				return false;
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * Generic method for adding reactions to a message, used so a no permission message can be sent if required
	 *
	 * @param m         The message to add the emojis to
	 * @param reactions An ArrayList<String> that contains a list of reactions that should be added to a given IMessage
	 */
	public static void addReactions(Message m, ArrayList<String> reactions)
	{
		for (String reaction : reactions)
		{
			if (!addReaction(m, reaction))
			{
				break;
			}
		}
	}

	/**
	 * Adds keycap emojis, increasing by value, starting at one and ending at nine. Used for menu selection
	 *
	 * @param m           The message to add the emojis to
	 * @param cancellable Whether an x emoji should be added at the end or not
	 * @param i           The amount of emojis to add, starting by one. If i is 5, all emojis from :one: to :five: will be added.
	 */
	public static void addNumberedReactions(Message m, boolean cancellable, int i)
	{
		ArrayList<String> finalReactions = new ArrayList<>();
		String[] reactions = {
				"\u0031\u20E3",
				"\u0032\u20E3",
				"\u0033\u20E3",
				"\u0034\u20E3",
				"\u0035\u20E3",
				"\u0036\u20E3",
				"\u0037\u20E3",
				"\u0038\u20E3",
				"\u0039\u20E3"
		};

		for (int j = 0; j < i; j++)
		{
			finalReactions.add(reactions[j]);
		}

		if (cancellable)
		{
			finalReactions.add("\u274C");
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
	 * @param guild The Guild for which to check if the bot is enabled
	 * @return true if the bot is enabled for the guild and globally, false otherwise
	 */
	public static boolean isEnabled(Guild guild) throws Exception
	{
		JSONObject botJson = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "config.json")));
		boolean guildEnabled = Main.mongoDatabase.getCollection("guilds").find(eq("guildId", guild.getId().asLong())).first().getBoolean("enabled");

		return botJson.getBoolean("enabled") && guildEnabled;
	}

	/**
	 * Builds a modular page message for the given parameters
	 *
	 * @param entries         An ArrayList<String> that contains all the entries that should be in the page builder
	 * @param pageSize        How many entries should be in a specific page
	 * @param pageNumber      Which page the method should build and send to the provided MessageChannel
	 * @param numberedEntries Whether the entries in a list should be prefixed with their corresponding number in the list or not
	 * @param codeBlock       Whether to surround the entries in a code block or not
	 * @param channel         The MessageChannel that the page message should be sent to
	 * @param user            The User that triggered the command's execution in the first place
	 * @return The Message object for the sent page message if an exception isn't thrown, null otherwise
	 */
	public static Message buildPage(ArrayList<String> entries, String title, int pageSize, int pageNumber, boolean numberedEntries, boolean codeBlock, MessageChannel channel, User user)
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

			Message m;

			if (pageNumber == 1 && (int) Math.ceil((float) entries.size() / (float) pageSize) == 1)
			{
				m = Util.msg(channel, user, "--- **" + title + "** ---" + System.lineSeparator() + list.toString());
			}
			else
			{
				m = Util.msg(channel, user, "--- **" + title + "** --- **Page " + pageNumber + "/" + (int) Math.ceil((float) entries.size() / (float) pageSize) + "** ---" + System.lineSeparator() + list.toString());
			}

			if (pageNumber != 1)
			{
				addReaction(m, "◀");
			}

			if (pageNumber != (int) Math.ceil((float) entries.size() / (float) pageSize))
			{
				addReaction(m, "▶");
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
