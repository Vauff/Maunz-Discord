package com.vauff.maunzdiscord.core;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractMenuPage
{
	/**
	 * Contains all active menus by users with their currently shown pages (used for reactions)
	 */
	public static final HashMap<Long, AbstractMenuPage> ACTIVE = new HashMap<Long, AbstractMenuPage>();
	protected AbstractCommand<MessageReceivedEvent> cmd;
	protected AbstractMenuPage[] childPages = new AbstractMenuPage[9];
	protected Consumer<ReactionAddEvent>[] childConsumers = new Consumer[9];
	protected MessageReceivedEvent trigger;
	/**
	 * The menu message sent by the bot
	 */
	public IMessage menu;
	/**
	 * A representative interface that helps saving data across multiple menu pages
	 */
	protected IDataHandler handler;
	protected ScheduledFuture removeTimer;

	/**
	 * @param trigger The message event that triggered this menu
	 * @param cmd     The command that triggered this menu
	 */
	public AbstractMenuPage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd)
	{
		this.trigger = trigger;
		this.cmd = cmd;
	}

	/**
	 * @param trigger The message event that triggered this menu
	 */
	public AbstractMenuPage(MessageReceivedEvent trigger, AbstractCommand<MessageReceivedEvent> cmd, IDataHandler handler)
	{
		this.trigger = trigger;
		this.cmd = cmd;
		this.handler = handler;
	}

	/**
	 * @return The title of the menu. Line separators are added automatically
	 */
	public abstract String getTitle();

	/**
	 * Any \n in the returned string will be replaced with a call to {@link java.lang.System.lineSeparator()}
	 *
	 * @param channel The IChannel that the trigger originated in
	 * @return An optional text that is being shown between {@code getTitle()} and {@code getItems()}
	 */
	public String getText(IChannel channel)
	{
		return null;
	}

	/**
	 * @return An array where each entry represents the description of the menu item with the same index.
	 * Minimum length is 1, maximum length is 9. Line separators and prefixes ("**`[1]`**  |  ")
	 * are added automatically. Can be null and will be ignored if so
	 */
	public abstract String[] getItems();

	/**
	 * Gets called when a reply has been posted by the correct user after calling {@code waitForReply}
	 *
	 * @param event The event that contains the reply in question
	 */
	public void onReplied(MessageReceivedEvent event) throws Exception
	{
	}

	/**
	 * Sends a specific page to chat after deleting the previous menu and canceling its timer
	 *
	 * @param page The page to show
	 */
	public final void show(AbstractMenuPage page) throws Exception
	{
		AbstractCommand.AWAITED.remove(trigger.getAuthor().getStringID());
		removeTimer.cancel(false);
		menu.delete();
		page.show();
	}

	/**
	 * Sends this menu to the chat. Does NOT take care of canceling and deleting the previous timer + menu.
	 * Don't call unless it's the start page
	 */
	public void show() throws Exception
	{
		String items = "";

		for (int i = 0; i < getAmount(); i++)
		{
			items += "**`[" + (i + 1) + "]`**  |  " + getItems()[i] + System.lineSeparator();
		}

		ACTIVE.put(trigger.getAuthor().getLongID(), this);
		menu = Util.msg(trigger.getChannel(), trigger.getAuthor(), getTitle() + System.lineSeparator() + System.lineSeparator() + (getText(trigger.getChannel()) != null ? getText(trigger.getChannel()).replaceAll("\n", System.lineSeparator()) + System.lineSeparator() + System.lineSeparator() : "") + items);
		Util.addNumberedReactions(menu, true, getAmount());

		removeTimer = Executors.newScheduledThreadPool(1).schedule(() ->
		{
			if (!menu.isDeleted())
			{
				AbstractCommand.AWAITED.remove(trigger.getAuthor().getStringID());
				ACTIVE.remove(trigger.getAuthor().getLongID());
				menu.delete();
			}
		}, 120, TimeUnit.SECONDS);
	}

	/**
	 * Ends the existance of this menu. This will remove the user from any message queues,
	 * remove him from the active menu map, cancel any timeouts and delete the menu message
	 */
	public void end()
	{
		AbstractCommand.AWAITED.remove(trigger.getAuthor().getStringID());
		ACTIVE.remove(trigger.getAuthor().getLongID());

		if (removeTimer != null)
		{
			removeTimer.cancel(false);
		}

		if (menu != null)
		{
			menu.delete();
		}
	}

	/**
	 * Gets called when the user who initiated the menu reacts with something.
	 * Tries to find a valid page first. If childPages[item] is null, this method will try to execute a child function.
	 *
	 * @param event The event that triggered this reaction
	 * @param item  The selected item, 0-8. -1 if the user clicked X
	 */
	public final void onReacted(ReactionAddEvent event, int item) throws Exception
	{
		if (item < -1 || item > 8)
		{
			Main.log.warn("Tried to add a child with index " + item + ". Index needs to be between including -1 and 8");
			return;
		}

		if (item == -1)
		{
			end();
		}
		else if (childPages[item] != null)
		{
			show(childPages[item]);
		}
		else if (childConsumers[item] != null)
		{
			end();
			childConsumers[item].accept(event);
		}
		else
		{
			Main.log.warn("A menu item was selected (" + item + ") but a corresponding page or function could not be found.");
		}
	}

	/**
	 * Adds a child page for this page. A child defines something that is being called once a reaction is added.
	 * Adding all childs should be done in the constructor of this page. The array needs to be populated
	 * from smallest to biggest index.
	 *
	 * @param i    The place at which to add the child (1 will be the second child, meaning when selecting the
	 *             second options the page at this index 1 will be shown). This parameter can be between
	 *             0 (including) and 8 (including)
	 * @param page The page to add
	 */
	public final void addChild(int i, AbstractMenuPage page)
	{
		if (i < 0 || i > 8)
		{
			Main.log.warn("i was " + i + " when calling addChild on a menu page. Page was not added.");
			return;
		}

		childPages[i] = page;
	}

	/**
	 * Adds a child function for this page. A child defines something that is being called once a reaction is added.
	 * Adding all childs should be done in the constructor of this page. The array needs to be populated
	 * from smallest to biggest index.
	 *
	 * @param i    The place at which to add the child (1 will be the second child, meaning when selecting the
	 *             second options the page at this index 1 will be shown). This parameter can be between
	 *             0 (including) and 8 (including)
	 * @param page The page to add
	 */
	public final void addChild(int i, Consumer<ReactionAddEvent> f)
	{
		if (i < 0 || i > 8)
		{
			Main.log.warn("index was " + i + " when calling addChild on a menu page, but needs to be between 0 and 8. Page was not added.");
			return;
		}

		childConsumers[i] = f;
	}

	/**
	 * @return The amount of menu items
	 */
	protected final int getAmount()
	{
		return getItems() != null ? getItems().length : 0;
	}

	/**
	 * Sets up this menu to await a reply by the user who triggered this menu
	 *
	 * @param messageID The message which will get deleted afterwards
	 * @param userID    The user who triggered this command
	 */
	public void waitForReply(String messageID, String userID)
	{
		AbstractCommand.AWAITED.put(userID, new Await(messageID, cmd));
	}

	public interface IDataHandler
	{
	}
}
