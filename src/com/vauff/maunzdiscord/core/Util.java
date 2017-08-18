package com.vauff.maunzdiscord.core;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class Util
{
	public static boolean isEnabled = true;
	public static boolean devMode;
	public static String token;

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
			Main.log.error(e);

			return null;
		}
	}

	public static String getFileContents(String arg) throws IOException
	{
		File file = new File(getJarLocation() + arg);

		return FileUtils.readFileToString(file, "UTF-8");
	}

	public static String getFileContents(File arg) throws IOException
	{
		if (!arg.exists())
		{
			arg.createNewFile();
			FileUtils.writeStringToFile(arg, " ", "UTF-8");
		}

		return FileUtils.readFileToString(arg, "UTF-8");
	}

	public static String getTime()
	{
		return getTime(System.currentTimeMillis());
	}

	public static String getTime(long time)
	{
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE MMMM d'" + getOrdinal(date.getDate()) + "', yyyy, h:mm a z");

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}

	public static String getUptime()
	{
		MainListener.uptime.split();

		String[] uptimeraw = MainListener.uptime.toSplitString().split("\\.");
		int hours = Integer.parseInt(uptimeraw[0].split(":")[0]);
		int days = (hours / 24) >> 0;

		hours = hours % 24;

		return days + ":" + (hours < 10 ? "0" + hours : hours) + ":" + uptimeraw[0].split(":")[1] + ":" + uptimeraw[0].split(":")[2];
	}

	public static String addArgs(String[] args, int startIndex)
	{
		String s = "";

		for (int i = startIndex; i < args.length; i++)
		{
			s += args[i] + " ";
		}

		return s.substring(0, s.lastIndexOf(" "));
	}

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

	public static boolean hasPermission(IUser user)
	{
		return user.getLongID() == 129448521861431296L;
	}

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

	public static Color averageColorFromURL(URL url)
	{
		BufferedImage image = null;

		try
		{
			image = ImageIO.read(url);
		}
		catch (Exception e)
		{
			return new Color(0, 154, 255);
		}

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
}
