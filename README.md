# Maunz-Discord

[![Version](https://badge.fury.io/gh/Vauff%2FMaunz-Discord.svg)](https://badge.fury.io/gh/Vauff%2FMaunz-Discord) [![Dependencies](https://www.versioneye.com/user/projects/58068becc3e528003890dfb8/badge.svg)](https://www.versioneye.com/user/projects/58068becc3e528003890dfb8)

Maunz-Discord is a Discord bot created by Vauff in Java using Discord4J. She is currently only on the GFL ZE discord server. This project is a Discord port of my [IRC bot Maunz](https://github.com/Vauff/Maunz). If you want to help Maunz development feel free to suggest ideas on [Trello](https://trello.com/b/9W7PmTvX/maunz) in the features or if you find any, even bugs in the bugs list :(

# Commands

Italic ones cannot be done by everyone and are restricted to just me. [] indicates that the argument is optional, \<> indicates the argument is required.

*about - Gives information about Maunz such as version and uptime.

_*disable_ - Disables Maunz.

_*enable_ - Enables Maunz.

*help \[command] - Links you to the README or gives command help if a command is given. Please note that command specific help defaults to channel syntax by default.

*notify \<list/confirm/mapname> - Lets you list, add or remove your ZE map notifications.

*map - Tells you which map GFL ZE is playing outside of the normal #map-tracking channel.

*ping - Makes Maunz respond to you with pong. Very useful for testing ping to the server!

_*restart_ - Restarts Maunz.

*source - Links you to the GitHub page of Maunz, you can submit issues/pull requests here.

_*stop_ - Stops Maunz.

*trello - Links you to the Trello board of Maunz. Feature requests and bug reports can be made here.

# Dependencies

Maunz depends on some java libraries to function, most are Discord4J dependencies, but some are mine that I've added for some features. These can be downloaded manually or automatically with Maven using Maunz's pom.xml. They are with their versions listed below.

[Apache Commons Codec](https://commons.apache.org/proper/commons-codec/) - 1.10

[Apache Commons Lang](https://commons.apache.org/proper/commons-lang/) - 3.5

[Apache Commons IO](https://commons.apache.org/proper/commons-io/) - 2.5

[Apache Commons Logging](https://commons.apache.org/proper/commons-logging/) - 1.2

[log4j-api](http://logging.apache.org/log4j/2.x/) - 2.8.2

[log4j-core](http://logging.apache.org/log4j/2.x/) - 2.8.2

[log4j-slf4j-impl](https://logging.apache.org/log4j/2.0/log4j-slf4j-impl/index.html) - 2.8.2

[slf4j-api](http://www.slf4j.org/) - 1.7.25

[jsoup](https://jsoup.org/) - 1.10.2

[Discord4J](https://github.com/austinv11/Discord4J) - 2.7.0

[gson](https://github.com/google/gson) - 2.8.0

[hamcrest-core](http://hamcrest.org/JavaHamcrest/) - 1.3

[httpclient](https://hc.apache.org/httpcomponents-client-ga/) - 4.5.3

[httpcore](https://hc.apache.org/httpcomponents-core-ga/) - 4.4.6

[httpmime](https://hc.apache.org/httpcomponents-client-ga/index.html) - 4.5.3

[jetty-io](http://www.eclipse.org/jetty/) - 9.4.3.v20170317

[jetty-util](http://www.eclipse.org/jetty/) - 9.4.3.v20170317

[jetty-client](http://www.eclipse.org/jetty/) - 9.4.3.v20170317

[jetty-http](http://www.eclipse.org/jetty/) - 9.4.3.v20170317

[jflac](http://jflac.sourceforge.net/) - 1.3

[jlayer](http://www.javazoom.net/javalayer/javalayer.html) - 1.0.1-2

[jna](https://github.com/java-native-access/jna) - 4.4.0

[jorbis](http://www.jcraft.com/jorbis/) - 0.0.17

[junit](http://junit.org/junit4/) - 4.12

[mp3spi](http://www.javazoom.net/mp3spi/mp3spi.html) - 1.9.5-2

[tritonus-dsp](http://www.tritonus.org/) - 0.3.6

[tritonus-share](http://www.tritonus.org/) - 0.3.7-3

[typetools](https://github.com/jhalterman/typetools) - 0.4.9

[websocket-api](https://www.eclipse.org/jetty/) - 9.4.3.v20170317

[websocket-client](https://www.eclipse.org/jetty/) - 9.4.3.v20170317

[websocket-common](https://www.eclipse.org/jetty/) - 9.3.14.v20161028
