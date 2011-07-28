package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;

import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeDialog;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;
import org.freeplane.plugin.workspace.view.WorkspacePopupMenu;

public class RenameFileNode extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RenameFileNode() {
		super("RenameFileNode");
	}

	public void actionPerformed(final ActionEvent e) {
		DefaultMutableTreeNode node = this.getNodeFromActionEvent(e);
		
////		WorkspacePopupMenu menu = (WorkspacePopupMenu) ((JFreeplaneMenuItem) e.getSource()).getParent();
////		JTree tree = (JTree) menu.getInvoker();
////		TreeModel treeModel = tree.getModel();
////		treeModel.
//		
//		if(node.getUserObject() instanceof DefaultFileNode) {
//			System.out.println((DefaultFileNode)node.getUserObject());
//		}
//        System.out.println("RenameFileNode: "+e.getActionCommand()+" : "+e.getID());
//        
//        final EditNodeBase.IEditControl editControl = new EditNodeBase.IEditControl() {
//			public void cancel() {
//				System.out.println("CANCEL");
//				stop();
//			}
//
//			private void stop() {
//				Controller.getCurrentModeController().setBlocked(false);
//				System.out.println("STOP");
//				//mCurrentEditDialog = null;
//			}
//
//			public void ok(final String text) {
//				System.out.println("OK");
//				stop();
//			}
//
//			public void split(final String newText, final int position) {
//				System.out.println("SPLIT");
//				stop();
//			}
//		};
//        
//        final EditNodeDialog editNodeDialog = new EditNodeDialog((NodeModel) (MutableTreeNode) node, "BLA", null, editControl, false); 
//        	
//		editNodeDialog.setTitle(TextUtils.getText("formula_editor"));
//		//textEditor.setContentType("text");
//		editNodeDialog.show(null);
//		//return editNodeDialog;
    }

}
