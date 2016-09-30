package com.vauff.maunzdiscord.core;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.vauff.maunzdiscord.features.GFLTimer;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;

public class ReadyEventListener
{
	@EventSubscriber
	public void onReady(ReadyEvent event)
	{
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(GFLTimer.timer, 0, 60, TimeUnit.SECONDS);
	}
}
