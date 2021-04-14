package net.readonly.core.listener;

import java.util.Base64;
import java.util.IllegalFormatException;
import java.util.concurrent.ExecutorService;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.readonly.BotData;
import net.readonly.core.command.processor.CommandProcessor;
import net.readonly.database.InitDatabase;
import net.readonly.utils.EmoteReference;
import net.readonly.utils.exports.Metrics;

public class BotListener implements EventListener {
	private static final Logger log = LoggerFactory.getLogger(BotListener.class);
    private static int commandTotal = 0;
    
    private final CommandProcessor commandProcessor;
    private final ExecutorService threadPool;
    //private final Cache<Long, Optional<CachedMessage>> messageCache;
    
    public BotListener(CommandProcessor processor, ExecutorService threadPool) {
        this.commandProcessor = processor;
        this.threadPool = threadPool;
    }
    
    public static int getCommandTotal() {
        return commandTotal;
    }
    
    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
        	new InitDatabase((ReadyEvent) event);
            threadPool.execute(() -> this.updateStats(event.getJDA()));
            return;
        }
        if (event instanceof GuildMessageReceivedEvent) {
        	Metrics.RECEIVED_MESSAGES.inc();
            var msg = (GuildMessageReceivedEvent) event;
            // Ignore myself and bots.
            // Technically ignoring oneself is an extra step -- we're a bot, and we ignore bots.
            var isSelf = msg.getAuthor().getIdLong() == msg.getJDA().getSelfUser().getIdLong();
            if (msg.getAuthor().isBot() || msg.isWebhookMessage() || isSelf) {
                return;
            }

            // Inserts a cached message into the cache. This only holds the id and the content, and is way lighter than saving the entire jda object.
//            messageCache.put(msg.getMessage().getIdLong(), Optional.of(
//                    new CachedMessage(msg.getGuild().getIdLong(), msg.getAuthor().getIdLong(), msg.getMessage().getContentDisplay()))
//            );

            // We can't talk here, so we don't need to run anything.
            // Run this check before executing on the pool to avoid wasting a thread.
            if (!msg.getChannel().canTalk()) {
                return;
            }

            threadPool.execute(() -> onCommand(msg));
        }
        
        if (event instanceof HttpRequestEvent) {
            // We've fucked up big time if we reach this
            final var httpRequestEvent = (HttpRequestEvent) event;
            if (httpRequestEvent.isRateLimit()) {
                log.error("!!! Reached 429 on: {}", httpRequestEvent.getRoute());
                Metrics.HTTP_429_REQUESTS.inc();
            }
            Metrics.HTTP_REQUESTS.inc();
        }
    }
    
    private void onCommand(GuildMessageReceivedEvent event) {
        try {
            if (commandProcessor.run(event)) {
                commandTotal++;
            } 
        } catch (IllegalFormatException e) {
            var id = toSnow64(event.getMessage().getIdLong());
            event.getChannel().sendMessageFormat(
                    "%sWe found at error when trying to format a String. Please report this to the devs with error ID `%s`",
                    EmoteReference.ERROR, id
            ).queue();

            log.warn("Wrong String format. Check this. ID: {}", id, e);
        } catch (IndexOutOfBoundsException e) {
            var id = toSnow64(event.getMessage().getIdLong());
            event.getChannel().sendMessageFormat(
                    "%sYour query returned no results or you used the incorrect arguments, seemingly (Error ID: `%s`): Just in case, check command help!",
                    EmoteReference.ERROR, id
            ).queue();

            log.warn("Exception caught and alternate message sent. We should look into this, anyway (ID: {})", id, e);
        } catch (PermissionException e) {
            if (e.getPermission() != Permission.UNKNOWN) {
                event.getChannel().sendMessageFormat(
                        "%sI don't have permission to do this :(\nI need the permission: **%s**",
                        EmoteReference.ERROR, e.getPermission().getName()
                ).queue();
            } else {
                event.getChannel().sendMessage(
                        EmoteReference.ERROR +
                        "I cannot perform this action due to the lack of permission! Is the role I might be trying to assign " +
                        "higher than my role? Do I have the correct permissions/hierarchy to perform this action?"
                ).queue();
            }
        } catch (IllegalArgumentException e) { //NumberFormatException == IllegalArgumentException
            var id = toSnow64(event.getMessage().getIdLong());
            event.getChannel().sendMessageFormat(
                    "%sI think you forgot something on the floor. (Error ID: `%s`):\n" +
                    "%sCould be an internal error, but check the command arguments or maybe the message I'm trying to send exceeds 2048 characters, " +
                    "Just in case, check command help!",
                    EmoteReference.ERROR, id, EmoteReference.WARNING
            ).queue();

            log.warn("Exception caught and alternate message sent. We should look into this, anyway (ID: {})", id, e);
        } catch (Exception e) {
            var quote = "Uh-oh, seemingly the devs forgot some 0's and 1's on the floor";
            var report = "Tell the devs to quit dropping binaries, oh and tell them the Error ID";
            var id = toSnow64(event.getMessage().getIdLong());

            event.getChannel().sendMessageFormat(
                    "%s%s \n(Unexpected error, ID: `%s`)\n%s",
                    EmoteReference.ERROR, quote, id, report
            ).queue();

            log.error("Error happened on command: {} (Error ID: {})", event.getMessage().getContentRaw(), id, e);
        }
    }

    private String toSnow64(long snowflake) {
        return Base64.getEncoder()
                .encodeToString(Longs.toByteArray((snowflake)))
                .replace('/', '-')
                .replace('=', ' ')
                .trim();
    }
    
    private void updateStats(JDA jda) {
        if (jda.getStatus() == JDA.Status.INITIALIZED) {
            return;
        }

        try(var jedis = BotData.getDefaultJedisPool().getResource()) {
            var json = new JSONObject()
                    .put("guild_count", jda.getGuildCache().size())
                    .put("cached_users", jda.getUserCache().size())
                    .put("gateway_ping", jda.getGatewayPing())
                    .toString();

            jedis.hset("stats-" + jda.getSelfUser().getId(), String.valueOf(jda.getSelfUser().getId()), json);
            log.debug("Sent process shard stats to redis -> {}", json);
        }
    }
}
