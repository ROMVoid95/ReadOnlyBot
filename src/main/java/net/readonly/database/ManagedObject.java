package net.readonly.database;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.readonly.BotData;

public interface ManagedObject {
    @Nonnull
    String getId();

    @JsonIgnore
    @Nonnull
    String getTableName();

    @JsonIgnore
    @Nonnull
    default String getDatabaseId() {
        return getId();
    }
    
    default void delete() {
        BotData.db().delete(this);
    }

    /**
     * Saves an object to the database.
     * This will save the object by REPLACING it, instead of updating.
     * Useful sometimes.
     */
    default void save() {
    	BotData.db().save(this);
    }

    /**
     * Saves an object to the database.
     * This will save the object by updating it.
     * Useful sometimes.
     */
    default void saveUpdating() {
    	BotData.db().saveUpdating(this);
    }

    default void deleteAsync() {
    	BotData.queue(this::delete);
    }

    default void saveAsync() {
    	BotData.queue(this::save);
    }
}
