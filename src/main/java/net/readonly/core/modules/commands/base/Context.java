/*
 * Copyright (c) 2021 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.readonly.core.modules.commands.base;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.utils.concurrent.Task;
import net.readonly.BotData;
import net.readonly.ReadOnlyBot;
import net.readonly.config.Config;
import net.readonly.core.modules.commands.i18n.I18nContext;
import net.readonly.database.ManagedDatabase;
import net.readonly.database.entity.DBGuild;
import net.readonly.utils.FinderUtil;
import net.readonly.utils.StringUtils;
import redis.clients.jedis.JedisPool;

public class Context {
    private final ReadOnlyBot bot = ReadOnlyBot.instance();
    private final Config config = BotData.config();
    private final ManagedDatabase managedDatabase = BotData.db();

    private final GuildMessageReceivedEvent event;
    private final String content;
    private final boolean isMentionPrefix;
    private I18nContext languageContext;

    public Context(GuildMessageReceivedEvent event, I18nContext languageContext, String content, boolean isMentionPrefix) {
        this.event = event;
        this.languageContext = languageContext;
        this.content = content;
        this.isMentionPrefix = isMentionPrefix;
    }

    public ReadOnlyBot getBot() {
        return bot;
    }

    public Config getConfig() {
        return config;
    }
    
    public ManagedDatabase db() {
        return managedDatabase;
    }

    public GuildMessageReceivedEvent getEvent() {
        return event;
    }

    public JDA getJDA() {
        return getEvent().getJDA();
    }
    
    public I18nContext getLanguageContext() {
        return languageContext;
    }
    
    public List<TextChannel> getMentionedTextChannels() {
    	final var mentionedChannels = getEvent().getMessage().getMentionedChannels();
        if (isMentionPrefix) {
            final var mutable = new LinkedList<>(mentionedChannels);
            return mutable.subList(1, mutable.size());
        }

        return mentionedChannels;
    }

    public List<User> getMentionedUsers() {
        final var mentionedUsers = getEvent().getMessage().getMentionedUsers();
        if (isMentionPrefix) {
            final var mutable = new LinkedList<>(mentionedUsers);
            return mutable.subList(1, mutable.size());
        }

        return mentionedUsers;
    }

    public List<Member> getMentionedMembers() {
        final var mentionedMembers = getEvent().getMessage().getMentionedMembers();
        if (isMentionPrefix) {
            final var mutable = new LinkedList<>(mentionedMembers);
            return mutable.subList(1, mutable.size());
        }

        return mentionedMembers;
    }

    public Member getMember() {
        return event.getMember();
    }

    public User getUser() {
        return event.getAuthor();
    }

    public User getAuthor() {
        return getUser();
    }

    public Guild getGuild() {
        return event.getGuild();
    }

    public Message getMessage() {
        return event.getMessage();
    }

    public SelfUser getSelfUser() {
        return event.getJDA().getSelfUser();
    }

    public Member getSelfMember() {
        return getGuild().getSelfMember();
    }

    public TextChannel getChannel() {
        return event.getChannel();
    }

    public boolean hasReactionPerms() {
        return getSelfMember().hasPermission(getChannel(), Permission.MESSAGE_ADD_REACTION) &&
                // Somehow also needs this?
                getSelfMember().hasPermission(getChannel(), Permission.MESSAGE_HISTORY);
    }

    public String getContent() {
        return content;
    }

    public String[] getArguments() {
        return StringUtils.advancedSplitArgs(content, 0);
    }

    public Map<String, String> getOptionalArguments() {
        return StringUtils.parseArguments(getArguments());
    }

    public void send(Message message) {
        getChannel().sendMessage(message).queue();
    }

    public void send(String message) {
        getChannel().sendMessage(message).queue();
    }

    public void sendFormat(String message, Object... format) {
        getChannel().sendMessage(
                String.format(StringUtils.getLocaleFromLanguage(getLanguageContext()), message, format)
        ).queue();
    }

    public void send(MessageEmbed embed) {
        // Sending embeds while supressing the failure callbacks leads to very hard
        // to debug bugs, so enable it.
        getChannel().sendMessage(embed).queue(success -> {}, Throwable::printStackTrace);
    }

    public void sendLocalized(String localizedMessage, Object... args) {
        // Stop swallowing issues with String replacements (somehow really common)
        getChannel().sendMessage(
                String.format(StringUtils.getLocaleFromLanguage(getLanguageContext()), languageContext.get(localizedMessage), args)
        ).queue(success -> {}, Throwable::printStackTrace);
    }

    public void sendLocalized(String localizedMessage) {
        getChannel().sendMessage(languageContext.get(localizedMessage)).queue();
    }

    public void sendStripped(String message) {
        getChannel().sendMessage(message)
                .allowedMentions(EnumSet.noneOf(Message.MentionType.class))
                .queue();
    }

    public void sendStrippedLocalized(String localizedMessage, Object... args) {
        getChannel().sendMessage(String.format(
        		StringUtils.getLocaleFromLanguage(getLanguageContext()), languageContext.get(localizedMessage), args)
        ).allowedMentions(EnumSet.noneOf(Message.MentionType.class)).queue();
    }

    public Task<List<Member>> findMember(String query, Consumer<List<Member>> success) {
        return FinderUtil.lookupMember(getGuild(), this, query).onSuccess(s -> {
            try {
                success.accept(s);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public User retrieveUserById(String id) {
        User user = null;
        try {
            user = ReadOnlyBot.instance().getJDA().retrieveUserById(id).complete();
        } catch (Exception ignored) { }

        return user;
    }

    public Member retrieveMemberById(Guild guild, String id, boolean update) {
        Member member = null;
        try {
            member = guild.retrieveMemberById(id, update).complete();
        } catch (Exception ignored) { }

        return member;
    }

    public Member retrieveMemberById(String id, boolean update) {
        Member member = null;
        try {
            member = getGuild().retrieveMemberById(id, update).complete();
        } catch (Exception ignored) { }

        return member;
    }

    public boolean isMentionPrefix() {
        return isMentionPrefix;
    }
    
    public JedisPool getJedisPool() {
        return BotData.getDefaultJedisPool();
    }
    
    public DBGuild getDBGuild() {
        return managedDatabase.getGuild(getGuild());
    }
}
