package net.readonly.core.modules.commands.base;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.readonly.ReadOnlyBot;

public enum ChannelPermission {
	WRITE() {
		@Override
		public boolean check(TextChannel channel) {
			Guild guild = channel.getGuild();
			Member bot = guild.getMember(ReadOnlyBot.instance().getBotUser());
			return channel.canTalk(bot);
		}
	};
	
    public abstract boolean check(TextChannel channel);

    @Override
    public String toString() {
        String name = name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
