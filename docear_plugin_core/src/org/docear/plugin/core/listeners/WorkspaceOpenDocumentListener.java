package org.docear.plugin.core.listeners;

import java.io.File;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.ALinkNode;

public class WorkspaceOpenDocumentListener implements IWorkspaceNodeActionListener {	
	
	public void handleAction(WorkspaceActionEvent event) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(event);
		if(targetNode != null) {
			File f = null;
			if(targetNode instanceof IFileSystemRepresentation) {
				f = ((IFileSystemRepresentation) targetNode).getFile();
			} 
			else if(targetNode instanceof ALinkNode) {				
				f = WorkspaceUtils.resolveURI(((ALinkNode) targetNode).getLinkPath());
			}
			if (f != null && !f.getName().endsWith(".mm")) {
				DocearController.getController().getDocearEventLogger().write(this, DocearLogEvent.FILE_OPENED, f);
			}
		}

	}
	
	protected AWorkspaceTreeNode getNodeFromActionEvent(WorkspaceActionEvent e) {
		int x = e.getX();
		int y = e.getY();
		JTree tree = WorkspaceController.getController().getWorkspaceViewTree();
		TreePath path = tree.getPathForLocation(x, y);
		if(path == null) {
			return null;
		}
		return (AWorkspaceTreeNode) path.getLastPathComponent();
	}

}
