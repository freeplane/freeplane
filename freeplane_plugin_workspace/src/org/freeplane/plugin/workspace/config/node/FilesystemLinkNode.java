package org.freeplane.plugin.workspace.config.node;

import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;

public class FilesystemLinkNode extends WorkspaceNode implements IWorkspaceNodeEventListener{

	public FilesystemLinkNode(String id) {
		super(id);
	}

	@Override
	public void handleEvent(WorkspaceNodeEvent event) {
	}

}
