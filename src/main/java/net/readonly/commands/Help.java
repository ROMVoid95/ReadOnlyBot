package net.readonly.commands;

import static net.readonly.commands.info.HelpUtils.forType;
import static net.readonly.utils.EmoteReference.BLUE_SMALL_MARKER;

import java.awt.Color;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.eventbus.Subscribe;

import net.dv8tion.jda.api.EmbedBuilder;
import net.readonly.core.CommandRegistry;
import net.readonly.core.command.processor.CommandProcessor;
import net.readonly.core.modules.Module;
import net.readonly.core.modules.commands.AliasCommand;
import net.readonly.core.modules.commands.SimpleCommand;
import net.readonly.core.modules.commands.base.CommandCategory;
import net.readonly.core.modules.commands.base.CommandPermission;
import net.readonly.core.modules.commands.base.Context;
import net.readonly.core.modules.commands.base.ITreeCommand;
import net.readonly.core.modules.commands.help.HelpContent;
import net.readonly.utils.EmoteReference;

@Module
public class Help {
	private void buildHelp(Context ctx, CommandCategory category)  {
		var languageContext = ctx.getLanguageContext();
		
        // Start building the help description.
        var description = new StringBuilder();
        if (category == null) {
            description.append(languageContext.get("commands.help.base"));
        } else {
            description.append(languageContext.get("commands.help.base_category")
                    .formatted(languageContext.get(category.toString()))
            );
        }
        // End of help description.
        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(languageContext.get("commands.help.title"), null, ctx.getGuild().getIconUrl())
                .setColor(Color.PINK)
                .setDescription(description.toString())
                .setFooter(languageContext.get("commands.help.footer").formatted(
                        "❤️", CommandProcessor.REGISTRY.commands()
                                .values()
                                .stream()
                                .filter(c -> c.category() != null)
                                .count()
                ), ctx.getGuild().getIconUrl());

        Arrays.stream(CommandCategory.values())
                .filter(c -> {
                    if (category != null) {
                        return c == category;
                    } else {
                        return true;
                    }
                })
                .filter(c -> c != CommandCategory.OWNER || CommandPermission.OWNER.test(ctx.getMember()))
                .filter(c -> !CommandProcessor.REGISTRY.getCommandsForCategory(c).isEmpty())
                .forEach(c ->
                        embed.addField(
                                languageContext.get(c.toString()) + " " + languageContext.get("commands.help.commands") + ":",
                                forType(ctx.getChannel(), c), false
                        )
                );

        ctx.send(embed.build());
	}
	
    @Subscribe
    public void help(CommandRegistry cr) {
        Random r = new Random();
        java.util.List<String> jokes = List.of(
                "Yo damn I heard you like help, because you just issued the help command to get the help about the help command.",
                "Helps you to help yourself.",
                "Help Inception.",
                "A help helping helping helping help.",
                "Halp!"
        );
        
        cr.register("help", new SimpleCommand(CommandCategory.INFO) {
            @Override
            protected void call(Context ctx, String content, String[] args) {
                var commandCategory = CommandCategory.lookupFromString(content);

                if (content.isEmpty()) {
                    buildHelp(ctx, null);
                } else if (commandCategory != null) {
                    buildHelp(ctx, commandCategory);
                } else {
                    var member = ctx.getMember();
                    var command = CommandProcessor.REGISTRY.commands().get(content);

                    if (command == null) {
                        ctx.sendLocalized("commands.help.extended.not_found", EmoteReference.ERROR);
                        return;
                    }

                    if (command.isOwnerCommand() && !CommandPermission.OWNER.test(member)) {
                        ctx.sendLocalized("commands.help.extended.not_found", EmoteReference.ERROR);
                        return;
                    }

                    var help = command.help();

                    if (help == null || help.getDescription() == null) {
                        ctx.sendLocalized("commands.help.extended.no_help", EmoteReference.ERROR);
                        return;
                    }

                    var descriptionList = help.getDescriptionList();

                    var desc = new StringBuilder();

                    if (descriptionList.isEmpty()) {
                        desc.append(help.getDescription());
                    }
                    else {
                        desc.append(descriptionList.get(r.nextInt(descriptionList.size())));
                    }

                    desc.append("\n").append("**Don't include <> or [] on the command itself.**");

                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(Color.PINK)
                            .setAuthor("Command help for " + content, null,
                                    ctx.getAuthor().getEffectiveAvatarUrl()
                            ).setDescription(desc);

                    if (help.getUsage() != null) {
                        builder.addField(EmoteReference.PENCIL.toHeaderString() + "Usage", help.getUsage(), false);
                    }

                    if (help.getParameters().size() > 0) {
                        builder.addField(EmoteReference.SLIDER.toHeaderString() + "Parameters", help.getParameters().entrySet().stream()
                                        .map(entry -> "`%s` - *%s*".formatted(entry.getKey(), entry.getValue()))
                                        .collect(Collectors.joining("\n")), false
                        );
                    }

                    // Ensure sub-commands show in help.
                    // Only god shall help me now with all of this casting lol.
                    if (command instanceof AliasCommand) {
                        command = ((AliasCommand) command).getCommand();
                    }

                    if (command instanceof ITreeCommand) {
                        var subCommands =
                                ((ITreeCommand) command).getSubCommands()
                                        .entrySet()
                                        .stream()
                                        .sorted(Comparator.comparingInt(a ->
                                                a.getValue().description() == null ? 0 : a.getValue().description().length())
                                        ).collect(
                                        Collectors.toMap(
                                                Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new
                                        )
                                );

                        var stringBuilder = new StringBuilder();

                        for (var inners : subCommands.entrySet()) {
                            var name = inners.getKey();
                            var inner = inners.getValue();
                            if (inner.isChild()) {
                                continue;
                            }

                            if (inner.description() != null) {
                                stringBuilder.append("""
                                        %s`%s` - %s
                                        """.formatted(BLUE_SMALL_MARKER, name, inner.description())
                                );
                            }
                        }

                        if (stringBuilder.length() > 0) {
                            builder.addField(EmoteReference.ZAP.toHeaderString() + "Sub-commands",
                                    "**Append the main command to use any of this.**\n" + stringBuilder,
                                    false
                            );
                        }
                    }

                    //Known command aliases.
                    var commandAliases = command.getAliases();
                    if (!commandAliases.isEmpty()) {
                        String aliases = commandAliases
                                .stream()
                                .filter(alias -> !alias.equalsIgnoreCase(content))
                                .map("`%s`"::formatted)
                                .collect(Collectors.joining(" "));

                        if (!aliases.trim().isEmpty()) {
                            builder.addField(EmoteReference.FORK.toHeaderString() + "Aliases", aliases, false);
                        }
                    }

                    builder.setTimestamp(Instant.now());

                    ctx.send(builder.build());
                }
            }

            @Override
            public HelpContent help() {
                return new HelpContent.Builder()
                        .setDescription("halp!")
                        .setDescriptionList(jokes)
                        .setUsage("help <command>")
                        .addParameter("command", "The command name of the command you want to check information about.")
                        .build();
            }
        });
    }
}
