package net.readonly.utils;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.BufferPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import net.readonly.BotData;
import net.readonly.utils.exports.DiscordLatencyExports;
import net.readonly.utils.exports.JFRExports;
import net.readonly.utils.exports.MemoryUsageExports;

public class Prometheus {
    public static final Duration UPDATE_PERIOD = Duration.ofSeconds(3);

    private static final AtomicReference<State> STATE = new AtomicReference<>(State.DISABLED);
    private static volatile HTTPServer server;

    public static State currentState() {
        return STATE.get();
    }

    public static void registerPostStartup() {
        DiscordLatencyExports.register();
        MemoryUsageExports.register();
    }

    public static void enable() throws IOException {
        if (STATE.compareAndSet(State.DISABLED, State.ENABLING)) {
            new StandardExports().register();
            new BufferPoolsExports().register();
            JFRExports.register();
            server = new HTTPServer(BotData.config().getPrometheusPort());
            STATE.set(State.ENABLED);
        }
    }

    public static void disable() {
        while (!STATE.compareAndSet(State.ENABLED, State.DISABLED)) {
            if (STATE.get() == State.DISABLED) {
                return;
            }

            Thread.yield();
        }
        server.stop();
    }

    public enum State {
        DISABLED, ENABLING, ENABLED
    }
}
