package net.readonly.core.modules.commands.base;

public class CmdContext<CAT, PERM> {

	private CAT commandCategory;
	private PERM commandPermission;
	
	private CmdContext(CAT commandCategory, PERM commandPermission) {
		this.commandCategory = commandCategory;
		this.commandPermission = commandPermission;
	}
	
	public static <CAT, PERM> CmdContext<CAT, PERM> of(CAT commandCategory, PERM commandPermission) {
		return new CmdContext<>(commandCategory, commandPermission);
	}
	
	public CAT getCommandCategory() {
		return commandCategory;
	}

	public PERM getCommandPermission() {
		return commandPermission;
	}
	
}
