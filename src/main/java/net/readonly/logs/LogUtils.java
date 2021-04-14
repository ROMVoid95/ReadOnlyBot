package net.readonly.logs;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedFooter;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedTitle;
import net.readonly.BotData;
import net.readonly.utils.DateFormatting;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;

public class LogUtils {
    private static final Logger log = LoggerFactory.getLogger(LogUtils.class);

    private final static String ICON_URL = "https://i.imgur.com/xNZtDcm.png";
    private static final String WEBHOOK_START = "https://discordapp.com/api/webhooks/";
    private static WebhookClient LOGBACK_WEBHOOK;

    static {
        String webhook = BotData.config().getWebhookUrl().replace(WEBHOOK_START, "");
        if (webhook != null) {
        	log.info(webhook);
        	var parts = webhook.split("/");
        	long id = Long.valueOf(parts[0]);
        	String token = parts[1];
            LOGBACK_WEBHOOK = new WebhookClientBuilder(id, token).build();
        } else {
            log.error("Webhook URL is null. Webhooks won't be posted at all to status channels.");
        }
    }

    public static void log(String title, String message) {
        if (LOGBACK_WEBHOOK == null) {
            return;
        }

        try {
        	LOGBACK_WEBHOOK.send(
        			msg()
        			.addEmbeds(new WebhookEmbedBuilder()
                    	.setColor(Color.GREEN.getRGB())
                    	.setTitle(new EmbedTitle(title, null))
                    	.setDescription(message)
                    	.setFooter(new EmbedFooter(DateFormatting.formatDate(OffsetDateTime.now()), ICON_URL))
                    	.build())
        			.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void log(String message) {
        if (LOGBACK_WEBHOOK == null) {
            return;
        }

        try {
            LOGBACK_WEBHOOK.send(new WebhookEmbed(
                    null, Color.PINK.getRGB(), message,
                    null, null,
                    new WebhookEmbed.EmbedFooter(DateFormatting.formatDate(OffsetDateTime.now()), ICON_URL),
                    new WebhookEmbed.EmbedTitle("Log", null), null,
                    new ArrayList<>()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void simple(String message) {
        if (LOGBACK_WEBHOOK == null) {
            return;
        }

        try {
            LOGBACK_WEBHOOK.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static @NotNull WebhookMessageBuilder msg() {
    	return new WebhookMessageBuilder()
    	.setUsername("RocketBot WebLog")
    	.setAvatarUrl(ICON_URL);
    }
}
