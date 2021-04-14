package net.readonly.utils;

@FunctionalInterface
public interface IntPairFunction<T> {
	T apply(int i, int j);
}
