/**
 * author: Marcel Genzmehr
 * 24.07.2011
 */
package org.freeplane.plugin.workspace.view;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

/**
 * 
 */
public class WorkspaceCellEditor extends DefaultTreeCellEditor {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/**
	 * @param tree
	 * @param renderer
	 */
	public WorkspaceCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
		super(tree, renderer);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public Component getTreeCellEditorComponent(JTree tree, Object node, boolean isSelected, boolean expanded, boolean leaf,
			int row) {
		if (node instanceof AWorkspaceTreeNode)
			return super.getTreeCellEditorComponent(tree,
					((AWorkspaceTreeNode) node).getName(), isSelected, expanded, leaf,
					row);
		else
			return super.getTreeCellEditorComponent(tree, node, isSelected, expanded, leaf, row);

	}

	public boolean isCellEditable(EventObject event) {		
		if (event != null && event.getSource() instanceof JTree) {
			setTree((JTree) event.getSource());
			if (event instanceof MouseEvent) {
				TreePath path = tree.getPathForLocation(((MouseEvent) event).getX(), ((MouseEvent) event).getY());
				if (path != null) {
					AWorkspaceTreeNode treeNode = (AWorkspaceTreeNode) path.getLastPathComponent();
					if(!treeNode.isEditable()) {
						return false;
					}					
				}
			}
		}
		return super.isCellEditable(event);
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
