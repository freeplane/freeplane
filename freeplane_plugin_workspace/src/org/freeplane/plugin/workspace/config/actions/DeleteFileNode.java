package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.freeplane.core.ui.components.JFreeplaneMenuItem;
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
        System.out.println("DeleteFileNode: "+e.getActionCommand()+" : "+e.getID());
        DefaultMutableTreeNode node = this.getNodeFromActionEvent(e);
        if (node.getUserObject() instanceof DefaultFileNode) {
        	((DefaultFileNode) node.getUserObject()).delete();
        }
        
        WorkspacePopupMenu menu = (WorkspacePopupMenu) ((JFreeplaneMenuItem) e.getSource()).getParent();
		JTree tree = (JTree) menu.getInvoker();
		DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
		treeModel.removeNodeFromParent(node);
		
    }


}
