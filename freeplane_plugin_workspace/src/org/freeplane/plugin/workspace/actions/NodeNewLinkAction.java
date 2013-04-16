/**
 * author: Marcel Genzmehr
 * 11.11.2011
 */
package org.freeplane.plugin.workspace.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;

import javax.swing.JFileChooser;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.nodes.AFolderNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

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
		AWorkspaceTreeNode targetNode = null;
		if(e == null || getRootPopupMenu((Component) e.getSource()) == null) {
			targetNode = WorkspaceController.getCurrentProject().getModel().getRoot();
		}
		else {
			targetNode = getNodeFromActionEvent(e);
		}
		
		if(targetNode == null) {
			return;
		}
		AWorkspaceProject project = WorkspaceController.getProject(targetNode);
		if(targetNode instanceof AFolderNode) {
			JFileChooser chooser = new JFileChooser(URIUtils.getAbsoluteFile(((AFolderNode) targetNode).getPath() == null ? WorkspaceController.getCurrentProject().getProjectHome() : ((AFolderNode) targetNode).getPath()));
			chooser.setMultiSelectionEnabled(false);
			int response = chooser.showOpenDialog(UITools.getFrame());
			if(response == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if(file != null) {
					LinkTypeFileNode node = new LinkTypeFileNode();
					node.setName(file.getName());
					URI path = chooser.getSelectedFile().toURI();
					if (path == null) {
						return;
					}
					URI uri = project.getRelativeURI(path);
					if(uri == null) {
						node.setLinkURI(path);
					}
					else {
						node.setLinkURI(uri);
					}
					targetNode.getModel().addNodeTo(node, targetNode);
					targetNode.refresh();
					targetNode.getModel().requestSave();
				}
			}
		}
		
	}
}
