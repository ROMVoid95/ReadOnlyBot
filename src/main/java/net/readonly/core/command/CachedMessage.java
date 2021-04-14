package net.readonly.core.command;

import net.dv8tion.jda.api.entities.User;
import net.readonly.ReadOnlyBot;

public class CachedMessage {
    private final long guildId;
    private final long author;
    private final String content;

    public CachedMessage(long guildId, long author, String content) {
        this.guildId = guildId;
        this.author = author;
        this.content = content;
    }

    public User getAuthor() {
        var guild = ReadOnlyBot.instance().getJDA().getGuildById(guildId);
        User user = null;

        if (guild != null)  {
            user = guild.retrieveMemberById(author).complete().getUser();
        }

        return user;
    }

    public String getContent() {
        return this.content;
    }
}
