package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Ramon on 03-Apr-17.
 */

public class IsItDown extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length == 1)
		{
			Util.msg(event.getChannel(), "You need to specify an argument! **Usage: *isitdown <hostname>**");
		}
		else
		{
			boolean isUp;
			String hostname = args[1].replaceAll("^https?:\\/\\/", "").split("/")[0];

			if (args[1].startsWith("https"))
			{
				isUp = pingHost(hostname, 443, 4000);
			}
			else
			{
				isUp = pingHost(hostname, 80, 4000);
			}

			Util.msg(event.getChannel(), (isUp ? ":white_check_mark:" : ":x:") + "**  |  " + hostname + "** is currently **" + (isUp ? "UP**" : "DOWN**"));
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*isitdown" };
	}

	/**
	 * Pings a host at a specific port. The ping will be deemed unsuccessful if the socket couldn't connect
	 * to the host within the given timeframe
	 * @param host The host to ping
	 * @param port The port to ping the host at
	 * @param timeout The timeout in milliseconds after which the ping will be deemed unsuccessful
	 * @return true if the connection was successful, false otherwise (aka the socket could not connect to the host/port after timeout amount of milliseconds
	 */
	private static boolean pingHost(String host, int port, int timeout)
	{
		Socket socket = new Socket();

		try
		{
			socket.connect(new InetSocketAddress(host, port), timeout);
			return true;
		}
		catch (IOException e)
		{
			return false; // Either timeout, unreachable or failed DNS lookup.
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
