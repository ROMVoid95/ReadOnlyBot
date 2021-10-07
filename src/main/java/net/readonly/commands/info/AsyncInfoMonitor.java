package net.readonly.commands.info;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.readonly.BotData;
import net.readonly.ReadOnlyBot;
import net.readonly.core.listener.BotListener;
import net.readonly.utils.exports.JFRExports;

public class AsyncInfoMonitor {
    private static final ScheduledExecutorService POOL = Executors.newSingleThreadScheduledExecutor(
            task -> new Thread(task, "RocketBot-AsyncInfoMonitor")
    );

    private static final Logger log = LoggerFactory.getLogger(AsyncInfoMonitor.class);

    private static final int availableProcessors = Runtime.getRuntime().availableProcessors();
    private static long freeMemory = 0;
    private static long maxMemory = 0;
    private static boolean started = false;
    private static long threadCount = 0;
    private static long totalMemory = 0;
    private static float processCpuUsage;
    private static double vpsCPUUsage = 0;
    private static long vpsFreeMemory = 0;
    private static long vpsMaxMemory = 0;

    public static int getAvailableProcessors() {
        check();
        return availableProcessors;
    }

    public static long getFreeMemory() {
        check();
        return freeMemory;
    }

    public static long getMaxMemory() {
        check();
        return maxMemory;
    }

    public static long getThreadCount() {
        check();
        return threadCount;
    }

    public static long getTotalMemory() {
        check();
        return totalMemory;
    }

    public static double getInstanceCPUUsage() {
        check();
        return processCpuUsage;
    }

    // The following methods are used by JFRExports to set values in this class

    public static void setProcessCpuUsage(float usage) {
        processCpuUsage = usage;
    }

    public static void setThreadCount(long count) {
        threadCount = count;
    }

    public static void setMachineMemoryUsage(long used, long total) {
        vpsMaxMemory = total;
        vpsFreeMemory = total - used;
    }

    public static void setMachineCPUUsage(float usage) {
        vpsCPUUsage = usage;
    }

    public static void start() {
        if (started) {
            throw new IllegalStateException("Already Started.");
        }

        // Some stats are set by JFRExports
        // By some I mean basically most of them
        JFRExports.register();
        var bot = ReadOnlyBot.instance();

        var nodeSetName = "node-stats-" + ReadOnlyBot.instance().getJDA().getSelfUser().getId();

        log.info("Started System Monitor! Monitoring system statistics since now!");
        log.info("Posting node system stats to redis on set {}", nodeSetName);

        POOL.scheduleAtFixedRate(() -> {
            freeMemory = Runtime.getRuntime().freeMemory();
            maxMemory = Runtime.getRuntime().maxMemory();
            totalMemory = Runtime.getRuntime().totalMemory();

            try(var jedis = BotData.getDefaultJedisPool().getResource()) {
                jedis.hset(nodeSetName, bot.getJDA().getSelfUser().getName(),
                        new JSONObject()
                                .put("uptime", ManagementFactory.getRuntimeMXBean().getUptime())
                                .put("thread_count", threadCount)
                                .put("available_processors", availableProcessors)
                                .put("free_memory", freeMemory)
                                .put("max_memory", maxMemory)
                                .put("total_memory", totalMemory)
                                .put("used_memory", totalMemory - freeMemory)
                                .put("cpu_usage", processCpuUsage)
                                .put("machine_cpu_usage", vpsCPUUsage)
                                .put("machine_free_memory", vpsFreeMemory)
                                .put("machine_total_memory", vpsMaxMemory)
                                .put("machine_used_memory", vpsMaxMemory - vpsFreeMemory)
                                .put("commands_ran", BotListener.getCommandTotal())
                                .toString()
                );
            }
        }, 15, 30, TimeUnit.SECONDS);

        started = true;
    }

    private static void check() {
        if (!started) throw new IllegalStateException("AsyncInfoMonitor not started");
    }
}
