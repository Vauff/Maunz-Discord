package com.vauff.maunzdiscord.servertracking;

import com.vauff.maunzdiscord.core.Util;
import discord4j.rest.util.Color;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.HashMap;

public class MapImages
{
	/**
	 * Holds the latest image lists pulled from vauff.com
	 */
	public static HashMap<String, ArrayList<String>> mapImages = new HashMap<>();

	/**
	 * Cached lookup results from Util#getMapImageURL
	 */
	public static HashMap<String, HashMap<String, String>> mapImageLookupCache = new HashMap<>();

	/**
	 * Cached colour results from Util#averageColourFromURL
	 */
	public static HashMap<String, Color> mapImageColourCache = new HashMap<>();

	/**
	 * Unix timestamp of when the currently stored map images API data was last updated
	 */
	public static long lastUpdated = 0L;

	/**
	 * Finds the best map image URL for a map, if possible
	 * Can be a vauff.com or gametracker.com image
	 *
	 * @param map   The map name
	 * @param appId App ID of the game
	 * @return Image URL for the map, or "" on failure
	 */
	public static String getMapImageURL(String map, String appId)
	{
		// Force lower case since GameTracker does, and for an accurate levenshtein distance
		String mapLower = map.toLowerCase();

		if (!mapImageLookupCache.containsKey(appId))
			mapImageLookupCache.put(appId, new HashMap<>());

		HashMap<String, String> gameCache = mapImageLookupCache.get(appId);

		if (gameCache.containsKey(mapLower))
			return gameCache.get(mapLower);

		String url = getVauffMapImageURL(mapLower, appId);
		String gtName = appIdToGameTrackerName(appId);

		// If we didn't find a high quality map image at vauff.com, try to fall back to GameTracker
		if (url.equals("") && !gtName.equals(""))
			url = "https://image.gametracker.com/images/maps/160x120/" + gtName + "/" + mapLower.replace(" ", "%20") + ".jpg";

		mapImageLookupCache.get(appId).put(mapLower, url);

		return url;
	}

	/**
	 * Gets the average colour of a map image URL, making appropriate use of the cache
	 *
	 * @param url The URL of the map image
	 * @return The average colour of the map image
	 * @throws Exception
	 */
	public static Color getMapImageColour(String url) throws Exception
	{
		if (mapImageColourCache.containsKey(url))
			return mapImageColourCache.get(url);

		Color colour = Util.averageColourFromURL(url, true);

		mapImageColourCache.put(url, colour);
		return colour;
	}

	/**
	 * Finds an image URL for a mapname from vauff.com, if possible
	 *
	 * @param map   The lower case map name
	 * @param appId App ID of the game
	 * @return Image URL for the map, or "" on failure
	 */
	private static String getVauffMapImageURL(String map, String appId)
	{
		// As we accomodate for mapCharacterLimit, all image map names at vauff.com should never exceed 31 characters
		String trimmedMap = StringUtils.substring(map, 0, 31);
		String bestMatch = "";
		int bmLevenshteinDist = 999;

		if (!mapImages.containsKey(appId))
			return "";

		for (String arrayMap : mapImages.get(appId))
		{
			// Keep both parameters lower case for an accurate levenshtein distance
			String arrayMapLower = arrayMap.toLowerCase();
			int distance = new LevenshteinDistance().apply(trimmedMap, arrayMapLower);

			if (distance < bmLevenshteinDist && trimmedMap.startsWith(arrayMapLower))
			{
				bestMatch = arrayMap;
				bmLevenshteinDist = distance;
			}
		}

		if (bestMatch.equals(""))
			return "";
		else
			return "https://vauff.com/mapimgs/" + appId + "/" + bestMatch.replace(" ", "%20") + ".jpg";
	}

	/**
	 * Matches an app ID to the game name used by GameTracker
	 * Currently excluded: GT Games not on Steam
	 *
	 * @param appId App ID of the game
	 * @return GameTracker's directory name for the given game
	 */
	private static String appIdToGameTrackerName(String appId)
	{
		return switch (appId)
		{
			case "10" -> "cs";
			case "20" -> "tfc";
			case "30" -> "dod";
			case "70" -> "hl";
			case "80" -> "czero";
			case "240" -> "css";
			case "300" -> "dods";
			case "320" -> "hl2dm";
			case "440" -> "tf2";
			case "500" -> "l4d";
			case "550" -> "left4dead2";
			case "570" -> "dota2";
			case "630" -> "alienswarm";
			case "730_csgo" -> "csgo";
			// TODO: Update this if GameTracker ever gets CS2 support
			case "730_cs2" -> "csgo";
			case "1200" -> "ror";
			case "1250" -> "killingfloor";
			case "1280" -> "rordh";
			case "2200" -> "q3";
			case "2210" -> "q4";
			case "2310" -> "qw";
			case "2320" -> "q2";
			case "2620" -> "cod";
			case "2630" -> "cod2";
			case "2640" -> "uo";
			case "4000" -> "garrysmod";
			case "4920" -> "ns2";
			case "6020" -> "swjk";
			case "6060" -> "swbf2";
			case "7940" -> "cod4";
			case "9010" -> "wolf";
			case "9050" -> "doom3";
			case "9460" -> "ffow";
			case "10000" -> "etqw";
			case "10090" -> "codww";
			case "13140" -> "aa3";
			case "13210" -> "ut3";
			case "13230" -> "ut2k4";
			case "13240" -> "ut";
			case "17300" -> "crysis";
			case "17330" -> "warhead";
			case "17500" -> "hl2zp";
			case "17700" -> "ins";
			case "21090" -> "fear";
			case "22350" -> "brink";
			case "24960" -> "bc2";
			case "33900" -> "arma2";
			case "35450" -> "ro2";
			case "42700" -> "blackops";
			case "47790" -> "moh";
			case "55100" -> "homefront";
			case "63200" -> "mnc";
			case "63380" -> "sniperelite2";
			case "65780" -> "arma";
			case "96300" -> "ravaged";
			case "107410" -> "arma3";
			case "108800" -> "crysis2";
			case "115300" -> "mw3";
			case "203290" -> "aapg";
			case "211820" -> "starbound";
			case "214630" -> "blackopsmac";
			case "221100" -> "dayz";
			case "222880" -> "insurgency2014";
			case "224580" -> "dayzmod";
			case "232090" -> "kf2";
			case "238430" -> "contagion";
			case "244850" -> "spaceengi";
			case "251570" -> "7daystodie";
			case "252490" -> "rust";
			case "253530" -> "ff";
			case "259080" -> "jc2";
			case "282440" -> "ql";
			case "290080" -> "lifyo";
			case "311210" -> "codbo3";
			case "346110" -> "arkse";
			case "393420" -> "hurtworld";
			case "440900" -> "conan";
			case "489940" -> "battalion1944";
			case "529180" -> "dnl";
			case "581320" -> "ins_sandstorm";
			case "659280" -> "urbanterror";
			case "1238820" -> "bf3";
			case "1238860" -> "bf4";
			case "1238880" -> "bfhl";
			case "1873030" -> "et";
			default -> "";
		};
	}
}
