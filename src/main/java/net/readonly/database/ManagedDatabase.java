package net.readonly.database;

import static com.rethinkdb.RethinkDB.r;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.rethinkdb.net.Connection;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.readonly.database.entity.BotObj;
import net.readonly.database.entity.DBGuild;

@Slf4j
public class ManagedDatabase {
    private final Connection conn;
    
    public ManagedDatabase(@Nonnull Connection conn) {
        this.conn = conn;
    }

    @Nonnull
    @CheckReturnValue
    public BotObj getBotData() {
        log.info("Requesting BotData from rethink");
        BotObj obj = r.table(BotObj.DB_TABLE).get("crashbot").runAtom(conn, BotObj.class);
        return obj == null ? BotObj.create() : obj;
    }
    
    @Nonnull
    @CheckReturnValue
    public DBGuild getGuild(@Nonnull String guildId) {
        log.info("Requesting guild {} from rethink", guildId);
        DBGuild guild = r.table(DBGuild.DB_TABLE).get(guildId).runAtom(conn, DBGuild.class);
        return guild == null ? DBGuild.of(guildId) : guild;
    }
    
    @Nullable
    @CheckReturnValue
    public Boolean checkGuild(@Nonnull String guildId) {
        log.info("Checking for guild {} from rethink", guildId);
        DBGuild guild = r.table(DBGuild.DB_TABLE).get(guildId).runAtom(conn, DBGuild.class);
        return guild == null ? false : true;
    }

    @Nonnull
    @CheckReturnValue
    public DBGuild getGuild(@Nonnull Guild guild) {
        return getGuild(guild.getId());
    }

    @Nonnull
    @CheckReturnValue
    public DBGuild getGuild(@Nonnull Member member) {
        return getGuild(member.getGuild());
    }

    @Nonnull
    @CheckReturnValue
    public DBGuild getGuild(@Nonnull GuildMessageReceivedEvent event) {
        return getGuild(event.getGuild());
    }

    public void save(@Nonnull ManagedObject object) {
        log.info("Saving {} {}:{} to rethink (replacing)", object.getClass().getSimpleName(), object.getTableName(), object.getDatabaseId());

        r.table(object.getTableName())
                .insert(object)
                .optArg("conflict", "replace")
                .runNoReply(conn);
    }

    public void saveUpdating(@Nonnull ManagedObject object) {
        log.info("Saving {} {}:{} to rethink (updating)", object.getClass().getSimpleName(), object.getTableName(), object.getDatabaseId());

        r.table(object.getTableName())
                .insert(object)
                .optArg("conflict", "update")
                .runNoReply(conn);
    }

    public void delete(@Nonnull ManagedObject object) {
        log.info("Deleting {} {}:{} from rethink", object.getClass().getSimpleName(), object.getTableName(), object.getDatabaseId());

        r.table(object.getTableName())
                .get(object.getId())
                .delete()
                .runNoReply(conn);
    }
}
