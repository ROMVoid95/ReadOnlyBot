package net.readonly.options.base;

import net.readonly.options.OptionController;

public abstract class OptHandler {

    public abstract String description();
    
    protected void registerOption(Option option) {
    	OptionController.addOption(option);
    }
}
