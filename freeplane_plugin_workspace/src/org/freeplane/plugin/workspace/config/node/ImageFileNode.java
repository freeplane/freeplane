package org.freeplane.plugin.workspace.config.node;

import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEventListener;

public class ImageFileNode extends WorkspaceNode implements WorkspaceNodeEventListener {
	
	public ImageFileNode(String id) {
		super(id);
	}
	
	public String toString() {
		return this.getName();
	}

	@Override
	public void handleEvent(WorkspaceNodeEvent event) {
		// TODO Auto-generated method stub
		
	}

}
