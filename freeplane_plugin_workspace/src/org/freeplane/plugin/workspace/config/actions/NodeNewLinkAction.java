/**
 * author: Marcel Genzmehr
 * 11.11.2011
 */
package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;

import javax.swing.JFileChooser;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.LinkTypeFileNode;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.node.AFolderNode;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

/**
 * 
 */
public class NodeNewLinkAction extends AWorkspaceAction {

	private static final long serialVersionUID = -2738773226743524919L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public NodeNewLinkAction() {
		super("workspace.action.node.new.link");
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void actionPerformed(ActionEvent e) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
		if(targetNode instanceof AFolderNode) {
			JFileChooser chooser = new JFileChooser(WorkspaceUtils.resolveURI(((AFolderNode) targetNode).getPath() == null ? WorkspaceUtils.getProfileBaseURI() : ((AFolderNode) targetNode).getPath()));
			chooser.setMultiSelectionEnabled(false);
			int response = chooser.showOpenDialog(UITools.getFrame());
			if(response == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if(file != null) {
					LinkTypeFileNode node = new LinkTypeFileNode();
					node.setName(file.getName());
					URI path = WorkspaceUtils.workspaceRelativeURI(chooser.getSelectedFile().toURI());
					if (path == null) {
						return;
					}	
					node.setLinkPath(path);
					WorkspaceUtils.getModel().addNodeTo(node, getNodeFromActionEvent(e));
					WorkspaceUtils.saveCurrentConfiguration();
					getNodeFromActionEvent(e).refresh();
				}
			}
		}
		
	}
}
