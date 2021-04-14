package net.readonly.options.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.readonly.options.core.OptionType;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Option {
	OptionType type() default OptionType.GENERAL;
}
