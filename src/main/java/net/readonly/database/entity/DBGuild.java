package net.readonly.database.entity;

import java.beans.ConstructorProperties;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.readonly.BotData;
import net.readonly.config.Config;
import net.readonly.database.ManagedObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DBGuild implements ManagedObject {
	
    public static final String DB_TABLE = "guilds";
    private final GuildData data;
    private final String id;
    
    @JsonIgnore
    private final Config config = BotData.config();
    
    @JsonCreator
    @ConstructorProperties({"id", "data"})
    public DBGuild(@JsonProperty("id") String id, @JsonProperty("data") GuildData data) {
        this.id = id;
        this.data = data;
    }
    
    public static DBGuild of(String id) {
        return new DBGuild(id, new GuildData());
    }

    public Guild getGuild(JDA jda) {
        return jda.getGuildById(getId());
    }
    
    @Nonnull
    @CheckReturnValue
    public DBGuild getGuild(@Nonnull Guild guild) {
        return DBGuild.of(guild.getId());
    }
    
    public GuildData getData() {
        return this.data;
    }

    @Override
	@Nonnull
    public String getId() {
        return this.id;
    }

    @JsonIgnore
    @Override
    @Nonnull
    public String getTableName() {
        return DB_TABLE;
    }
    
    public Config getConfig() {
        return this.config;
    }
}
