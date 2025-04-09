package com.vauff.maunzdiscord.servertracking;

import com.vauff.maunzdiscord.core.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds a timer to update map images
 */
public class MapImageTimer
{
	/**
	 * Updates the mapImages hashmap with the latest available map images from vauff.com and clears related caches
	 */
	public static Runnable timer = () ->
	{
		try
		{
			Logger.log.debug("Starting a map image timer run...");

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
					Logger.log.error("Failed to connect to the map images API, automatically retrying in 1 minute");
					Thread.sleep(60000);
				}
			}

			if (response.getLong("lastUpdated") <= MapImages.lastUpdated)
				return;

			Logger.log.debug("Map image API updated, rebuilding image list and clearing caches");

			for (String appId : response.keySet())
			{
				if (appId.equals("lastUpdated"))
					continue;

				ArrayList<String> maps = new ArrayList<>();

				for (int i = 0; i < response.getJSONArray(appId).length(); i++)
					maps.add(response.getJSONArray(appId).getString(i));

				MapImages.mapImages.put(appId, maps);
				MapImages.mapImageLookupCache.put(appId, new ConcurrentHashMap<>());
				MapImages.mapImageColourCache.clear();
			}

			MapImages.lastUpdated = response.getLong("lastUpdated");
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
	};
}
