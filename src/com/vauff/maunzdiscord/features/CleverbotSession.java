package com.vauff.maunzdiscord.features;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import com.vauff.maunzdiscord.core.Util;
import org.json.JSONObject;

import java.util.Locale;

public class CleverbotSession
{
	private ChatterBotSession session;

	public CleverbotSession() throws Exception
	{
		JSONObject json = new JSONObject(Util.getFileContents("config.json"));
		ChatterBotFactory factory = new ChatterBotFactory();
		ChatterBot bot = factory.create(ChatterBotType.CLEVERBOT, json.getString("cleverbotAPIKey"));

		session = bot.createSession(Locale.ENGLISH);
	}

	public ChatterBotSession getSession()
	{
		return session;
	}
}
