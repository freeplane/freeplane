package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.AFolderNode;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;
import org.freeplane.plugin.workspace.nodes.WorkspaceRoot;

public class NodeNewDirectoryLinkAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1L;

	public NodeNewDirectoryLinkAction() {
		super("workspace.action.node.new.directory");
	}
	
	/**
	 * @param targetNode
	 * @param file
	 */
	private void makeNewDirectoryLink(AWorkspaceTreeNode targetNode, File file) {
		if(file == null) {
			return;
		}
		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setSelectedFile(file);

		int retVal = fileChooser.showOpenDialog(UITools.getFrame());
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File path = fileChooser.getSelectedFile();
			if (path != null) {
				FolderLinkNode node = new FolderLinkNode();
				String name = path.getName();				
				node.setName(name == null ? "directory" : name);			
				node.setPath(MLinkController.toLinkTypeDependantURI(WorkspaceUtils.getWorkspaceBaseFile(), path, LinkController.LINK_RELATIVE_TO_WORKSPACE));
				WorkspaceUtils.getModel().addNodeTo(node, targetNode);
				WorkspaceUtils.saveCurrentConfiguration();
				node.refresh();
			}			
		}		
		targetNode.refresh();
	} 
	

	public void actionPerformed(final ActionEvent e) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
		if(targetNode == null) {
			targetNode = (AWorkspaceTreeNode) WorkspaceUtils.getModel().getRoot();
		}
		if(targetNode instanceof AFolderNode) {			
			File file = WorkspaceUtils.resolveURI(((AFolderNode) targetNode).getPath() == null ? WorkspaceUtils.getProfileBaseURI() : ((AFolderNode) targetNode).getPath());
			makeNewDirectoryLink(targetNode, file);
		}
		else
		if(targetNode instanceof WorkspaceRoot) {
			File file = WorkspaceUtils.getWorkspaceBaseFile();
			makeNewDirectoryLink(targetNode, file);
		}
	}	
}
