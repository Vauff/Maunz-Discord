package com.vauff.maunzdiscord.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

public class Util
{
	public static boolean devMode;
	public static String token;
	public static String mapChannel;

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
		BufferedReader reader = new BufferedReader(new FileReader(file));

		if (!file.exists())
		{
			file.createNewFile();
		}
		String result = reader.readLine();
		reader.close();

		return result;
	}

	public static boolean hasPermission(IUser user)
	{
		if (user.getID().equals("129448521861431296"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static void msg(IChannel channel, String message)
	{
		try
		{
			new MessageBuilder(Main.client).withChannel(channel).withContent(message).build();
		}
		catch (Exception e)
		{
			Main.log.error(e);
		}
	}
	
	public static void msg(String channel, String message)
	{
		try
		{
			new MessageBuilder(Main.client).withChannel(channel).withContent(message).build();
		}
		catch (Exception e)
		{
			Main.log.error(e);
		}
	}
}
