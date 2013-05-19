/**
 * author: Marcel Genzmehr
 * 10.11.2011
 */
package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;

@CheckEnableOnPopup
public class NodeRemoveAction extends AWorkspaceAction {

	public static final String KEY = "workspace.action.node.remove";
	private static final long serialVersionUID = -8965412338727545850L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public NodeRemoveAction() {
		super(KEY);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void setEnabledFor(AWorkspaceTreeNode node, TreePath[] selectedPaths) {
		if(node.isSystem() || !node.isTransferable() || node instanceof DefaultFileNode) {
			setEnabled(false);
		}
		else{
			setEnabled();
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void actionPerformed(ActionEvent e) {
		AWorkspaceTreeNode[] targetNodes = getSelectedNodes(e);
		if(targetNodes.length <= 0) {
			return;
		}
		String question = "the selected nodes";
		if(targetNodes.length == 1) {
			question = targetNodes[0].getName();
		}
		int option = JOptionPane.showConfirmDialog(
				UITools.getFrame()
				,TextUtils.format("workspace.action.node.remove.confirm.text", question)
				,TextUtils.getRawText("workspace.action.node.remove.confirm.title")
				,JOptionPane.YES_NO_OPTION
				,JOptionPane.QUESTION_MESSAGE
		);
		if(option == JOptionPane.YES_OPTION) {			
			for (AWorkspaceTreeNode targetNode : targetNodes) {
				AWorkspaceTreeNode parent = targetNode.getParent();
				if(targetNode instanceof DefaultFileNode) {
					//WORKSPACE - info: used in case of key events
					((DefaultFileNode) targetNode).delete();
				}
				else {
					targetNode.getModel().removeNodeFromParent(targetNode);
				}
				if(parent != null) {
					parent.refresh();
				}
				
			}
			
		}
	}
}
