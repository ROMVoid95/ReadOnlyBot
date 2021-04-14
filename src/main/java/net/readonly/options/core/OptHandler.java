package net.readonly.options.core;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.base.Supplier;

import net.readonly.core.modules.commands.base.Context;
import net.readonly.options.annotations.Option;

public abstract class OptHandler {
	
	private OptionType optionType;

    public abstract String description();

    protected void registerOption(String name, String displayName, String description, Consumer<Context> code) {
        BotOption.addOption(name, new BotOption(displayName, description, optionType).setAction(code).setShortDescription(description));
    }

    protected void registerOption(String name, String displayName, String description, String shortDescription, Consumer<Context> code) {
        BotOption.addOption(name, new BotOption(displayName, description, optionType).setAction(code).setShortDescription(shortDescription));
    }

    protected void registerOption(String name, String displayName, String description, String shortDescription, BiConsumer<Context, String[]> code) {
        BotOption.addOption(name, new BotOption(displayName, description, optionType).setAction(code).setShortDescription(shortDescription));
    }

    protected void addOptionAlias(String original, String alias) {
        BotOption.addOptionAlias(original, alias);
    }

    public void setType(Class<? extends OptHandler> clazz) {
		this.optionType = clazz.getAnnotation(Option.class).type();
    }
    
    public void setType(Supplier<? extends OptHandler> supplier) {
		this.optionType = supplier.getClass().getAnnotation(Option.class).type();
    }
}
