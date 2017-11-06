# Maunz-Discord

[![Version](https://badge.fury.io/gh/Vauff%2FMaunz-Discord.svg)](https://badge.fury.io/gh/Vauff%2FMaunz-Discord) [![Invite Link](https://img.shields.io/badge/add%20bot%20on-Discord-7289da.svg)](https://discordapp.com/oauth2/authorize?&client_id=230780946142593025&scope=bot)

Maunz is a Discord bot created by Vauff written in Java using the Discord4J library. You can add Maunz to your guild by clicking on the Discord badge above. This project is a Discord port of my [IRC bot Maunz](https://github.com/Vauff/Maunz). If you'd like to help Maunz development feel free to suggest ideas on [Trello](https://trello.com/b/9W7PmTvX/maunz) in the features list, or if you find any, even bugs in the bugs list :(

# Commands

Italic ones are permission restricted commands to either just Vauff or guild administrators. [] indicates that the argument is optional, \<> indicates the argument is required.

*about - Gives information about Maunz such as version and uptime.

*accinfo \<username> - Gives you full information about any Minecraft account.

*benchmark \<gpu/cpu> - Provides complete benchmark information on a GPU or CPU powered by PassMark.

*changelog [version] - Tells you the changelog of the Maunz version you specify.

_*disable_ - Disables Maunz.

_*enable_ - Enables Maunz.

*help [command] - Links you to the README or gives command help if a command is given. Please note that command specific help defaults to channel syntax by default.

*isitdown \<hostname> - Tells you if the given website is down or not.

*map - Tells you which map a server is playing outside of its standard map tracking channel.

*notify \<list/wipe/mapname> - Lets you list, add or remove your server map notifications.

*ping - Makes Maunz respond to you with pong. Very useful for testing your connection!

*players - Lists the current players online on a server (in a PM).

*quote <view/list/add> <quoteid>/[page] - Allows you to view chat quotes.

*reddit \<subreddit> - Links you to a subreddit that you provide.

_*restart_ - Restarts Maunz.

_*say [channel] <message>_ - Makes Maunz say whatever you want her to!

_*services_ - Opens an interface for enabling specific services on a guild.

*source - Links you to the GitHub page of Maunz, you can submit issues/pull requests here.

*steam \<steamid> - Links you to a Steam profile based on a Steam ID.

_*stop_ - Stops Maunz.

*trello - Links you to the Trello board of Maunz. Feature requests and bug reports can be made here.

# Creating a Fork

For the most part, the bot should be ready to go with a simple clone and compile in a Maven supported environment. However, you will need to create a file called Passwords.java in the com.vauff.maunzdiscord.core package with the following template.

```java
package com.vauff.maunzdiscord.core;

public class Passwords
{
	public static final String discordToken = "";
	public static final String discordDevToken = "";
	public static final String database = "";
}
```

In discordToken you need to input the token for a bot user you've created at the [Discord API page](https://discordapp.com/developers/applications/me). discordDevToken can be left blank as long as you aren't going to use the dev mode feature, and database can be left blank if you aren't going to use the *quote command.