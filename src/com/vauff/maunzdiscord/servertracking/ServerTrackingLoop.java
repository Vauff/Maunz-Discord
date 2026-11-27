package com.vauff.maunzdiscord.servertracking;

import com.vauff.maunzdiscord.core.Logger;
import com.vauff.maunzdiscord.core.Main;
import discord4j.common.util.Snowflake;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

public class ServerTrackingLoop {

    public static final ConcurrentHashMap<ObjectId, Integer> serverActiveServices = new ConcurrentHashMap<>();


    public static Instant lastInvalidatedCache = Instant.ofEpochMilli(0L);

    static final int MAX_DOWNTIME = 10080;

    private static final long LOOP_TIME = 60000;
    private static final long TASK_TIMEOUT = 120000;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "servertracking-scheduler"));

    private static final ConcurrentHashMap<ObjectId, Thread> threads = new ConcurrentHashMap<>();


    private static final ConcurrentHashMap<ObjectId, Long> lastActive = new ConcurrentHashMap<>();

    public static void start() {
        // Initial 10 second delay to allow map images to fully initialize
        scheduler.scheduleAtFixedRate(ServerTrackingLoop::run, 10000, LOOP_TIME, TimeUnit.MILLISECONDS);
        scheduler.scheduleWithFixedDelay(ServerTrackingLoop::checkThreads, 1, 1, TimeUnit.SECONDS);
    }

    static void submit(ObjectId id, Runnable task) {
        if (Main.shutdownState != Main.ShutdownState.NONE)
            return;

        if (!Objects.isNull(lastActive.putIfAbsent(id, System.currentTimeMillis())))
            return;

        Thread thread = Thread.ofVirtual().name("servertracking-" + id).unstarted(() ->
        {
            try {
                task.run();
            } finally {
                threads.remove(id);
                lastActive.remove(id);
            }
        });

        threads.put(id, thread);
        thread.start();
    }

    static void keepAlive(ObjectId id) {
        lastActive.replace(id, System.currentTimeMillis());
    }

    private static void checkThreads() {
        if (Main.shutdownState == Main.ShutdownState.SHUTDOWN_QUEUED && lastActive.isEmpty()) {
            Main.shutdownState = Main.ShutdownState.SHUTDOWN_SAFE;
            return;
        }

        for (Entry<ObjectId, Thread> entry : threads.entrySet()) {
            Long time = lastActive.get(entry.getKey());

            if (Objects.isNull(time) || System.currentTimeMillis() - time < TASK_TIMEOUT)
                continue;

            Logger.log.warn("Server tracking thread {} has been stuck for " + (TASK_TIMEOUT / 1000) + " seconds, interrupting it", entry.getValue().getName());
            entry.getValue().interrupt();
            keepAlive(entry.getKey());
        }
    }

    private static void run() {
        try {
            if (Main.shutdownState != Main.ShutdownState.NONE)
                return;

            if (Boolean.FALSE.equals(Main.gateway.getGatewayClient(0).get().isConnected().block(Duration.ofSeconds(10))))
                return;

            ServerRequestTask.checkServerAddresses();

            List<Document> serverDocs = Main.mongoDatabase.getCollection("servers").find(eq("enabled", true)).projection(new Document("ip", 1).append("port", 1)).into(new ArrayList<>());
            List<Document> serviceDocs = null;
            List<Document> trackedServers = new ArrayList<>();

            for (Document doc : serverDocs) {
                ObjectId id = doc.getObjectId("_id");

                if (!serverActiveServices.containsKey(id)) {
                    int serviceCount = 0;

                    if (Objects.isNull(serviceDocs))
                        serviceDocs = Main.mongoDatabase.getCollection("services").find(eq("enabled", true)).projection(new Document("serverID", 1).append("guildID", 1)).into(new ArrayList<>());

                    for (Document serviceDoc : serviceDocs) {
                        ObjectId serverID = serviceDoc.getObjectId("serverID");
                        Snowflake guildId = Snowflake.of(serviceDoc.getLong("guildID"));

                        if (id.equals(serverID) && Main.guildCache.containsKey(guildId))
                            serviceCount++;
                    }

                    serverActiveServices.put(id, serviceCount);
                }

                if (serverActiveServices.get(id) > 0)
                    trackedServers.add(doc);
            }

            long spacing = LOOP_TIME / Math.max(trackedServers.size(), 1);

            for (int i = 0; i < trackedServers.size(); i++) {
                ObjectId id = trackedServers.get(i).getObjectId("_id");
                String ipPort = trackedServers.get(i).getString("ip") + ":" + trackedServers.get(i).getInteger("port");

                scheduler.schedule(() -> submit(id, new ServerRequestTask(id, ipPort)), i * spacing, TimeUnit.MILLISECONDS);
            }

            Logger.log.debug("Started server tracking loop for {} servers", trackedServers.size());
        } catch (Exception e) {
            Logger.log.error("", e);
        }
    }
}
