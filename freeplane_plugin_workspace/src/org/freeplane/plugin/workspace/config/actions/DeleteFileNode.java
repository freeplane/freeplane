package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspacePreferences;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;
import org.freeplane.plugin.workspace.view.WorkspacePopupMenu;

public class DeleteFileNode extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeleteFileNode() {
		super("DeleteFileNode");
	}

	public void actionPerformed(final ActionEvent e) {
		System.out.println("DeleteFileNode: " + e.getActionCommand() + " : " + e.getID());

		int yesorno = JOptionPane.OK_OPTION;
		yesorno = JOptionPane.showConfirmDialog(Controller.getCurrentController().getViewController().getContentPane(),
				TextUtils.getText("confirm_delete_file_action"),
				TextUtils.getText("confirm_delete_file_action_title"), JOptionPane.OK_CANCEL_OPTION);
		if (yesorno == JOptionPane.OK_OPTION) {
			deleteFile(e);
		}		
	}

	private void deleteFile(final ActionEvent e) {
		DefaultMutableTreeNode node = this.getNodeFromActionEvent(e);
		 if (node.getUserObject() instanceof DefaultFileNode) {
		 ((DefaultFileNode) node.getUserObject()).delete();
		 }
		
		 WorkspacePopupMenu menu = (WorkspacePopupMenu) ((JFreeplaneMenuItem)
		 e.getSource()).getParent();
		 JTree tree = (JTree) menu.getInvoker();
		 DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
		 treeModel.removeNodeFromParent(node);
	}

}
