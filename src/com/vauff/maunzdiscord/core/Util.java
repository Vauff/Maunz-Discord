package com.vauff.maunzdiscord.core;

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
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.apache.commons.io.FileUtils;
import org.bson.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
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
		Logger.logMessage(event.getInteraction().getChannel().block(), event.getReply().block().getId().asString(), message, embeds, components);
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

			Message sentMsg = msg.withAllowedMentions(AllowedMentions.suppressAll()).block();
			Logger.logMessage(channel, sentMsg.getId().asString(), message, Arrays.asList(embed), null);

			return sentMsg;
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
	 * Gets the average colour from the picture found at a URL
	 *
	 * @param url              The URL leading to the picture
	 * @param handleExceptions Whether to return (0, 154, 255) when an exception happens
	 * @return The average colour of the picture, null or (0, 154, 255) on error
	 */
	public static Color averageColourFromURL(String url, boolean handleExceptions) throws Exception
	{
		BufferedImage image;

		try
		{
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", Main.cfg.getFakeUserAgent());
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

	/**
	 * Gets the bots real user agent
	 *
	 * @return The user agent
	 */
	public static String getUserAgent()
	{
		return "Maunz-Discord " + Main.version;
	}

	/**
	 * Adds escape characters to all characters Discord uses for Markdown formatting
	 *
	 * @param text The original string
	 * @return The escaped string
	 */
	public static String escapeString(String text)
	{
		return text.replace("_", "\\_").replace("*", "\\*").replace("`", "\\`").replace("-", "\\-").replace(">", "\\>")
			.replace("#", "\\#").replace("[", "\\[").replace("]", "\\]").replace("(", "\\(").replace(")", "\\)");
	}

	public static final long MAP_AUTOCOMPLETE_CACHE_INVALIDATED = 60 * 10 * 1000;
	public static final int FUZZY_LIMIT_CUTOFF = 75;

	public static String getMapKey(String mapName)
	{
		return mapName.replace('_', ' ');
	}

	public static ApplicationCommandOptionChoiceData mapChoice(final String map)
	{
		return ApplicationCommandOptionChoiceData.builder()
			.name(map)
			.value(map)
			.build();
	}

}
