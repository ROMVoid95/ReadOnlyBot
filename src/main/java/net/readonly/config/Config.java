package net.readonly.config;

import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@Data
@NoArgsConstructor
public class Config {

	private String botname = "ReadOnlyBot";
	private String avatarUrl = "https://i.imgur.com/S7CqwYg.png";
	private String token = "TOKEN";
	private String prefix = "PREFIX";
	private String clientId = "CLIENTID";
	private Boolean debug = false;
	private List<String> owners = Arrays.asList("OWNER-ID'S");
	private String jedisPoolAddress = "127.0.0.1";
	private int jedisPoolPort = 6379;
	private int prometheusPort = 9091;
	private String webhookUrl = "URL";
	private Database database = new Database();

	public boolean isOwner(Member member) {
		return isOwner(member.getUser());
	}

	public boolean isOwner(User user) {
		return isOwner(user.getId());
	}

	public boolean isOwner(String id) {
		return owners.contains(id);
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	public class Database {
		private String hostname = "localhost";
		private int port = 28015;
		private String databaseName = "readonly";
		private String user = "USER";
		private String password = "PASSWORD";
	}
}
