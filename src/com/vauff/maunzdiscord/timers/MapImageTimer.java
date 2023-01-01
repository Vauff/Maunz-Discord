package com.vauff.maunzdiscord.timers;

import com.vauff.maunzdiscord.core.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Holds a timer to update map images
 */
public class MapImageTimer
{
	/**
	 * Holds the latest image lists pulled from vauff.com/mapimgs
	 */
	public static HashMap<Integer, ArrayList<String>> mapImages = new HashMap<>();

	/**
	 * Cached lookup results from Util#getMapImageURL
	 */
	public static HashMap<Integer, HashMap<String, String>> mapImageLookupCache = new HashMap<>();

	/**
	 * Updates the mapImages hashmap with the latest available map images from vauff.com/mapimgs
	 */
	public static Runnable timer = () ->
	{
		try
		{
			int attempts = 0;
			JSONObject response;

			while (true)
			{
				try
				{
					URL url = new URL("https://vauff.com/mapimgs/list.php");
					HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

					connection.setRequestMethod("GET");
					connection.connect();

					if (connection.getResponseCode() != 200)
						throw new IOException();

					String jsonString = "";
					Scanner scanner = new Scanner(url.openStream());

					while (scanner.hasNext())
						jsonString += scanner.nextLine();

					scanner.close();
					response = new JSONObject(jsonString);

					break;
				}
				catch (IOException | JSONException e)
				{
					attempts++;

					if (attempts >= 5)
					{
						Logger.log.error("Failed to connect to the map images API, automatically retrying in 1 hour");
						return;
					}
					else
					{
						Thread.sleep(1000);
					}
				}
			}

			for (String key : response.keySet())
			{
				ArrayList<String> maps = new ArrayList<>();
				int appId = Integer.parseInt(key);

				for (int i = 0; i < response.getJSONArray(key).length(); i++)
					maps.add(response.getJSONArray(key).getString(i));

				mapImages.put(appId, maps);
				mapImageLookupCache.put(appId, new HashMap<>());
			}
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	};
}
