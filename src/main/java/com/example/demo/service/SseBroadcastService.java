package com.example.demo.service;

import com.example.demo.dto.MilestoneBroadcastPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages all Server-Sent Events (SSE) connections and broadcasts
 * milestone mutation events to subscribed Layout Nodes (frontend dashboards).
 *
 * Design decisions:
 * - CopyOnWriteArrayList: thread-safe for concurrent subscribe/broadcast without locks
 * - @Async on broadcast(): ensures PM's sync HTTP response is NOT blocked
 *   by the time required to iterate and push to all connected clients
 * - Per-projectId filtering: Layout Nodes only receive events for their project,
 *   preventing unnecessary DOM updates on unrelated views
 * - Automatic cleanup: dead emitters (timeout/error/complete) are removed immediately
 */
@Service
public class SseBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(SseBroadcastService.class);

    // SSE connection timeout: 5 minutes. Client EventSource auto-reconnects after timeout.
    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L;

    // Thread-safe list of all active SSE connections
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private final ObjectMapper objectMapper;

    public SseBroadcastService() {
        // Manually instantiate and configure ObjectMapper to guarantee it exists
        // regardless of Spring Boot autoconfiguration exclusions.
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Registers a new SSE connection from a Layout Node.
     * Called by the Controller when a client hits GET /api/milestones/stream.
     *
     * @return a new SseEmitter bound to this client connection
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        // Remove this emitter from active list when it finishes/times out/errors
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.debug("SSE emitter completed and removed. Active connections: {}", emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.debug("SSE emitter timed out and removed. Active connections: {}", emitters.size());
        });
        emitter.onError(e -> {
            emitters.remove(emitter);
            log.warn("SSE emitter error, removed. Active connections: {}", emitters.size(), e);
        });

        emitters.add(emitter);
        log.info("New SSE subscriber connected. Total active connections: {}", emitters.size());

        // Send an initial "connected" ping so the client knows the stream is live
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"message\":\"SSE stream established.\"}"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    /**
     * Broadcasts a milestone mutation event asynchronously to all active Layout Nodes.
     *
     * @Async ensures this method runs in a separate thread pool, so the calling
     * HTTP request thread (the PM's sync call) returns its response immediately
     * and is NOT blocked waiting for all emitters to receive the push.
     *
     * @param payload the event payload containing milestone update details
     */
    @Async
    public void broadcast(MilestoneBroadcastPayload payload) {
        if (emitters.isEmpty()) {
            log.debug("No active SSE subscribers. Skipping broadcast for project {}.", payload.getProjectId());
            return;
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Failed to serialize broadcast payload to JSON.", e);
            return;
        }

        log.info("Broadcasting milestone event '{}' for project {} to {} subscribers.",
                payload.getEventType(), payload.getProjectId(), emitters.size());

        // Iterate over a snapshot to avoid ConcurrentModificationException
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("milestone-update")
                        .data(json));
            } catch (IOException e) {
                // Client disconnected — clean up this dead emitter
                emitters.remove(emitter);
                log.warn("Failed to send SSE event to a client. Emitter removed.");
            }
        }
    }

    /** Returns the number of currently active SSE connections (for monitoring). */
    public int getActiveConnectionCount() {
        return emitters.size();
    }
}
