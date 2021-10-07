package net.readonly.commands.info.stats;

import java.util.stream.Collectors;

import net.dv8tion.jda.api.EmbedBuilder;
import net.readonly.utils.tracker.Instance;
import net.readonly.utils.tracker.TrackerGroup;

public class CommandStatsManager extends StatsManager<String> {
    private static final TrackerGroup<String> TRACKERS = new TrackerGroup<>();

    public static void log(String cmd) {
        if (cmd.isEmpty()) {
            return;
        }

        TRACKERS.tracker(cmd).increment();
    }

    public static String resume(Instance instance) {
        var total = TRACKERS.total(instance);

        return (total == 0) ? ("No Events Logged.") : ("Count: " + total + "\n" + TRACKERS.highest(instance, 5)
                .map(tracker -> {
                    int percent = Math.round((float) instance.amount(tracker) * 100 / total);
                    return String.format("%s %d%% **%s** (%d)", bar(percent, 15), percent, tracker.getKey(), instance.amount(tracker));
                })
                .collect(Collectors.joining("\n")));
    }

    public static EmbedBuilder fillEmbed(Instance instance, EmbedBuilder builder) {
        long total = TRACKERS.total(instance);

        if (total == 0) {
            builder.addField("Nothing Here.", "Just dust.", false);
            return builder;
        }

        TRACKERS.highest(instance, 12)
                .forEach(tracker -> {
                    long percent = instance.amount(tracker) * 100 / total;
                    builder.addField(tracker.getKey(), String.format("%s %d%% (%d)", bar(percent, 15), percent, instance.amount(tracker)), false);
                });

        return builder;
    }
}
