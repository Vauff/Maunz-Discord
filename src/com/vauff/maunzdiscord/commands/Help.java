package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Help extends AbstractCommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length == 1)
		{
			Util.msg(event.getChannel(), "Help documents are located at https://github.com/Vauff/Maunz-Discord/blob/master/README.md");
		}
		else
		{
			Util.msg(event.getChannel(), cmdHelp(args));
		}
	}

	/**
	 * Returns a help string corresponding to the command prefix at index 1 in the given array
	 * @param args The string array containing the command prefix of which to get the help string
	 * @return The help string corresponding to the command prefix
	 */
	public String cmdHelp(String[] args)
	{
		switch (args[1].toLowerCase().replace("*", ""))
		{
		case "about":
			return "Gives information about Maunz such as version and uptime. **Usage: *about**";
		case "accinfo":
			return "Shows information about a Minecraft account. **Usage: *accinfo <username>**";
		case "benchmark":
			return "Provides complete benchmark information on a GPU or CPU powered by PassMark. **Usage: *benchmark <gpu/cpu>**";
		case "changelog":
			return "Tells you the changelog of the Maunz version you specify. **Usage: *changelog [version]**";
		case "disable":
			return "Disables Maunz, only usable by Vauff. **Usage: *disable**";
		case "enable":
			return "Enables Maunz, only usable by Vauff. **Usage: *enable**";
		case "help":
			return "Links you to the README or gives command help if a command is given. Please note that command specific help defaults to channel syntax by default. **Usage: *help [command]**";
		case "isitdown":
			return "Tells you if the given website is down or not. **Usage: *isitdown <hostname>**";
		case "map":
			return "Tells you which map a server is playing outside of its standard map tracking channel. **Usage: *map**";
		case "notify":
			return "Lets you list, add or remove your server map notifications. **Usage: *notify <list/wipe/mapname>**";
		case "ping":
			return "Makes Maunz respond to you with pong. Very useful for testing ping to the IRC server! **Usage: *ping**";
		case "players":
			return "Lists the current players online on a server (in a PM). **Usage: *players**";
		case "reddit":
			return "Links you to a subreddit that you provide. **Usage: *reddit <subreddit>**";
		case "restart":
			return "Restarts Maunz, only usable by Vauff. **Usage: *restart**";
		case "say":
			return "Makes Maunz say whatever you want her to! **Usage: *say [channel] <message>**";
		case "services":
			return "Opens an interface for enabling specific services on a guild. only usable by guild administrators. **Usage: *services**";
		case "source":
			return "Links you to the GitHub page of Maunz, you can submit issues/pull requests here. **Usage: *source**";
		case "steam":
			return "Links you to a Steam profile based on a Steam ID. **Usage *steam <steamid>**";
		case "stop":
			return "Stops Maunz, only usable by Vauff. **Usage: *stop**";
		case "trello":
			return "Links you to the Trello board of Maunz. Feature requests and bug reports can be made here. **Usage: *trello**";
		default:
			return "I don't recognize the command " + args[1] + "!";
		}
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "*help" };
	}
}