package org.freeplane.plugin.workspace.config.node;

import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;

public class ImageFileNode extends WorkspaceNode implements IWorkspaceNodeEventListener {
	
	public ImageFileNode(String id) {
		super(id);
	}
	
	public String toString() {
		return this.getName();
	}

	@Override
	public void handleEvent(WorkspaceNodeEvent event) {
		
		
	}

}
