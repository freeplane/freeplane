/**
 * author: Marcel Genzmehr
 * 24.07.2011
 */
package org.freeplane.plugin.workspace.view;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;

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

	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf,
			int row) {
		if (value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) value).getUserObject() instanceof AWorkspaceNode)
			return super.getTreeCellEditorComponent(tree,
					((AWorkspaceNode) ((DefaultMutableTreeNode) value).getUserObject()).getName(), isSelected, expanded, leaf,
					row);
		else
			return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);

	}

	public boolean isCellEditable(EventObject event) {
		if (event.getSource() instanceof JTree) {
			setTree((JTree) event.getSource());
			if (event instanceof MouseEvent) {
				TreePath path = tree.getPathForLocation(((MouseEvent) event).getX(), ((MouseEvent) event).getY());
				if (path != null) {
					DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
					if(treeNode.getUserObject() instanceof AWorkspaceNode) {
						if(!((AWorkspaceNode)treeNode.getUserObject()).isEditable()) {
							return false;
						}
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
