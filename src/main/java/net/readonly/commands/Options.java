package net.readonly.commands;

import static net.readonly.core.modules.commands.base.CommandCategory.MODERATION;
import static net.readonly.core.modules.commands.base.CommandPermission.ADMIN;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.google.common.eventbus.Subscribe;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.readonly.core.CommandRegistry;
import net.readonly.core.modules.Module;
import net.readonly.core.modules.commands.SimpleCommand;
import net.readonly.core.modules.commands.base.CmdContext;
import net.readonly.core.modules.commands.base.Context;
import net.readonly.options.core.BotOption;
import net.readonly.utils.EmbedUtil;
import net.readonly.utils.EmoteReference;
import net.readonly.utils.StringUtils;

@Slf4j
@Module
public class Options {

	public static SimpleCommand optionsCmd;

	public static SimpleCommand getOptions() {
		return optionsCmd;
	}

	@Subscribe
	public void register(CommandRegistry registry) {
		registry.register("opt", optionsCmd = new SimpleCommand(CmdContext.of(MODERATION, ADMIN)) {
			@Override
			protected void call(Context ctx, String content, String[] args) {
				List<String> ars = Arrays.asList(args);
				for(String a : ars)  {
					log.info("ARG: " + a);
					log.info("ARG2: " + args[0]);
				}
				
				if (args.length == 0) {
					ctx.sendLocalized("options.error_general", EmoteReference.WARNING);
					return;
				}

				var languageContext = ctx.getLanguageContext();

				if (args.length == 1 && args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("ls")) {
					var builder = new StringBuilder();

					for (var opt : BotOption.getAvaliableOptions()) {
						builder.append(opt).append("\n");
					}

					var dividedMessages = EmbedUtil.divideString(builder);
					List<String> messages = new LinkedList<>();
					for (var msgs : dividedMessages) {
						messages.add(String.format(languageContext.get("commands.opts.list.header"),
								ctx.hasReactionPerms() ? languageContext.get("general.text_menu") + " "
										: languageContext.get("general.arrow_react"),
								String.format("```prolog\n%s```", msgs)));
					}

					if (ctx.hasReactionPerms()) {
						EmbedUtil.list(ctx.getEvent(), 45, false, messages);
					} else {
						EmbedUtil.listText(ctx.getEvent(), 45, false, messages);
					}

					return;
				}

				if (args.length < 2) {
					ctx.sendLocalized("options.error_general", EmoteReference.WARNING);
					return;
				}

				var name = new StringBuilder();

				if (args[0].equalsIgnoreCase("help")) {
					for (int i = 1; i < args.length; i++) {
						var s = args[i];
						if (name.length() > 0) {
							name.append(":");
						}

						name.append(s);
						var option = BotOption.getOptionMap().get(name.toString());

						if (option != null) {
							try {
								var builder = new EmbedBuilder()
										.setAuthor(option.getOptionName(), null,
												ctx.getAuthor().getEffectiveAvatarUrl())
										.setDescription(option.getDescription())
										.setThumbnail("https://i.imgur.com/lFTJSE4.png")
										.addField(EmoteReference.PENCIL.toHeaderString() + "Type",
												option.getType().toString(), false);

								ctx.send(builder.build());
							} catch (IndexOutOfBoundsException ignored) {
							}
							return;
						}
					}

					ctx.sendLocalized("commands.opts.option_not_found", EmoteReference.ERROR);
					return;
				}

				for (int i = 0; i < args.length; i++) {
					var str = args[i];
					if (name.length() > 0) {
						name.append(":");
					}

					name.append(str);
					var option = BotOption.getOptionMap().get(name.toString());

					if (option != null) {
						var callable = BotOption.getOptionMap().get(name.toString()).getEventConsumer();

						try {
							String[] a;
							if (++i < args.length) {
								a = Arrays.copyOfRange(args, i, args.length);
							} else {
								a = StringUtils.EMPTY_ARRAY;
							}

							callable.accept(ctx, a);
						} catch (IndexOutOfBoundsException ignored) {}
						return;
					}
				}

				ctx.sendLocalized("options.error_general", EmoteReference.WARNING);
			}
		});
	}
}
