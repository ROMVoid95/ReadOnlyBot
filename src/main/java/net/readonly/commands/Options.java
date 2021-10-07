package net.readonly.commands;

import static net.readonly.core.modules.commands.base.CommandCategory.MODERATION;
import static net.readonly.core.modules.commands.base.CommandPermission.ADMIN;

import java.util.Arrays;
import java.util.List;

import com.google.common.eventbus.Subscribe;

import net.readonly.core.CommandRegistry;
import net.readonly.core.modules.Module;
import net.readonly.core.modules.commands.SimpleCommand;
import net.readonly.core.modules.commands.base.CmdContext;
import net.readonly.core.modules.commands.base.Context;
import net.readonly.options.OptionController;
import net.readonly.options.base.ActionType;
import net.readonly.options.base.Option.Action;
import net.readonly.utils.Builders;
import net.readonly.utils.Builders.OptionsBuilder;
import net.readonly.utils.EmoteReference;

@Module
public class Options {
	public static SimpleCommand optionsCmd;
	
	@Subscribe
	public void options(CommandRegistry cr) {
		cr.register("opt", optionsCmd = new SimpleCommand(CmdContext.of(MODERATION, ADMIN)) {
			@Override
			protected void call(Context ctx, String content, String[] args) {
				List<String> ars = Arrays.asList(args);
				
				if (args.length == 0) {
					ctx.sendLocalized("options.error_general", EmoteReference.WARNING);
					return;
				}

				if (args.length == 1 && args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("ls")) {
					OptionsBuilder optionsBuilder = new Builders.OptionsBuilder(ctx);
										
					optionsBuilder.embeds.forEach(e -> {
						ctx.getEvent().getChannel().sendMessage(e).queue();
					});
					return;
				}

				if (args.length < 2) {
					ctx.sendLocalized("options.error_general", EmoteReference.WARNING);
					return;
				}
				
				var bool = OptionController.getMap().containsKey(args[0]);
				if(bool) {
					var opt = OptionController.getMap().get(args[0]);
					var actionType = ActionType.valueOf(args[1]);
					if(actionType != null) {
						for(Action act : opt.getActions()) {
							if(act.getActionType().equals(actionType)) {
								var callable = act.getEventConsumer();
								ars.removeAll(Arrays.asList(args[0], args[1]));
								String[] array = new String[ars.size()];
								callable.accept(ctx, ars.toArray(array));
							}
						}
					}
				}
				ctx.sendLocalized("options.error_general", EmoteReference.WARNING);
			}
		});
	}
}
