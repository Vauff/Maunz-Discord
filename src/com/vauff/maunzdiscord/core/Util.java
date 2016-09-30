package com.vauff.maunzdiscord.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

public class Util
{
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
}
