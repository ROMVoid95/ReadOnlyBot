package net.readonly.options.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.readonly.core.modules.commands.base.Context;

public class BotOption {
    private static final Map<String, BotOption> optionMap = new HashMap<>();
    //Display names + desc in the avaliable options list.
    private static final List<String> avaliableOptions = new ArrayList<>();
    private static String shortDescription = "Not set.";
    private final String description;
    private final String optionName;
    private final OptionType type;
    private BiConsumer<Context, String[]> eventConsumer;

    public BotOption(String displayName, String description, OptionType type) {
        this.optionName = displayName;
        this.description = description;
        this.type = type;
    }

    public static void addOption(String name, BotOption option) {
        BotOption.optionMap.put(name, option);
        String toAdd = String.format(
                "%-34s" + " | %s",
                name.replace(":", " "),
                getShortDescription()
        );
        BotOption.avaliableOptions.add(toAdd);
    }

    public static void addOptionAlias(String current, String name) {
        BotOption.optionMap.put(name, optionMap.get(current));
        String toAdd = String.format(
                "%-34s" + " | %s (Alias) ",
                name.replace(":", " "),
                getShortDescription()
        );
        BotOption.avaliableOptions.add(toAdd);
    }

    public static Map<String, BotOption> getOptionMap() {
        return BotOption.optionMap;
    }

    public static List<String> getAvaliableOptions() {
        return BotOption.avaliableOptions;
    }

    public static String getShortDescription() {
        return BotOption.shortDescription;
    }

    public BotOption setShortDescription(String sd) {
        shortDescription = sd;
        return this;
    }

    public BotOption setAction(Consumer<Context> code) {
        eventConsumer = (event, ignored) -> code.accept(event);
        return this;
    }

    public BotOption setAction(BiConsumer<Context, String[]> code) {
        eventConsumer = code;
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public String getOptionName() {
        return this.optionName;
    }

    public OptionType getType() {
        return this.type;
    }

    public BiConsumer<Context, String[]> getEventConsumer() {
        return this.eventConsumer;
    }
}
