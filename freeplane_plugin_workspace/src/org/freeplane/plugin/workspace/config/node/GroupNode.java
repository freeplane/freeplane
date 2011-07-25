package org.freeplane.plugin.workspace.config.node;

import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;

public class GroupNode extends WorkspaceNode implements IWorkspaceNodeEventListener {

	public GroupNode(String id) {
		super(id); 
	}

	@Override
	public void handleEvent(WorkspaceNodeEvent event) {
		System.out.println("Event: "+event);
		
	}


}
