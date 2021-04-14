package net.readonly.utils.regex;

@FunctionalInterface
public interface IRegexPattern<T> {
	T check(String toCheck);
}
