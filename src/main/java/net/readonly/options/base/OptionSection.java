package net.readonly.options.base;

public enum OptionSection {
	
	PREFIX("Prefix"),
	_116("116");
	
	private final String name;
	
	private OptionSection(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
