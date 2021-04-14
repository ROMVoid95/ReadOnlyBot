package net.readonly.options;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.eventbus.Subscribe;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.TextChannel;
import net.readonly.core.modules.commands.base.ChannelPermission;
import net.readonly.database.entity.DBGuild;
import net.readonly.database.entity.GuildData;
import net.readonly.options.annotations.Option;
import net.readonly.options.core.OptHandler;
import net.readonly.options.core.OptionType;
import net.readonly.options.event.OptionEvent;
import net.readonly.utils.EmoteReference;

@Option(type = OptionType.GUILD)
public class GuildOptions extends OptHandler {

	public GuildOptions() {
		setType(GuildOptions::new);
	}

	@Subscribe
	public void onRegistry(OptionEvent.Register e) {
		registerOption("prefix:set", "Prefix set", """
				Sets the server prefix.
				**Example:** `--opts prefix set .`
				""", "Sets the server prefix.", (ctx, args) -> {
			if (args.length < 1) {
				ctx.sendLocalized("options.prefix_set.no_prefix", EmoteReference.ERROR);
				return;
			}

			String prefix = args[0];

			if (prefix.length() > 50) {
				ctx.sendLocalized("options.prefix_set.too_long", EmoteReference.ERROR);
				return;
			}

			if (prefix.isEmpty()) {
				ctx.sendLocalized("options.prefix_set.empty_prefix", EmoteReference.ERROR);
				return;
			}

			if (prefix.equals("/tts")) {
				var tts = ctx.getSelfMember().hasPermission(ctx.getChannel(), Permission.MESSAGE_TTS);
				ctx.getChannel().sendMessage("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwww").tts(tts).queue();
				return;
			}

			if (prefix.equals("/shrug") || prefix.equals("¯\\_(ツ)_/¯")) {
				ctx.send("¯\\_(ツ)_/¯");
				return;
			}

			DBGuild dbGuild = ctx.getDBGuild();
			GuildData guildData = dbGuild.getData();
			guildData.setPrefix(prefix);
			dbGuild.save();

			ctx.sendLocalized("options.prefix_set.success", EmoteReference.MEGA, prefix);
		});

		registerOption("116:reply", "116 Auto Reply",
				"Set reply when 1.16+ is referenced\n"
						+ "**Example:** `--option 116 reply $(event.user.tag) 1.16+ Galacticraft is Under Development`",
				"Set reply when 1.16+ is referenced", (ctx, args) -> {
					if (args.length == 0) {
						ctx.sendLocalized("options.logs_banmessage.no_message", EmoteReference.ERROR);
						return;
					}

					DBGuild dbGuild = ctx.getDBGuild();
					GuildData guildData = dbGuild.getData();

					if (args[0].equals("reset")) {
						guildData.setReply("$(event.user.tag) 1.16+ Galacticraft is Under Development");
						dbGuild.save();
						ctx.sendLocalized("options.116_reply.reset_success", EmoteReference.CORRECT);
						return;
					}

					String replyMessage = String.join(" ", args);
					guildData.setReply(replyMessage);
					dbGuild.save();
					ctx.sendLocalized("options.116_reply.success", EmoteReference.CORRECT, replyMessage);
				});
		addOptionAlias("116:reply", "replymessage");

		registerOption("116:listen:add", "Listen Channel add", """
				Adds channel(s) to listen for references to 1.16+ Galacticraft
				**Example:** `--option 116 listen add #[channel] #[channel]...`
				""", "Adds channel(s) to listen for references to 1.16+ Galacticraft", ((ctx, args) -> {
			List<TextChannel> mutable = new ArrayList<>(ctx.getMentionedTextChannels());
					
			if (mutable.isEmpty()) {
				ctx.sendLocalized("options.116_listen.no_channels", EmoteReference.ERROR);
				return;
			}
            if (mutable.stream().anyMatch(ChannelPermission.WRITE::check)) {
                ctx.sendLocalized("options.116_listen.cannot_write", EmoteReference.ERROR);
                return;
            }
            
			DBGuild dbGuild = ctx.getDBGuild();
			GuildData guildData = dbGuild.getData();
			List<String> channels = guildData.getListenChannels();
			for(TextChannel ch : mutable) {
				if(channels.contains(ch.getId())) {
					mutable.remove(ch);
				}
			}

			List<String> toList = mutable.stream().map(ISnowflake::getId).collect(Collectors.toList());
            String listed = mutable.stream().map(TextChannel::getAsMention).collect(Collectors.joining(","));

            channels.addAll(toList);
            dbGuild.save();
            
            ctx.sendLocalized("options.116_listen_add.success", EmoteReference.CORRECT, listed);
		}));
		registerOption("116:listen:remove", "Listen Channel remove", """
				Removes channel(s) to listen for references to 1.16+ Galacticraft
				**Example:** `--option 116 listen remove #[channel] #[channel]...`
				""", "Removes channel(s) to listen for references to 1.16+ Galacticraft", ((ctx, args) -> {
				List<TextChannel> mutable = new ArrayList<>(ctx.getMentionedTextChannels());
					
			if (mutable.isEmpty()) {
				ctx.sendLocalized("options.116_listen.no_channels", EmoteReference.ERROR);
				return;
			}
            
			DBGuild dbGuild = ctx.getDBGuild();
			GuildData guildData = dbGuild.getData();
			List<String> channels = guildData.getListenChannels();
			for(TextChannel ch : mutable) {
				if(channels.contains(ch.getId())) {
					mutable.remove(ch);
				}
			}

			List<String> toList = mutable.stream().map(ISnowflake::getId).collect(Collectors.toList());
            String listed = mutable.stream().map(TextChannel::getAsMention).collect(Collectors.joining(","));

            channels.removeAll(toList);
            dbGuild.save();
            
            ctx.sendLocalized("options.116_listen_remove.success", EmoteReference.CORRECT, listed);
		}));
	}

	@Override
	public String description() {
		return "Guild Options";
	}
}
