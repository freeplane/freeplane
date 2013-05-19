package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

@CheckEnableOnPopup
public class FileNodeDeleteAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileNodeDeleteAction() {
		super("workspace.action.file.delete");
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void setEnabledFor(AWorkspaceTreeNode node, TreePath[] selectedPath) {
		if(node.isSystem()|| !node.isTransferable() || (!(node instanceof DefaultFileNode) && !(node instanceof LinkTypeFileNode))) {
			setEnabled(false);
		}
		else{
			setEnabled();
		}
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void actionPerformed(final ActionEvent e) {
		AWorkspaceTreeNode node = this.getNodeFromActionEvent(e);
		int yesorno = JOptionPane.showConfirmDialog(UITools.getFrame(),
				TextUtils.format("workspace.action.file.delete.confirm.text", node.getName()), 
				TextUtils.getText("workspace.action.file.delete.confirm.title"),
				JOptionPane.YES_NO_OPTION);
		if (yesorno == JOptionPane.OK_OPTION) {
			deleteFile(node);
		}	
	}

	private void deleteFile(final AWorkspaceTreeNode node) {
		if (node instanceof DefaultFileNode) {
			((DefaultFileNode) node).delete();
		} 
		else if (node instanceof LinkTypeFileNode) {						
			File file = URIUtils.getAbsoluteFile(((LinkTypeFileNode) node).getLinkURI());
			if(file != null) {
				if(!file.delete()) {
					//show message?
				}
			}			
		}
		AWorkspaceTreeNode parent = node.getParent();
		node.getModel().removeNodeFromParent(node);
		parent.refresh();
		parent.getModel().requestSave();
		
	}

}
