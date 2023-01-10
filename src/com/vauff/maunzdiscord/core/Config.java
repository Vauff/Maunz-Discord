package com.vauff.maunzdiscord.core;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class Config extends JSONObject
{
	private File file = new File(Util.getJarLocation() + "config.json");

	public Config() throws Exception
	{
		super(getCfgJson());

		if (!file.exists())
		{
			file.createNewFile();
			put("altPlayingText", "discord.gg/v55fW9b");
			put("botOwners", new JSONArray());
			put("devGuilds", new JSONArray());
			put("discordToken", "");
			put("gitHubToken", "");
			put("userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.61 Safari/537.36");
			put("mongoDatabase", new JSONObject());
			getJSONObject("mongoDatabase").put("connectionString", "");
			getJSONObject("mongoDatabase").put("database", "");
			put("reddit", new JSONObject());
			getJSONObject("reddit").put("id", "");
			getJSONObject("reddit").put("secret", "");
			save();
		}
	}

	public String getPlayingText()
	{
		return getString("altPlayingText");
	}

	public long[] getOwners()
	{
		return getLongArray("botOwners");
	}

	public long[] getDevGuilds()
	{
		return getLongArray("devGuilds");
	}

	public String getToken()
	{
		return getString("discordToken");
	}

	public String getGitHubToken()
	{
		return getString("gitHubToken");
	}

	public String getUserAgent()
	{
		return getString("userAgent");
	}

	public String getMongoConnectionString()
	{
		return getJSONObject("mongoDatabase").getString("connectionString");
	}

	public String getMongoDatabase()
	{
		return getJSONObject("mongoDatabase").getString("database");
	}

	public String getRedditId()
	{
		return getJSONObject("reddit").getString("id");
	}

	public String getRedditSecret()
	{
		return getJSONObject("reddit").getString("secret");
	}

	private long[] getLongArray(String name)
	{
		JSONArray json = getJSONArray(name);
		int length = json.length();
		long[] longs = new long[length];

		for (int i = 0; i < length; i++)
			longs[i] = json.getLong(i);

		return longs;
	}

	private void save() throws IOException
	{
		FileUtils.writeStringToFile(file, toString(4), "UTF-8");
	}

	// Required workaround due to "Call to 'super()' must be first statement in constructor body"
	private static String getCfgJson() throws Exception
	{
		File file = new File(Util.getJarLocation() + "config.json");

		if (file.exists())
			return Util.getFileContents(file);
		else
			return "{}";
	}
}
