package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.commands.templates.CommandHelp;
import com.vauff.maunzdiscord.commands.templates.SubCommandHelp;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

/**
 * Created by Ramon on 03-Apr-17.
 */

public class IsItDown extends AbstractCommand<MessageCreateEvent>
{
	@Override
	public void exe(MessageCreateEvent event, MessageChannel channel, User author) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length == 1)
		{
			Util.msg(channel, author, "You need to specify an argument! **Usage: *isitdown <hostname>**");
		}
		else
		{
			boolean isUp;
			URI uri;
			String origUri = args[1];
			String cleanedUri = args[1].replaceAll("(^\\w+:|^)\\/\\/", "").split("/")[0];
			String host;
			int port;

			try
			{
				uri = new URI("my://" + cleanedUri);

				host = uri.getHost();

				if (uri.getPort() == -1)
				{
					port = getPortByProtocol(origUri);
				}
				else
				{
					port = uri.getPort();
				}

				if (host == null)
				{
					Util.msg(channel, author, "Please specify a valid hostname or URI.");
					return;
				}

				isUp = pingHost(host, port, cleanedUri);

				Util.msg(channel, author, (isUp ? ":white_check_mark:" : ":x:") + "**  |  " + cleanedUri + "** is currently **" + (isUp ? "UP**" : "DOWN**"));
			}
			catch (Exception e)
			{
				Util.msg(channel, author, "Please specify a valid hostname or URI.");
			}
		}
	}

	/**
	 * Pings a host at a specific port. The ping will be deemed unsuccessful if the socket couldn't connect
	 * to the host within the given timeframe
	 *
	 * @param host The host to ping
	 * @param port The port to ping the host at
	 * @return true if the connection was successful, false otherwise (aka the socket could not connect to the host/port after timeout amount of milliseconds
	 */
	private static boolean pingHost(String host, int port, String uri) throws Exception
	{
		Socket socket = new Socket();

		try
		{
			socket.connect(new InetSocketAddress(host, port), 4000);

			if (port == 80 || port == 443)
			{
				Jsoup.connect((port == 80 ? "http" : "https") + "://" + uri).userAgent(Util.getUserAgent()).get();
			}

			return true;
		}
		catch (IOException e)
		{
			return false; // Either timeout, unreachable, failed DNS lookup, or bad HTTP status code.
		}
		finally
		{
			socket.close();
		}
	}

	/**
	 * Finds the appropriate port number for the given connection protocol
	 *
	 * @param uri The host to determine the port number for
	 * @return 443 if the protocol is https, 21 if the protocol is ftp, and 80 otherwise
	 */
	private int getPortByProtocol(String uri)
	{
		if (uri.startsWith("https"))
		{
			return 443;
		}

		if (uri.startsWith("ftp"))
		{
			return 21;
		}

		// Assume http
		return 80;
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*isitdown" };
	}

	@Override
	public int getPermissionLevel()
	{
		return 0;
	}

	@Override
	public CommandHelp getHelp()
	{
		return new CommandHelp(getAliases(), new SubCommandHelp[] { new SubCommandHelp("<hostname>", "Tells you if the given hostname is down or not.") });
	}
}
