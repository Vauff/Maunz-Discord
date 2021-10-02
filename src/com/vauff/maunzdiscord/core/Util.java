package com.vauff.maunzdiscord.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
	 * @param arg The path of the file to read, relative to {@link Util#getJarLocation()}
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
	 * @param arg The File object to read
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
	 * Concatenates a string array from a given start index
	 *
	 * @param args       The array to concatenate
	 * @param startIndex The index to start concatenating the array
	 * @return The concatenated array
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
	 * Checks if a user is a bot administrator
	 *
	 * @param user The user to check
	 * @return true if user is a bot administrator, false otherwise
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
	 * Checks if a user is a bot or guild administrator
	 *
	 * @param user  The user to check
	 * @param guild The guild to check for permissions in
	 * @return true if the user is a bot or guild administrator, false otherwise
	 * @throws Exception
	 */
	public static boolean hasPermission(User user, Guild guild) throws Exception
	{
		if (hasPermission(user))
			return true;

		// Always PM, i think?
		if (Objects.isNull(guild))
			return false;

		return (user.asMember(guild.getId()).block().getBasePermissions().block().contains(Permission.ADMINISTRATOR) || user.asMember(guild.getId()).block().getBasePermissions().block().contains(Permission.MANAGE_GUILD) ? true : false);
	}

	//TODO: Remove the msg methods for responding to users, they are basically useless now with error handling for them moved to MessageCreateThread

	/**
	 * Sends a message to a channel
	 *
	 * @param channel The channel
	 * @param author  The author
	 * @param message The message
	 * @return The message object
	 */
	public static Message msg(MessageChannel channel, User author, String message)
	{
		return channel.createMessage(message).withAllowedMentions(AllowedMentions.builder().build()).block();
	}

	/**
	 * Sends a message to a channel
	 *
	 * @param channel The channel
	 * @param message The message
	 * @return The message object
	 */
	public static Message msg(MessageChannel channel, String message)
	{
		try
		{
			return channel.createMessage(message).withAllowedMentions(AllowedMentions.builder().build()).block();
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
	 * @return The message object
	 */
	public static Message msg(MessageChannel channel, User author, EmbedCreateSpec embed)
	{
		return channel.createMessage().withEmbeds(embed).withAllowedMentions(AllowedMentions.builder().build()).block();
	}

	/**
	 * Sends an embed to a channel
	 *
	 * @param channel The channel
	 * @param embed   The embed
	 * @return The message object
	 */
	public static Message msg(MessageChannel channel, EmbedCreateSpec embed)
	{
		try
		{
			return channel.createMessage().withEmbeds(embed).withAllowedMentions(AllowedMentions.builder().build()).block();
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
	 * @param url              The URL leading to the picture
	 * @param handleExceptions Whether to return (0, 154, 255) when an exception happens
	 * @return The average color of the picture, null or (0, 154, 255) on error
	 */
	public static Color averageColorFromURL(URL url, boolean handleExceptions)
	{
		BufferedImage image;

		try
		{
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent", getUserAgent());
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
				return null;
			}
		}
	}

	/**
	 * Generic method for adding a reaction to a message, used so a no permission message can be sent if required
	 *
	 * @param m        The message to add the reaction to
	 * @param reaction The reaction to add
	 * @return true if the reaction was added successfully, false otherwise
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
	 * @param m         The message to add the reactions to
	 * @param reactions An ArrayList<String> that contains a list of reactions to add
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
	 * Adds keycap reactions, increasing by value, starting at one and ending at nine. Used for menu selection
	 *
	 * @param m           The message to add the reactions to
	 * @param cancellable Whether an x reaction should be added at the end or not
	 * @param i           The amount of reactions to add
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
	 * @throws Exception
	 */
	public static boolean isEnabled() throws Exception
	{
		JSONObject botJson = new JSONObject(Util.getFileContents(new File(Util.getJarLocation() + "config.json")));

		return botJson.getBoolean("enabled");
	}

	/**
	 * Checks whether the bot is enabled both for a specific guild and globally
	 *
	 * @param guild The Guild for which to check if the bot is enabled
	 * @return true if the bot is enabled in both, false otherwise
	 * @throws Exception
	 */
	public static boolean isEnabled(Guild guild) throws Exception
	{
		return isEnabled() && Main.mongoDatabase.getCollection("guilds").find(eq("guildId", guild.getId().asLong())).first().getBoolean("enabled");
	}

	/**
	 * Builds a modular page message in response to an interaction for the given parameters
	 *
	 * @param entries           An ArrayList<String> that contains all the entries that should be in the page builder
	 * @param title             The title to give all the pages
	 * @param pageSize          How many entries should be in a specific page
	 * @param pageNumber        Which page the method should build and send to the provided channel
	 * @param numberStyle       Which style to use for numbered entries, 0 = none, 1 = standard, 2 = code block surrounded & unique per page
	 * @param codeBlock         Whether to surround all the entries in a code block or not
	 * @param numberedReactions Whether to add numbered reactions for each entry
	 * @param cancellable       Whether to add an X emoji to close the page
	 * @param event             The InteractionCreateEvent that is being responded to
	 * @return The Message object for the sent page message if an exception isn't thrown, null otherwise
	 */
	public static Message buildPage(List<String> entries, String title, int pageSize, int pageNumber, int numberStyle, boolean codeBlock, boolean numberedReactions, boolean cancellable, InteractionCreateEvent event)
	{
		return buildPage(entries, title, pageSize, pageNumber, numberStyle, codeBlock, numberedReactions, cancellable, null, null, event);
	}

	/**
	 * Builds a modular page message for the given parameters
	 *
	 * @param entries           An ArrayList<String> that contains all the entries that should be in the page builder
	 * @param title             The title to give all the pages
	 * @param pageSize          How many entries should be in a specific page
	 * @param pageNumber        Which page the method should build and send to the provided channel
	 * @param numberStyle       Which style to use for numbered entries, 0 = none, 1 = standard, 2 = code block surrounded & unique per page
	 * @param codeBlock         Whether to surround all the entries in a code block or not
	 * @param numberedReactions Whether to add numbered reactions for each entry
	 * @param cancellable       Whether to add an X emoji to close the page
	 * @param channel           The MessageChannel that the page message should be sent to (if legacy command)
	 * @param user              The User that triggered the command's execution in the first place (if legacy command)
	 * @return The Message object for the sent page message if an exception isn't thrown, null otherwise
	 */
	public static Message buildPage(List<String> entries, String title, int pageSize, int pageNumber, int numberStyle, boolean codeBlock, boolean numberedReactions, boolean cancellable, MessageChannel channel, User user)
	{
		return buildPage(entries, title, pageSize, pageNumber, numberStyle, codeBlock, numberedReactions, cancellable, channel, user, null);
	}

	private static Message buildPage(List<String> entries, String title, int pageSize, int pageNumber, int numberStyle, boolean codeBlock, boolean numberedReactions, boolean cancellable, MessageChannel channel, User user, InteractionCreateEvent event)
	{
		if (pageNumber > (int) Math.ceil((float) entries.size() / (float) pageSize))
		{
			if (Objects.isNull(event))
				return Util.msg(channel, user, "That page doesn't exist!");
			else
				return Main.gateway.getMessageById(event.getInteraction().getChannelId(), Snowflake.of(event.getInteractionResponse().createFollowupMessage("That page doesn't exist!").block().id())).block();
		}
		else
		{
			StringBuilder list = new StringBuilder();

			if (codeBlock)
			{
				list.append("```" + System.lineSeparator());
			}

			int usedEntries = 0;

			for (int i = (int) (entries.size() - ((((float) entries.size() / (float) pageSize) - (pageNumber - 1)) * pageSize)); entries.size() - ((((float) entries.size() / (float) pageSize) - pageNumber) * pageSize) > i; i++)
			{
				if (i > entries.size() - 1)
				{
					break;
				}
				else
				{
					usedEntries++;

					if (numberStyle == 0)
					{
						list.append(entries.get(i) + System.lineSeparator());
					}
					else if (numberStyle == 1)
					{
						list.append((i + 1) + " - " + entries.get(i) + System.lineSeparator());
					}
					else if (numberStyle == 2)
					{
						list.append("**`[" + ((i + 1) - (pageSize * (pageNumber - 1))) + "]`** | " + entries.get(i) + System.lineSeparator());
					}
				}
			}

			if (codeBlock)
			{
				list.append("```");
			}

			Message m;
			String msg;

			if (pageNumber == 1 && (int) Math.ceil((float) entries.size() / (float) pageSize) == 1)
				msg = "--- **" + title + "** ---" + System.lineSeparator() + list.toString();
			else
				msg = "--- **" + title + "** --- **Page " + pageNumber + "/" + (int) Math.ceil((float) entries.size() / (float) pageSize) + "** ---" + System.lineSeparator() + list.toString();

			if (Objects.isNull(event))
				m = Util.msg(channel, user, msg);
			else
				m = Main.gateway.getMessageById(event.getInteraction().getChannelId(), Snowflake.of(event.getInteractionResponse().createFollowupMessage(msg).block().id())).block();

			final int finalUsedEntries = usedEntries;
			ScheduledExecutorService reactionAddPool = Executors.newScheduledThreadPool(1);

			reactionAddPool.schedule(() ->
			{
				reactionAddPool.shutdown();

				if (pageNumber != 1)
				{
					addReaction(m, "◀");
				}
				if (numberedReactions)
				{
					Util.addNumberedReactions(m, false, finalUsedEntries);
				}
				if (pageNumber != (int) Math.ceil((float) entries.size() / (float) pageSize))
				{
					addReaction(m, "▶");
				}
				if (cancellable)
				{
					Util.addReaction(m, "\u274C");
				}
			}, 250, TimeUnit.MILLISECONDS);

			return m;
		}
	}

	/**
	 * Converts an emoji into an integer
	 *
	 * @param emoji The string value of the emoji
	 * @return The integer value of the emoji
	 */
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
			return -1;
		}
	}

	/**
	 * Provides the bots global user agent from config.json
	 *
	 * @return The user agent
	 */
	public static String getUserAgent() throws Exception
	{
		JSONObject json = new JSONObject(getFileContents("config.json"));

		return json.getString("userAgent");
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
}
