package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.AFolderNode;
import org.freeplane.plugin.workspace.config.node.PhysicalFolderNode;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class NodeNewDirectoryLinkAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1L;

	public NodeNewDirectoryLinkAction() {
		super("workspace.action.node.new.directory");
	}

	public void actionPerformed(final ActionEvent e) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
		if(targetNode instanceof AFolderNode) {
			JFileChooser fileChooser = new JFileChooser();		
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			File file = WorkspaceUtils.resolveURI(((AFolderNode) targetNode).getPath() == null ? WorkspaceUtils.getWorkspaceBaseURI() : ((AFolderNode) targetNode).getPath());
			if(file == null) {
				return;
			}
			fileChooser.setSelectedFile(file);
	
			int retVal = fileChooser.showOpenDialog(UITools.getFrame());
			if (retVal == JFileChooser.APPROVE_OPTION) {
				File path = fileChooser.getSelectedFile();
				if (path != null) {
					PhysicalFolderNode node = new PhysicalFolderNode();
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
	}

}
