package net.readonly.database.entity;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Category;

@Getter
@Setter
@NoArgsConstructor
public class GuildData {
	
	@ConfigName("Guild Name")
	private String guildName = "";
    @ConfigName("Guild Owner ID")
    private String guildOwner = "";
	
    @ConfigName("Disabled Categories")
    private List<Category> disabledCategories= new ArrayList<>();
    @ConfigName("Disabled Channels")
    private List<String> disabledChannels = new ArrayList<>();
    @ConfigName("Server language")
    private String lang = "en_US";

    @ConfigName("Server Admin Roles")
    private List<String> adminRoles = new ArrayList<>();
    @ConfigName("1.16 Listening Channels")
    private List<String> listenChannels = new ArrayList<>();
    @ConfigName("1.16 AutoReply")
    private String reply = "";
    @ConfigName("Custom Prefix")
    private String prefix = "--";

}
