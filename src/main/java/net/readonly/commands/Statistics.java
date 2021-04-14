package net.readonly.commands;

import java.awt.Color;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONObject;

import com.google.common.eventbus.Subscribe;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.readonly.core.CommandRegistry;
import net.readonly.core.modules.Module;
import net.readonly.core.modules.commands.SimpleTreeCommand;
import net.readonly.core.modules.commands.SubCommand;
import net.readonly.core.modules.commands.base.CommandCategory;
import net.readonly.core.modules.commands.base.Context;
import net.readonly.core.modules.commands.help.HelpContent;
import net.readonly.utils.EmbedUtil;
import net.readonly.utils.StringUtils;
import redis.clients.jedis.Jedis;

@Module
public class Statistics {
	
    @Subscribe
    public void stats(CommandRegistry cr) {
        SimpleTreeCommand statsCommand = cr.register("stats", new SimpleTreeCommand(CommandCategory.INFO) {
            @Override
            public HelpContent help() {
                return new HelpContent.Builder()
                        .setDescription("See the bot, usage or vps statistics.")
                        .setUsage("stats <option>` - Returns statistical information.")
                        .addParameter("option", "What to check for. See subcommands")
                        .build();
            }
        });
        
        statsCommand.addSubCommand("vps", new SubCommand() {
            @Override
            public String description() {
                return "vps statistics.";
            }

            @Override
            protected void call(Context ctx, String content) {
                Map<String, String> nodeMap;
                try (Jedis jedis = ctx.getJedisPool().getResource()) {
                    nodeMap = jedis.hgetAll("node-stats-" + ctx.getJDA().getSelfUser().getId());
                }

                var embed = new EmbedBuilder().setTitle("Mantaro Node Statistics")
                        .setDescription("This shows the current status of the online nodes. " +
                                "Every node contains a set amount of shards.")
                        .setThumbnail(ctx.getSelfUser().getAvatarUrl())
                        .setColor(Color.PINK)
                        .setFooter("Available Nodes: " + nodeMap.size());

                java.util.List<MessageEmbed.Field> fields = new LinkedList<>();
                nodeMap.entrySet().stream().sorted(
                        Comparator.comparingInt(e -> Integer.parseInt(e.getKey().split("-")[1]))
                ).forEach(node -> {
                    var nodeData = new JSONObject(node.getValue());
                    fields.add(new MessageEmbed.Field("Node " + node.getKey(),
                            """
                               **Uptime**: %s
                               **CPU Cores**: %s
                               **CPU Usage**: %s
                               **Memory**: %s
                               **Threads**: %,d
                               **Guilds**: %,d
                               **User Cache**: %,d
                               **Machine Memory**: %s
                               """.formatted(
                                    StringUtils.formatDuration(nodeData.getLong("uptime")),
                                    nodeData.getLong("available_processors"),
                                    "%.2f%%".formatted(nodeData.getDouble("cpu_usage")),
                                    StringUtils.formatMemoryUsage(nodeData.getLong("used_memory"), nodeData.getLong("total_memory")),
                                    nodeData.getLong("thread_count"),
                                    nodeData.getLong("guild_count"),
                                    nodeData.getLong("user_count"),
                                    StringUtils.formatMemoryAmount(nodeData.getLong("machine_total_memory"))
                            ), false
                    ));
                });

                EmbedUtil.sendPaginatedEmbed(ctx, embed, EmbedUtil.divideFields(3, fields));
            }
        });
    }
}
