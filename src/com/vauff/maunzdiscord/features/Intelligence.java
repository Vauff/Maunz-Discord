package com.vauff.maunzdiscord.features;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.google.code.chatterbotapi.ChatterBotSession;

import com.vauff.maunzdiscord.features.CleverbotSession;
import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Intelligence extends AbstractCommand<MessageReceivedEvent>
{
	public static HashMap<String, CleverbotSession> sessions = new HashMap<String, CleverbotSession>();
	public static HashMap<String, Timer> sessionTimers = new HashMap<String, Timer>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		try
		{
			String[] message = event.getMessage().getContent().split(" ");
			ChatterBotSession chatSession;
			CleverbotSession session;

			if (!sessions.containsKey(event.getChannel().getName()))
			{
				session = new CleverbotSession();
				sessions.put(event.getChannel().getName(), session);

				TimerTask timerTask = new TimerTask()
				{
					@Override
					public void run()
					{
						sessions.remove(event.getChannel().getName());
						Intelligence.sessionTimers.get(event.getChannel().getName()).cancel();
						Intelligence.sessionTimers.remove(event.getChannel().getName());
					}
				};

				sessionTimers.put(event.getChannel().getName(), new Timer());
				sessionTimers.get(event.getChannel().getName()).schedule(timerTask, 900000);
			}
			else
			{
				session = sessions.get(event.getChannel().getName());

				TimerTask timerTask = new TimerTask()
				{

					@Override
					public void run()
					{
						sessions.remove(event.getChannel().getName());
						Intelligence.sessionTimers.get(event.getChannel().getName()).cancel();
						Intelligence.sessionTimers.remove(event.getChannel().getName());

					}
				};

				sessionTimers.get(event.getChannel().getName()).cancel();
				sessionTimers.put(event.getChannel().getName(), new Timer());
				sessionTimers.get(event.getChannel().getName()).schedule(timerTask, 900000);
			}

			chatSession = session.getSession();
			Util.msg(event.getChannel(), event.getAuthor(), event.getAuthor().mention() + " " + chatSession.think(Util.addArgs(message, 1).replaceAll("\\<\\@[0-9]+\\>", "")));
		}
		catch (Exception e)
		{
			Main.log.error("", e);
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "<@" + Main.client.getOurUser().getLongID() + ">" };
	}
}