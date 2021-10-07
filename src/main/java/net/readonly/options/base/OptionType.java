package net.readonly.options.base;

import net.readonly.utils.StringUtils;

public enum OptionType {
	
	COMMAND, 
	GENERAL, 
	GUILD, 
	MODERATION;
	
    @Override
    public String toString() {
        return StringUtils.capitalize(this.name().toLowerCase());
    }
}
