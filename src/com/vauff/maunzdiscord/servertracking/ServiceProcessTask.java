package com.vauff.maunzdiscord.servertracking;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import com.vauff.maunzdiscord.core.Util;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.http.client.ClientException;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static com.mongodb.client.model.Filters.eq;

public class ServiceProcessTask implements Runnable {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final ObjectId id;
    private final Document serverDoc;
    private final Guild guild;
    private Document doc;
    private MessageChannel channel;
    private boolean channelFetched = false;

    public ServiceProcessTask(ObjectId id, Document serverDoc, Guild guild) {
        this.id = id;
        this.serverDoc = serverDoc;
        this.guild = guild;
    }

    @Override
    public void run() {
        try {
            doc = Main.mongoDatabase.getCollection("services").find(eq("_id", id)).first();

            if (Objects.isNull(doc) || !doc.getBoolean("enabled"))
                return;

            boolean showServerName = doc.getBoolean("alwaysShowName") || Util.isMultiTrackingChannel(doc.getLong("guildID"), doc.getLong("channelID"));
            String msgServerName = "The server";

            if (showServerName)
                msgServerName = "**" + Util.escapeString(serverDoc.getString("name")) + "**";

            if (serverDoc.getInteger("downtimeTimer") >= serverDoc.getInteger("failedConnectionsThreshold") && doc.getBoolean("online")) {
                send(msgServerName + " has gone offline");
                setField("online", false);
            }

            if (serverDoc.getInteger("downtimeTimer") >= ServerTrackingLoop.MAX_DOWNTIME) {
                send(msgServerName + " has now been offline for a week and its server tracking was automatically disabled, it can be re-enabled by a guild administrator using **/servers toggle <id> enabled**");
                setField("enabled", false);
            }

            if (serverDoc.getInteger("downtimeTimer") >= 1)
                return;

            if (!doc.getBoolean("online")) {
                send(msgServerName + " has come back online");
                setField("online", true);
            }

            String map = serverDoc.getString("map");

            if (map.isEmpty() || doc.getString("lastMap").equalsIgnoreCase(map))
                return;

            String url = MapImages.getMapImageURL(map, serverDoc.getString("appId"));
            String ipPort = serverDoc.getString("ip") + ":" + serverDoc.getInteger("port");

            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(MapImages.getMapImageColour(url))
                    .timestamp(Instant.ofEpochMilli(serverDoc.getLong("timestamp")))
                    .description("Now Playing: **" + Util.escapeString(map) + "**\nPlayers Online: **" + serverDoc.getInteger("playerCount") + "/" + serverDoc.getInteger("maxPlayers") + "**\nQuick Join: **[" + ipPort + "](https://vauff.com/connect.php?ip=" + ipPort + ")**")
                    .build();

            if (!url.isEmpty())
                embed = embed.withThumbnail(url);

            EmbedCreateSpec titledEmbed = embed.withTitle(Util.escapeString(serverDoc.getString("name")));

            if (showServerName)
                send(titledEmbed);
            else
                send(embed);

            setField("lastMap", map);

            parentLoop:
            for (Document notificationDoc : doc.getList("notifications", Document.class)) {
                for (String mapNotification : notificationDoc.getList("notifications", String.class)) {
                    if (wildcardMatches(mapNotification, map)) {
                        try {
                            Member member = guild.getMemberById(Snowflake.of(notificationDoc.getLong("userID")), EntityRetrievalStrategy.REST).block(TIMEOUT);
                            Util.msg(member.getPrivateChannel().block(TIMEOUT), true, titledEmbed);
                        } catch (Exception e) {
                            //invalid member, or member is no longer in guild
                            Thread.interrupted();
                            continue parentLoop;
                        } finally {
                            ServerTrackingLoop.keepAlive(id);
                        }

                        break;
                    }
                }
            }
        } catch (Exception e) {
            Logger.log.error("", e);
        }
    }

    private void send(String message) {
        if (!Objects.isNull(getChannel()))
            Util.msg(getChannel(), true, message);
    }

    private void send(EmbedCreateSpec embed) {
        if (!Objects.isNull(getChannel()))
            Util.msg(getChannel(), true, embed);
    }

    private MessageChannel getChannel() {
        if (channelFetched)
            return channel;

        try {
            channel = Main.gateway.getChannelById(Snowflake.of(doc.getLong("channelID"))).ofType(MessageChannel.class).block(TIMEOUT);
        } catch (ClientException e) {
            if (e.getStatus().code() != 403 && e.getStatus().code() != 404)
                throw e;
        }

        channelFetched = true;
        return channel;
    }

    private void setField(String field, Object value) {
        Main.mongoDatabase.getCollection("services").updateOne(eq("_id", id), new Document("$set", new Document(field, value)));
    }

    /**
     * Checks if provided text matches the provided pattern with wildcards
     *
     * @param pattern Provided pattern with wildcards
     * @param text    Text to match with the pattern
     * @return Whether the text matches the pattern
     */
    private static boolean wildcardMatches(String pattern, String text) {
        int patternIndex = 0;
        int textIndex = 0;
        int starIndex = -1;
        int starTextIndex = 0;

        while (textIndex < text.length()) {
            if (patternIndex < pattern.length() && pattern.charAt(patternIndex) == '*') {
                starIndex = patternIndex;
                starTextIndex = textIndex;
                patternIndex++;
            } else if (patternIndex < pattern.length() && charsMatch(pattern.charAt(patternIndex), text.charAt(textIndex))) {
                patternIndex++;
                textIndex++;
            } else if (starIndex != -1) {
                patternIndex = starIndex + 1;
                starTextIndex++;
                textIndex = starTextIndex;
            } else {
                return false;
            }
        }

        while (patternIndex < pattern.length() && pattern.charAt(patternIndex) == '*')
            patternIndex++;

        return patternIndex == pattern.length();
    }

    private static boolean charsMatch(char a, char b) {
        return a == b || Character.toUpperCase(a) == Character.toUpperCase(b) || Character.toLowerCase(a) == Character.toLowerCase(b);
    }
}
