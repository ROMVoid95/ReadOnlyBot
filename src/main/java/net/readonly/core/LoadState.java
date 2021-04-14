package net.readonly.core;

public enum LoadState {
	PRELOAD("Pre Load"), 
	LOADING("Loading"), 
	LOADED("Loaded"),
	POSTLOAD("Ready");

	private final String s;

	LoadState(String s) {
		this.s = s;
	}

	@Override
	public String toString() {
		return s;
	}
}
