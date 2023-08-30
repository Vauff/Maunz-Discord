package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;

/**
 * Created by Ramon on 03-Apr-17.
 */

public class IsItDown extends AbstractCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, MessageChannel channel, User user) throws Exception
	{
		String hostname = event.getInteraction().getCommandInteraction().get().getOption("hostname").get().getValue().get().asString();
		boolean isUp;
		String cleanedUri = hostname.replaceAll("(^\\w+:|^)\\/\\/", "").split("/")[0];
		int port;

		try
		{
			URI uri = new URI("my://" + cleanedUri);
			String host = uri.getHost();

			if (uri.getPort() == -1)
			{
				port = getPortByProtocol(hostname);
			}
			else
			{
				port = uri.getPort();
			}

			if (host == null)
			{
				Util.editReply(event, "Please specify a valid hostname or URI.");
				return;
			}

			isUp = pingHost(host, port, cleanedUri);
		}
		catch (Exception e)
		{
			Util.editReply(event, "Please specify a valid hostname or URI.");
			return;
		}

		Util.editReply(event, (isUp ? ":white_check_mark:" : ":x:") + "**  |  " + cleanedUri + "** is currently **" + (isUp ? "UP**" : "DOWN**"));
	}

	/**
	 * Pings a host at a specific port. The ping will be deemed unsuccessful if the socket couldn't connect
	 * to the host within the given timeframe
	 *
	 * @param host The host to ping
	 * @param port The port to ping the host at
	 * @return true if the connection was successful, false otherwise (aka the socket could not connect to the host/port after timeout amount of milliseconds)
	 */
	private boolean pingHost(String host, int port, String uri) throws Exception
	{
		Socket socket = new Socket();

		try
		{
			socket.connect(new InetSocketAddress(host, port), 4000);

			if (port == 80 || port == 443)
			{
				URL url = new URL((port == 80 ? "http" : "https") + "://" + uri);
				HttpURLConnection connection = (port == 80 ? (HttpURLConnection) url.openConnection() : (HttpsURLConnection) url.openConnection());

				connection.setRequestMethod("GET");
				connection.setRequestProperty("User-Agent", Main.cfg.getFakeUserAgent());
				connection.connect();

				int divCode = connection.getResponseCode() / 100;

				// 2xx or 3xx HTTP status codes
				if (divCode == 2 || divCode == 3)
					return true;
				else
					return false;
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
	public ApplicationCommandRequest getCommandRequest()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Tells you if the given hostname is down or not")
			.addOption(ApplicationCommandOptionData.builder()
				.name("hostname")
				.description("Hostname to ping")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.required(true)
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "isitdown";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}
