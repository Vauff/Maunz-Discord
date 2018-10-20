package com.vauff.maunzdiscord.commands.servicesmenu.delete;

import com.vauff.maunzdiscord.core.AbstractCommand;
import com.vauff.maunzdiscord.core.AbstractMenuPage;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

public class DeleteConfirmationPage extends AbstractMenuPage
{
	private String serviceToDelete;

	public DeleteConfirmationPage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd, String std)
	{
		super(trigger, cmd);

		serviceToDelete = std;
	}

	@Override
	public final String getTitle()
	{
		return ":x:  |  **Delete Existing Service: " + serviceToDelete + "**";
	}

	@Override
	public final String getText(IChannel channel)
	{
		return "Are you sure you would like to delete this service? If you have more than one service added manually by Vauff, please be aware that this will delete all of them\n\n**WARNING:** This will delete your service data **permanently**";
	}

	@Override
	public String[] getItems()
	{
		return new String[] {
				"Yes, delete the service permanently",
				"No, keep the service"
		};
	}
}
