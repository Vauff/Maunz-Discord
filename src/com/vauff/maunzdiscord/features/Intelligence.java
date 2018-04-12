package com.vauff.maunzdiscord.features;

import com.google.code.chatterbotapi.ChatterBotSession;
import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Intelligence extends AbstractCommand<MessageReceivedEvent>
{
	public static HashMap<String, CleverbotSession> sessions = new HashMap<>();
	public static HashMap<String, Timer> sessionTimers = new HashMap<>();

	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length != 1)
		{
			event.getChannel().setTypingStatus(true);
			Thread.sleep(250);

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
			Util.msg(event.getChannel(), event.getAuthor(), event.getAuthor().mention() + " " + chatSession.think(Util.addArgs(args, 1).replaceAll("\\<\\@[0-9]+\\>", "")));
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {
				"<@" + Main.client.getOurUser().getLongID() + ">",
				"<@!" + Main.client.getOurUser().getLongID() + ">"
		};
	}
}