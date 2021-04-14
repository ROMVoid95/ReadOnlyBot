package net.readonly.utils.exports;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

public class Metrics {
    public static final ThreadPoolCollector THREAD_POOL_COLLECTOR = new ThreadPoolCollector().register();
    public static final Histogram COMMAND_LATENCY = Histogram.build()
            .name("command_latency")
            .help("Time it takes for a command to process.")
            .register();
    public static final Counter COMMAND_COUNTER = Counter.build()
            .name("commands")
            .help("Amounts of commands ran by name")
            .labelNames("name")
            .register();
    public static final Counter CATEGORY_COUNTER = Counter.build()
            .name("categories")
            .help("Amounts of categories ran by name")
            .labelNames("name")
            .register();
    public static final Gauge GUILD_COUNT = Gauge.build()
            .name("guilds")
            .help("Guild Count")
            .register();
    public static final Gauge USER_COUNT = Gauge.build()
            .name("users")
            .help("User Count")
            .register();
    public static final Counter HTTP_REQUESTS = Counter.build()
            .name("http_requests")
            .help("Successful HTTP Requests (JDA)")
            .register();
    public static final Counter HTTP_429_REQUESTS = Counter.build()
            .name("http_ratelimit_requests")
            .help("429 HTTP Requests (JDA)")
            .register();
    public static final Counter RECEIVED_MESSAGES = Counter.build()
            .name("messages_received")
            .help("Received messages (all users + bots)")
            .register();
}
