package net.readonly.options.base;

public enum ActionType {
	ADD,
	REMOVE,
	SET,
	RESET,
	VALIDATE;
	
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}
