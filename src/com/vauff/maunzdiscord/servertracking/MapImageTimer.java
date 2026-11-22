package com.vauff.maunzdiscord.servertracking;

import com.vauff.maunzdiscord.core.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds a timer to update map images
 */
public class MapImageTimer {
    private static final String LIST_URL = "https://vauff.com/mapimgs/list.php";

    /**
     * Updates the mapImages hashmap with the latest available map images from vauff.com and clears related caches
     */
    public static final Runnable timer = () ->
    {
        try {
            Logger.log.debug("Starting a map image timer run...");

            JSONObject response = fetchList();

            if (response.getLong("lastUpdated") <= MapImages.lastUpdated)
                return;

            Logger.log.debug("Map image API updated, rebuilding image list and clearing caches");

            for (String appId : response.keySet()) {
                if (appId.equals("lastUpdated"))
                    continue;

                ArrayList<String> maps = new ArrayList<>();

                for (int i = 0; i < response.getJSONArray(appId).length(); i++)
                    maps.add(response.getJSONArray(appId).getString(i));

                MapImages.mapImages.put(appId, maps);
                MapImages.mapImageLookupCache.put(appId, new ConcurrentHashMap<>());
            }

            MapImages.mapImageColourCache.clear();
            MapImages.lastUpdated = response.getLong("lastUpdated");
        } catch (Exception e) {
            Logger.log.error("", e);
        }
    };

    private static JSONObject fetchList() throws InterruptedException {
        while (true) {
            try {
                HttpURLConnection connection = (HttpURLConnection) URI.create(LIST_URL).toURL().openConnection();

                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                if (connection.getResponseCode() != 200)
                    throw new IOException("HTTP " + connection.getResponseCode());

                try (InputStream stream = connection.getInputStream()) {
                    return new JSONObject(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
                }
            } catch (IOException | JSONException e) {
                Logger.log.error("Failed to connect to the map images API, automatically retrying in 1 minute");
                Thread.sleep(60000);
            }
        }
    }
}
