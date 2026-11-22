package com.vauff.maunzdiscord.servertracking;

import com.github.koraktor.steamcondenser.servers.GameServer;
import com.github.koraktor.steamcondenser.servers.SourceServer;
import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import discord4j.common.util.Snowflake;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.net.InetAddress;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class ServerRequestTask implements Runnable {
    /**
     * Cached InetAddress objects, because it's *very* expensive to constantly run InetAddress.getByName
     */
    private static final ConcurrentHashMap<String, InetAddress> serverAddresses = new ConcurrentHashMap<>();
    private static Instant serverAddressesCreated = Instant.ofEpochMilli(0);

    private final ObjectId id;
    private final String ipPort;

    public ServerRequestTask(ObjectId id, String ipPort) {
        this.id = id;
        this.ipPort = ipPort;
    }

    @Override
    public void run() {
        GameServer server = null;

        try {
            Document doc = Main.mongoDatabase.getCollection("servers").find(eq("_id", id)).first();

            if (Objects.isNull(doc))
                return;

            Document update = new Document();
            int attempts = 0;
            boolean serverInfoSuccess = false;

            while (true) {
                try {
                    if (Objects.isNull(server)) {
                        InetAddress address;

                        if (serverAddresses.containsKey(ipPort)) {
                            address = serverAddresses.get(ipPort);
                        } else {
                            address = InetAddress.getByName(doc.getString("ip"));
                            serverAddresses.put(ipPort, address);
                        }

                        server = new SourceServer(address, doc.getInteger("port"));
                    }

                    if (!serverInfoSuccess) {
                        server.updateServerInfo();
                        serverInfoSuccess = true;
                    }

                    server.updatePlayers();
                    update.append("players", new ArrayList<>(server.getPlayers().keySet()));
                    break;
                } catch (Exception e) {
                    attempts++;

                    if (attempts >= 5 || (!serverInfoSuccess && doc.getInteger("downtimeTimer") >= doc.getInteger("failedConnectionsThreshold"))) {
                        if (!serverInfoSuccess) {
                            int downtimeTimer = doc.getInteger("downtimeTimer") + 1;

                            Logger.log.warn("Failed to connect to the server {}, automatically retrying in 1 minute", ipPort);
                            Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("downtimeTimer", downtimeTimer)));

                            if (downtimeTimer >= ServerTrackingLoop.MAX_DOWNTIME) {
                                Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", new Document("enabled", false)));
                                ServerTrackingLoop.lastInvalidatedCache = Instant.now();
                                ServerTrackingLoop.serverActiveServices.remove(id);
                            }

                            runServiceTasks();
                            return;
                        }

                        Logger.log.warn("Failed to retrieve player information from {}, automatically retrying in 1 minute", ipPort);
                        update.append("players", List.of("SERVER_UPDATEPLAYERS_FAILED"));
                        break;
                    }

                    Thread.sleep(1000);
                }
            }

            HashMap<String, Object> serverInfo = server.getServerInfo();
            String appId = "0";
            int playerCount = 0;
            int maxPlayers = 0;

            if (Objects.isNull(serverInfo.get("mapName"))) {
                Logger.log.warn("Null mapname received for server {}, automatically retrying in 1 minute", ipPort);
                Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), new Document("$set", update));
                return;
            }

            String map = serverInfo.get("mapName").toString();

            // 24-bit app id within 64-bit game id, may not be available
            if (!Objects.isNull(serverInfo.get("gameId")))
                appId = String.valueOf((int) (((long) serverInfo.get("gameId")) & (1L << 24) - 1L));
            // 16-bit app id, possibly truncated but (theoretically) always available
            else if (!Objects.isNull(serverInfo.get("appId")))
                appId = String.valueOf((short) serverInfo.get("appId"));

            // Special handling for CS:GO/CS2, thanks for two games on the same app id, Valve!
            if (appId.equals("730")) {
                appId = "0";

                if (!Objects.isNull(serverInfo.get("gameVersion"))) {
                    // Take the major and minor version only e.g. "1.38.8.1" > 1.38
                    String[] version = serverInfo.get("gameVersion").toString().split("\\.");

                    if (version.length >= 2) {
                        int majorVersion = Integer.parseInt(version[0]);
                        int minorVersion = Integer.parseInt(version[1]);

                        if (majorVersion > 1 || minorVersion >= 39)
                            appId = "730_cs2";
                        else
                            appId = "730_csgo";
                    }
                }
            }

            if (!Objects.isNull(serverInfo.get("numberOfPlayers")))
                playerCount = ((Byte) serverInfo.get("numberOfPlayers")).intValue();

            if (!Objects.isNull(serverInfo.get("maxPlayers")))
                maxPlayers = ((Byte) serverInfo.get("maxPlayers")).intValue();

            if (playerCount > maxPlayers && maxPlayers >= 0)
                playerCount = maxPlayers;

            Document changes = new Document("$set", update);

            update.append("playerCount", playerCount).append("maxPlayers", maxPlayers).append("downtimeTimer", 0);

            if (!appId.equals("0"))
                update.append("appId", appId);

            if (!Objects.isNull(serverInfo.get("serverName")) && !serverInfo.get("serverName").toString().isEmpty())
                update.append("name", serverInfo.get("serverName").toString());

            if (!map.isEmpty() && !map.equalsIgnoreCase(doc.getString("map"))) {
                long timestamp = System.currentTimeMillis();
                boolean mapFound = false;

                update.append("map", map).append("timestamp", timestamp);

                for (int i = 0; i < doc.getList("mapDatabase", Document.class).size(); i++) {
                    String dbMap = doc.getList("mapDatabase", Document.class).get(i).getString("map");

                    if (dbMap.equalsIgnoreCase(map)) {
                        mapFound = true;
                        update.append("mapDatabase." + i + ".lastPlayed", timestamp);
                        break;
                    }
                }

                if (!mapFound)
                    changes.append("$push", new Document("mapDatabase", new Document("map", map).append("firstPlayed", timestamp).append("lastPlayed", timestamp)));

                // Precache the map image colour for service process threads, because multiple threads can run getMapImageColour at the same time, wasting resources if not cached yet
                MapImages.getMapImageColour(MapImages.getMapImageURL(map, appId));
            }

            Main.mongoDatabase.getCollection("servers").updateOne(eq("_id", id), changes);
            runServiceTasks();
        } catch (InterruptedException e) {
            Logger.log.warn("Request to {} was interrupted", ipPort);
        } catch (Exception e) {
            Logger.log.error("", e);
        } finally {
            if (!Objects.isNull(server))
                server.disconnect();
        }
    }

    private void runServiceTasks() throws InterruptedException {
        Document serverDoc = Main.mongoDatabase.getCollection("servers").find(eq("_id", id)).projection(new Document("mapDatabase", 0).append("players", 0)).first();
        List<Document> serviceDocs = Main.mongoDatabase.getCollection("services").find(and(eq("enabled", true), eq("serverID", id))).projection(new Document("guildID", 1)).into(new ArrayList<>());

        for (Document serviceDoc : serviceDocs) {
            ObjectId serviceId = serviceDoc.getObjectId("_id");
            Snowflake guildId = Snowflake.of(serviceDoc.getLong("guildID"));

            if (!Main.guildCache.containsKey(guildId))
                continue;

            ServerTrackingLoop.submit(serviceId, new ServiceProcessTask(serviceId, serverDoc, Main.guildCache.get(guildId)));

            // only start ~20 threads per second
            Thread.sleep(50);
        }
    }

    /**
     * Wipes cached server addresses once they're older than a day
     * This is to allow for potential DNS updates
     */
    public static void checkServerAddresses() {
        if (serverAddressesCreated.plus(1, ChronoUnit.DAYS).isAfter(Instant.now()))
            return;

        serverAddresses.clear();
        serverAddressesCreated = Instant.now();
    }
}
