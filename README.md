# Maunz-Discord

<img align="right" width="200" height="200" src="https://github.com/Vauff/Maunz-Discord/assets/6075172/f46345b6-8039-413c-b2e6-873a618c2c72">

[![Version](https://img.shields.io/github/release/Vauff/Maunz-Discord.svg?color=4CC61E&label=version)](https://github.com/Vauff/Maunz-Discord/releases/latest) [![Add Bot to Server](https://img.shields.io/badge/add%20bot%20on-Discord-7289da.svg)](https://discord.com/api/oauth2/authorize?client_id=230780946142593025&permissions=517647752257&scope=bot%20applications.commands)

Maunz is a multi-purpose Discord bot with a focus on Source server tracking, developed by Vauff using the Discord4J library. Maunz features chronological server map tracking in specific channels, and on-demand tracking in others via commands. Administrators can add/remove unlimited servers, while users can configure personal notifications for specific maps.

Join the official [Maunz Hub](https://discord.gg/v55fW9b) Discord server if you'd like to discuss the bot, suggest features, report bugs, receive assistance, or just follow along development.

# Running the Bot

If you choose to self-host your own instance of the bot, the only requirements are Java 21 and MongoDB. You can download the latest .jar [here](https://github.com/Vauff/Maunz-Discord/releases/latest), running it with the following parameters is recommended:
```
java -XX:-OmitStackTraceInFastThrow -Djava.net.preferIPv4Stack=true -jar Maunz-Discord.jar
```
The bot will auto-create a `config.json` file on first start, and prompt you to supply a token from an app created in the [Discord Developer Portal](https://discord.com/developers/applications). You will also be prompted to supply a MongoDB connection string and database name, see [this MongoDB manual page](https://www.mongodb.com/docs/manual/reference/connection-string/) on how to format a connection string.

If you make your own edits and run them on a Discord bot accessible to other users, you **must** open source your changes to remain compliant with Maunz's AGPLv3 license.

# Links

- [Bot invite](https://discord.com/api/oauth2/authorize?client_id=230780946142593025&permissions=517647752257&scope=bot%20applications.commands)
- [Maunz Hub Discord server invite](https://discord.gg/v55fW9b)
- [Download](https://github.com/Vauff/Maunz-Discord/releases/latest)
- [Maunz-Migrator](https://github.com/Vauff/Maunz-Migrator)
- [Maunz-Web](https://github.com/Vauff/Maunz-Web)

# Commands

[] indicates that the argument is optional, \<> indicates the argument is required.

### Public

`/about` - Gives information about Maunz such as version and uptime

`/benchmark <query>` - Provides complete benchmark information on a GPU or CPU powered by PassMark

`/changelog [version]` - Shows the changelog of a Maunz version

`/colour [image] [link]` - Returns the average RGB and HTML/Hex colour codes of an image attachment or link

`/invite` - Provides invite links to the Maunz Hub server, and to add Maunz to your own server

`/help list [page]` - Lists all the available bot commands and the syntax for using each

`/help view <command>` - View a specific commands syntax

`/isitdown <hostname>` - Tells you if the given hostname is down or not

`/map [mapname]` - Tells you info about the current map on a server, or one played in the past

`/minecraft <account>` - Gives you full information about a Minecraft account

`/notify toggle <mapname>` - Add or removes a map to/from your map notifications

`/notify list [page]` - Lists your current map notifications

`/notify wipe` - Wipes ALL of your map notifications

`/ping` - Makes Maunz respond with pong

`/players` - Lists the current players online on a server

`/reddit <subreddit>` - Links you to the subreddit name that you provide

`/steam <steamid>` - Gives you full information about a Steam account

### Server Admin

`/say <message> [channel]` - Sends a custom message from Maunz to any channel

`/servers add <ip> <channel>` - Add a new server to track

`/servers list [page]` - List current servers

`/servers delete <id>` - Delete a server

### Bot Admin

`/stop` - Shuts down the bot
