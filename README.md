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

*ping - Makes Maunz respond to you with pong. Very useful for testing ping to the server!

*players - Lists the current players online on a server (in a PM).

*reddit \<subreddit> - Links you to a subreddit that you provide.

_*restart_ - Restarts Maunz.

_*services_ - Opens an interface for enabling specific services on a guild.

*source - Links you to the GitHub page of Maunz, you can submit issues/pull requests here.

*steam \<steamid> - Links you to a Steam profile based on a Steam ID.

_*stop_ - Stops Maunz.

*trello - Links you to the Trello board of Maunz. Feature requests and bug reports can be made here.

# Dependencies

Maunz depends on some java libraries to function, most are Discord4J dependencies, but some are mine that I've added for some features. These can be downloaded manually or automatically with Maven using Maunz's pom.xml. They are with their versions listed below.

[Apache Commons Codec](https://commons.apache.org/proper/commons-codec/) - 1.10

[Apache Commons Lang](https://commons.apache.org/proper/commons-lang/) - 3.6

[Apache Commons IO](https://commons.apache.org/proper/commons-io/) - 2.5

[Apache Commons Logging](https://commons.apache.org/proper/commons-logging/) - 1.2

[Apache Commons Compress](https://commons.apache.org/proper/commons-compress/) - 1.14

[log4j-api](http://logging.apache.org/log4j/2.x/) - 2.9.1

[log4j-core](http://logging.apache.org/log4j/2.x/) - 2.9.1

[log4j-slf4j-impl](https://logging.apache.org/log4j/2.0/log4j-slf4j-impl/index.html) - 2.9.1

[slf4j-api](http://www.slf4j.org/) - 1.7.25

[jsoup](https://jsoup.org/) - 1.10.3

[Discord4J](https://github.com/austinv11/Discord4J) - 2.9.1

[hamcrest-core](http://hamcrest.org/JavaHamcrest/) - 1.3

[httpclient](https://hc.apache.org/httpcomponents-client-ga/) - 4.5.3

[httpcore](https://hc.apache.org/httpcomponents-core-ga/) - 4.4.7

[httpmime](https://hc.apache.org/httpcomponents-client-ga/index.html) - 4.5.3

[jetty-io](http://www.eclipse.org/jetty/) - 9.4.7.v20170914

[jetty-util](http://www.eclipse.org/jetty/) - 9.4.7.v20170914

[jetty-client](http://www.eclipse.org/jetty/) - 9.4.7.v20170914

[jetty-http](http://www.eclipse.org/jetty/) - 9.4.7.v20170914

[jflac](http://jflac.sourceforge.net/) - 1.3

[jlayer](http://www.javazoom.net/javalayer/javalayer.html) - 1.0.1-2

[jna](https://github.com/java-native-access/jna) - 4.5.0

[jorbis](http://www.jcraft.com/jorbis/) - 0.0.17

[junit](http://junit.org/junit4/) - 4.12

[mp3spi](http://www.javazoom.net/mp3spi/mp3spi.html) - 1.9.5-2

[tritonus-dsp](http://www.tritonus.org/) - 0.3.6

[tritonus-share](http://www.tritonus.org/) - 0.3.7-3

[typetools](https://github.com/jhalterman/typetools) - 0.5.0

[websocket-api](https://www.eclipse.org/jetty/) - 9.4.7.v20170914

[websocket-client](https://www.eclipse.org/jetty/) - 9.4.7.v20170914

[websocket-common](https://www.eclipse.org/jetty/) - 9.4.7.v20170914

[emoji-java](https://github.com/vdurmont/emoji-java) - 3.3.0

[json](https://github.com/stleary/JSON-java) - 20170516

[koloboke-impl-common-jdk8](https://github.com/leventov/Koloboke) - 1.0.0

[koloboke-api-jdk8](https://github.com/leventov/Koloboke) - 1.0.0

[jackson-module-afterburner](https://github.com/FasterXML/jackson-modules-base) - 2.9.1

[jackson-core](https://github.com/FasterXML/jackson-core) - 2.9.1

[jackson-databind](https://github.com/FasterXML/jackson-databind) - 2.9.1

[jackson-annotations](https://github.com/FasterXML/jackson-annotations) - 2.9.1

[steam-condenser](https://github.com/koraktor/steam-condenser-java) - 1.3.9

[pircbot](http://www.jibble.org/pircbot.php) - 1.5.0