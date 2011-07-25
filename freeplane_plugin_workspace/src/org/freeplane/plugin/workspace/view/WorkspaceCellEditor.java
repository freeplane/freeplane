/**
 * author: Marcel Genzmehr
 * 24.07.2011
 */
package org.freeplane.plugin.workspace.view;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.plugin.workspace.config.node.WorkspaceNode;

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
	
	public Component getTreeCellEditorComponent(JTree tree, Object value,
				boolean isSelected,
				boolean expanded,
				boolean leaf, int row) {
		if(value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode)value).getUserObject() instanceof WorkspaceNode)
			return super.getTreeCellEditorComponent(tree, ((WorkspaceNode)((DefaultMutableTreeNode)value).getUserObject()).getName(), isSelected, expanded, leaf, row);
		else 
			return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
