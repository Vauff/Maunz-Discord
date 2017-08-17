package com.vauff.maunzdiscord.core;

public class Await
{
	private String userID;
	private AbstractCommand command;
	
	public Await(String uID, AbstractCommand cmd)
	{
		userID = uID;
		command = cmd;
	}
	
	public String getUserID()
	{
		return userID;
	}
	
	public AbstractCommand getCommand()
	{
		return command;
	}
}
