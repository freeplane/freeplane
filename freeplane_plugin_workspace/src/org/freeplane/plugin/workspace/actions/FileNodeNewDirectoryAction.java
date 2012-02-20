/**
 * author: Marcel Genzmehr
 * 15.11.2011
 */
package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.components.dialog.NewDirectoryDialogPanel;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.AFolderNode;
import org.freeplane.plugin.workspace.nodes.FolderFileNode;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;

/**
 * 
 */
@Deprecated
public class FileNodeNewDirectoryAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public FileNodeNewDirectoryAction() {
		super("workspace.action.file.new.directory");
	}

	
	/***********************************************************************************
	 * METHODS
	 * @param targetNode 
	 **********************************************************************************/

	/**
	 * @param targetNode
	 * @param parentDir
	 */
	private void makeNewDirectory(AWorkspaceTreeNode targetNode, File parentDir) {
		NewDirectoryDialogPanel newDirectoryDialog = new NewDirectoryDialogPanel(parentDir.getPath());
		int okOrCancel = JOptionPane.showConfirmDialog(UITools.getFrame(), 
		newDirectoryDialog, TextUtils.getRawText("workspace.action.file.new.directory.title")
		, JOptionPane.OK_CANCEL_OPTION
		, JOptionPane.PLAIN_MESSAGE);
		
		if(okOrCancel == JOptionPane.OK_OPTION) {
			try {
				WorkspaceController.getController().getFilesystemMgr().createDirectory(newDirectoryDialog.getDirectoryName(), parentDir);
				targetNode.refresh();
			}
			catch (IOException e1) {
				JOptionPane.showMessageDialog(UITools.getFrame(), e1.getMessage());
			}
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void actionPerformed(ActionEvent e) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
		if(targetNode instanceof FolderFileNode || (targetNode instanceof IFileSystemRepresentation && targetNode instanceof AFolderNode)) {
			File parentDir = ((IFileSystemRepresentation) targetNode).getFile();
			makeNewDirectory(targetNode, parentDir);
		} 
		else 
		if(targetNode instanceof FolderLinkNode && ((FolderLinkNode)targetNode).getPath() != null) {
			File parentDir = WorkspaceUtils.resolveURI(((FolderLinkNode)targetNode).getPath());
			makeNewDirectory(targetNode, parentDir);
			
		}
	}	
}
