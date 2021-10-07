package net.readonly.options.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.readonly.core.modules.commands.base.Context;

public class Option {
	
	private final OptionType type;
	private final String description;
	private final String name;
	private final OptionSection optionSection;
	private List<Action> optionActions = new ArrayList<>();
	
    private Option(OptionType type, OptionSection optionSection, String name, String description) {
    	this.optionSection = optionSection;
        this.name = name;
        this.description = description;
        this.type = type;
    }
    
    public static Option of(OptionSection optionSection, String name, String description) {
    	return new Option(OptionType.GUILD, optionSection, name, description);
    }
    
    public Option addAction(Action action) {
    	this.optionActions.add(action);
    	return this;
    }
    
    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public OptionType getType() {
        return this.type;
    }

	public OptionSection getSection() {
		return this.optionSection;
	}

	public List<Action> getActions() {
		return this.optionActions;
	}
	
	public Field getAsField() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.description + "\n");
		for(Action action : this.optionActions) {
			action.getFormatted(builder);
			builder.append("\n");
		}
		builder.append("\n");
		return new Field(this.name, builder.toString(), false);
	}
	
	public static class Action {

		private final ActionType actionType;
		private final String description;
		
		private BiConsumer<Context, String[]> eventConsumer;
		
		private Action(ActionType actionType, String description) {
			this.actionType = actionType;
			this.description = description;
		}
		
		public static Action ofType(ActionType actionType, String description) {
			return new Action(actionType, description);
		}
		
	    public Action setActionConsumer(Consumer<Context> code) {
	        eventConsumer = (event, ignored) -> code.accept(event);
	        return this;
	    }

	    public Action setActionBiConsumer(BiConsumer<Context, String[]> code) {
	        eventConsumer = code;
	        return this;
	    }

		public String getDescription() {
			return this.description;
		}

		public ActionType getActionType() {
			return this.actionType;
		}
		
	    public BiConsumer<Context, String[]> getEventConsumer() {
	        return this.eventConsumer;
	    }
		
		public void getFormatted(StringBuilder builder) {
			builder.append("    ");
			builder.append(String.format("%-s %s","`" + this.actionType.toString() + "`", this.description));
		}
	}
}
