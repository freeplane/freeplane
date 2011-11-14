package org.docear.plugin.core.workspace.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JTree;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.config.node.AFolderNode;
import org.freeplane.plugin.workspace.config.node.VirtualFolderNode;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;

public class AddNewNodeAction extends AWorkspaceAction {

	public AddNewNodeAction() {
		super("workspace.action.node.new");		
	}

	public void actionPerformed(ActionEvent e) {		
//		WorkspaceUtils.createVirtualFolderNode("blubb", this.getNodeFromActionEvent(e));
		VirtualFolderNode node = new VirtualFolderNode(AFolderNode.FOLDER_TYPE_VIRTUAL);
		node.setName("TestNode");
		WorkspaceUtils.getModel().addNodeTo(node, getNodeFromActionEvent(e));
		WorkspaceUtils.getModel().reload(getNodeFromActionEvent(e));
		WorkspaceUtils.saveCurrentConfiguration();
	}

}
