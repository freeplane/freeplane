package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;
import org.freeplane.plugin.workspace.view.WorkspacePopupMenu;

public class FileNodeRenameAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileNodeRenameAction() {
		super("FileNodeRenameAction");
	}

	public void actionPerformed(final ActionEvent e) {
		System.out.println("FileNodeRenameAction: "+e.getActionCommand()+" : "+e.getID());
		DefaultMutableTreeNode node = this.getNodeFromActionEvent(e);
		WorkspacePopupMenu menu = (WorkspacePopupMenu) ((JFreeplaneMenuItem) e.getSource()).getParent();
		JTree tree = (JTree) menu.getInvoker();			
		
		String oldFileName = ((AWorkspaceNode) node.getUserObject()).getName();		
		String newFileName = JOptionPane.showInputDialog(Controller.getCurrentController().getViewController().getContentPane(),
				TextUtils.getText("confirm_rename_file_action"), oldFileName);

		if (newFileName != null) {		
			if (node.getUserObject() instanceof DefaultFileNode) {
				if (((DefaultFileNode) node.getUserObject()).rename(newFileName)) {		
					DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
					
					treeModel.valueForPathChanged(new TreePath(node.getPath()), newFileName);
				}
				else {
					JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
							TextUtils.getText("error_rename_file"), TextUtils.getText("error_rename_file_title"), 
							JOptionPane.ERROR_MESSAGE);
				}

			}
		}

	}

}
