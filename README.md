# Maunz-Discord

![Version](https://img.shields.io/github/release/Vauff/Maunz-Discord.svg?color=4CC61E&label=version) [![Add Bot to Server](https://img.shields.io/badge/add%20bot%20on-Discord-7289da.svg)](https://discord.com/api/oauth2/authorize?client_id=230780946142593025&permissions=517647752257&scope=bot%20applications.commands)

Maunz is a multi-purpose Discord bot with a focus on Source server tracking, it is developed by Vauff using the Discord4J library. You can add Maunz to your Discord server by clicking on the add bot badge above. If you'd like to suggest features, report bugs, receive assistance on using the bot or just follow development you're free to join the official [Maunz Discord server](https://discord.gg/v55fW9b).

# Commands

[] indicates that the argument is optional, \<> indicates the argument is required.

## Public

`/about` - Gives information about Maunz such as version and uptime

`/benchmark <query>` - Provides complete benchmark information on a GPU or CPU powered by PassMark

`*changelog [version]` - Shows you the changelog of the Maunz version you specify

`/colour [image] [link]` - Returns the average RGB and HTML/Hex colour codes of an image attachment or link

`/invite` - Provides invite links to the Maunz Hub server, and to add Maunz to your own server

`*help [page]` - Lists all the available bot commands and the syntax for using each

`*help <command>` - Gives you help on how to use a specific command

`/isitdown <hostname>` - Tells you if the given hostname is down or not

`/map [mapname]` - Tells you info about the current map on a server, or one played in the past

`/minecraft <account>` - Gives you full information about a Minecraft account

`*notify list [page]` - Lists your current map notifications

`*notify wipe` - Wipes ALL of your map notifications

`*notify <mapname>` - Adds or removes a map to/from your map notifications, wildcard characters are also supported here

`/ping` - Makes Maunz respond with pong

`/players` - Lists the current players online on a server

`/reddit <subreddit>` - Links you to the subreddit name that you provide

`/steam <steamid>` - Gives you full information about a Steam account

## Server Admin

`/say <message> [channel]` - Sends a custom message from Maunz to any channel

`/services add <ip> [channel]` - Add a new service

`/services list [page]` - List current services

`/services info <id>` - View full info about a specific service

`/services delete <id>` - Delete a service

`/services edit <id> [ip] [channel]` - Edit a service value (only pick what you're changing)

`/services toggle <id> <option> <value>` - Toggle a service value

## Bot Admin

`/stop` - Shuts down the bot