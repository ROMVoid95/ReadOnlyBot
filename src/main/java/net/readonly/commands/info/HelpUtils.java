package net.readonly.commands.info;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.TextChannel;
import net.readonly.core.command.processor.CommandProcessor;
import net.readonly.core.modules.commands.base.CommandCategory;

public class HelpUtils {
	
    public static String forType(TextChannel channel, CommandCategory category) {
        return forType(
                CommandProcessor.REGISTRY.commands().entrySet().stream()
                        .filter(entry -> entry.getValue().category() == category)
                        .map(Entry::getKey)
                        .collect(Collectors.toList())
        );
    }
	
    public static String forType(List<String> values) {
        if (values.size() == 0) {
            return "`Disabled`";
        }

        return "\u2009\u2009`" + values.stream().sorted()
                .collect(Collectors.joining("` `")) + "`";
    }

}
