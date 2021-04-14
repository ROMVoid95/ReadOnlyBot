package net.readonly.database;

import java.util.List;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.readonly.BotData;

public class InitDatabase {
	
	private ManagedDatabase DB = BotData.db();
	private List<Guild> guilds;
	
	public InitDatabase(ReadyEvent event) {
		this.guilds = event.getJDA().getGuilds();
		checkDatabase();
	}
	
	private void checkDatabase() {
		for(Guild guild :  guilds) {
			boolean exists =  DB.checkGuild(guild.getId());
			if(!exists) {
	            final var dbGuild = DB.getGuild(guild);
	            final var data = dbGuild.getData();
	            data.setGuildName(guild.getName());
	            data.setGuildOwner(guild.getOwnerId());
	            dbGuild.save();
			}
		}
	}
}
