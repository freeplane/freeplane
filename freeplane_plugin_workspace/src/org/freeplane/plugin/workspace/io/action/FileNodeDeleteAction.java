package org.freeplane.plugin.workspace.io.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

public class FileNodeDeleteAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileNodeDeleteAction() {
		super("FileNodeDeleteAction");
	}

	public void actionPerformed(final ActionEvent e) {
		int yesorno = JOptionPane.showConfirmDialog(UITools.getFrame(),
				TextUtils.getText("confirm_delete_file_action"), 
				TextUtils.getText("confirm_delete_file_action_title"),
				JOptionPane.YES_NO_OPTION);
		if (yesorno == JOptionPane.OK_OPTION) {
			deleteFile(e);
		}
		WorkspaceUtils.saveCurrentConfiguration();	
	}

	private void deleteFile(final ActionEvent e) {
		AWorkspaceTreeNode node = this.getNodeFromActionEvent(e);
		if (node instanceof DefaultFileNode) {
			((DefaultFileNode) node).delete();
		}
		WorkspaceUtils.getModel().removeNodeFromParent(node);
	}

}
