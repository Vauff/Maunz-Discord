package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Util;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

public class Help implements ICommand<MessageReceivedEvent>
{
	@Override
	public void exe(MessageReceivedEvent event) throws Exception
	{
		String[] args = event.getMessage().getContent().split(" ");

		if (args.length == 1)
		{
			Util.msg(event.getMessage().getChannel(), "Help documents are located at https://github.com/Vauff/Maunz-Discord/blob/master/README.md");
		}
		else
		{
			Util.msg(event.getMessage().getChannel(), cmdHelp(args));
		}
	}

	public String cmdHelp(String[] args)
	{
		switch (args[1].toLowerCase())
		{
		case "*about":
		case "about":
			return "Gives information about Maunz such as version and uptime. **Usage: *about**";
		case "*help":
		case "help":
			return "Links you to the README or gives command help if a command is given. Please note that command specific help defaults to channel syntax by default. **Usage: *help [command]**";
		case "*map":
		case "map":
			return "Tells you which map GFL ZE is playing outside of the normal #map-tracking channel. **Usage: *map**";
		case "*notify":
		case "notify":
			return "Lets you list, add or remove your ZE map notifications. **Usage: *notify <list/confirm/mapname>**";
		case "*ping":
		case "ping":
			return "Makes Maunz respond to you with pong. Very useful for testing ping to the IRC server! **Usage: *ping**";
		case "*restart":
		case "restart":
			return "Restarts Maunz, only usable by Vauff. **Usage: *restart**";
		case "*stop":
		case "stop":
			return "Stops Maunz, only usable by Vauff. **Usage: *stop**";
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