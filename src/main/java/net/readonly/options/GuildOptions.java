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
import net.readonly.options.annotations.Optionable;
import net.readonly.options.base.ActionType;
import net.readonly.options.base.OptHandler;
import net.readonly.options.base.Option;
import net.readonly.options.base.Option.Action;
import net.readonly.options.base.OptionSection;
import net.readonly.options.event.OptionRegisterEvent;
import net.readonly.utils.EmoteReference;

@Optionable
public class GuildOptions extends OptHandler {

	public GuildOptions() {
	}

	@Subscribe
	public void onRegistry(OptionRegisterEvent e) {
		Option PREFIX = Option.of(OptionSection.PREFIX, "prefix", "Controls the bot's prefix");
		Option _116_LISTEN = Option.of(OptionSection._116, "listen", "What channels the bot should listen in");
		Option _116_REPLY = Option.of(OptionSection._116, "reply", "Message replied with when 1.16+ is referenced");
		
		PREFIX.addAction(Action.ofType(ActionType.SET, "Sets the server prefix").setActionBiConsumer((ctx, args) -> {
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
		}));

		_116_LISTEN.addAction(Action.ofType(ActionType.ADD, "Adds channel(s) to listen for references to 1.16+")
				.setActionConsumer(((ctx) -> {
					List<TextChannel> mutable = new ArrayList<>(ctx.getMentionedTextChannels());

					if (mutable.isEmpty()) {
						ctx.sendLocalized("options.116_listen.no_channels", EmoteReference.ERROR);
						return;
					}
					List<TextChannel> permError = new ArrayList<>();
					for (TextChannel x : mutable) {
						if (ChannelPermission.WRITE.check(x)) {
							mutable.remove(x);
							permError.add(x);
						}
					}
					if (!permError.isEmpty()) {
						String errored = permError.stream().map(TextChannel::getAsMention)
								.collect(Collectors.joining(","));
						ctx.sendLocalized("options.116_listen.cannot_write", EmoteReference.ERROR, errored);
					}

					DBGuild dbGuild = ctx.getDBGuild();
					GuildData guildData = dbGuild.getData();
					List<String> channels = guildData.getListenChannels();
					for (TextChannel ch : mutable) {
						if (channels.contains(ch.getId())) {
							mutable.remove(ch);
						}
					}

					List<String> toList = mutable.stream().map(ISnowflake::getId).collect(Collectors.toList());
					String listed = mutable.stream().map(TextChannel::getAsMention).collect(Collectors.joining(","));

					channels.addAll(toList);
					dbGuild.save();

					ctx.sendLocalized("options.116_listen.add.success", EmoteReference.CORRECT, listed);
				})));

		_116_LISTEN.addAction(
				Action.ofType(ActionType.REMOVE, "Removes channel(s) to listen for references to 1.16+ Galacticraft")
						.setActionConsumer(((ctx) -> {
							List<TextChannel> mutable = new ArrayList<>(ctx.getMentionedTextChannels());

							if (mutable.isEmpty()) {
								ctx.sendLocalized("options.116_listen.no_channels", EmoteReference.ERROR);
								return;
							}

							DBGuild dbGuild = ctx.getDBGuild();
							GuildData guildData = dbGuild.getData();
							List<String> channels = guildData.getListenChannels();
							for (TextChannel ch : mutable) {
								if (channels.contains(ch.getId())) {
									mutable.remove(ch);
								}
							}

							List<String> toList = mutable.stream().map(ISnowflake::getId).collect(Collectors.toList());
							String listed = mutable.stream().map(TextChannel::getAsMention)
									.collect(Collectors.joining(","));

							channels.removeAll(toList);
							dbGuild.save();

							ctx.sendLocalized("options.116_listen.remove.success", EmoteReference.CORRECT, listed);
						})));

		_116_REPLY.addAction(Action.ofType(ActionType.SET, "Sets reply when 1.16+ is referenced")
				.setActionBiConsumer((ctx, args) -> {
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
				}));
		
		registerOption(_116_REPLY);
		registerOption(_116_LISTEN);
		registerOption(PREFIX);
	}

	@Override
	public String description() {
		return "Guild Options";
	}
}
