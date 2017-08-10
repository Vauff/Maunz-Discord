package com.vauff.maunzdiscord.features;

import com.vauff.maunzdiscord.core.Main;

import sx.blah.discord.handle.obj.IGuild;

public class StatsTimer
{
	public static Runnable timer = new Runnable()
	{
		public void run()
		{
			try
			{
				int users = 0;
				
				for (IGuild guild : Main.client.getGuilds())
				{
					users = users + guild.getTotalMemberCount();
				}
				
				Main.client.changePlayingText(Main.client.getGuilds().size() + " guilds, " + users + " users");
			}
			catch (Exception e)
			{
				Main.log.error("", e);
			}
		}
	};
}
