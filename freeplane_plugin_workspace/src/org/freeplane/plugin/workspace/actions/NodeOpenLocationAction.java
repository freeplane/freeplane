package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.WorkspaceRoot;

public class NodeOpenLocationAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NodeOpenLocationAction() {
		super("workspace.action.node.open.location");
	}
	
	public void actionPerformed(ActionEvent event) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(event);
		if(targetNode instanceof IFileSystemRepresentation) {
			openFolder(((IFileSystemRepresentation) targetNode).getFile());
		}
		else if(targetNode instanceof WorkspaceRoot) {
			openFolder(WorkspaceUtils.getWorkspaceBaseFile());
		}
	}

	protected void openFolder(File folder) {
		try {
			Controller.getCurrentController().getViewController().openDocument(Compat.fileToUrl(folder));
		} catch (Exception e) {
			LogUtils.warn(e);
		}
	}

}
