package net.readonly.commands.info.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

public class CategoryStatsManager extends StatsManager<String> {

    public static final ExpiringMap<String, AtomicInteger> DAY_CATS = ExpiringMap.builder()
            .expiration(1, TimeUnit.DAYS)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();
    public static final ExpiringMap<String, AtomicInteger> HOUR_CATS = ExpiringMap.builder()
            .expiration(1, TimeUnit.HOURS)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();
    public static final ExpiringMap<String, AtomicInteger> MINUTE_CATS = ExpiringMap.builder()
            .expiration(1, TimeUnit.MINUTES)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();
    public static final Map<String, AtomicInteger> TOTAL_CATS = new HashMap<>();

    public static void log(String cmd) {
        if (cmd.isEmpty()) {
            return;
        }

        TOTAL_CATS.computeIfAbsent(cmd, k -> new AtomicInteger(0)).incrementAndGet();
        DAY_CATS.computeIfAbsent(cmd, k -> new AtomicInteger(0)).incrementAndGet();
        HOUR_CATS.computeIfAbsent(cmd, k -> new AtomicInteger(0)).incrementAndGet();
        MINUTE_CATS.computeIfAbsent(cmd, k -> new AtomicInteger(0)).incrementAndGet();
    }
	
}
