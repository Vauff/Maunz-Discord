package com.vauff.maunzdiscord.threads;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.timers.ServerTimer;
import org.bson.Document;

public class ServerProcessThread implements Runnable
{
	private Document doc;
	private Thread thread;
	private String id;

	public ServerProcessThread(Document doc, String id)
	{
		this.doc = doc;
		this.id = id;
	}

	public void start()
	{
		if (thread == null)
		{
			thread = new Thread(this, "servertracking-" + id);
			thread.start();
		}
	}

	public void run()
	{
		try
		{
			wait();
		}
		catch (Exception e)
		{
			Logger.log.error("", e);
		}
		finally
		{
			ServerTimer.threadRunning.put("servertracking-" + id, false);
		}
	}
}
