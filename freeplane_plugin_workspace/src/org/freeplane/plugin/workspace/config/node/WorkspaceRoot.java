package org.freeplane.plugin.workspace.config.node;

public class WorkspaceRoot extends ConfigurationNode {

	public WorkspaceRoot(String id) {
		super(id);
	}
	
	public String toString() {
		return this.getName();
	}

}
