/**
 * author: Marcel Genzmehr
 * 24.07.2011
 */
package org.freeplane.plugin.workspace.components;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.AFolderNode;

/**
 * 
 */
public class WorkspaceCellEditor extends DefaultTreeCellEditor {

	private static final Icon DEFAULT_ICON = new ImageIcon(WorkspaceNodeRenderer.class.getResource("/images/16x16/text-x-preview.png"));
	private static final Icon DEFAULT_FOLDER_CLOSED_ICON = new ImageIcon(WorkspaceNodeRenderer.class.getResource("/images/16x16/folder-blue.png"));
	private static final Icon DEFAULT_FOLDER_OPEN_ICON = new ImageIcon(WorkspaceNodeRenderer.class.getResource("/images/16x16/folder-blue_open.png"));
	
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

	public Component getTreeCellEditorComponent(JTree tree, Object treeNode, boolean isSelected, boolean expanded, boolean leaf,
			int row) {
		if (treeNode instanceof AWorkspaceTreeNode) {
			AWorkspaceTreeNode node = (AWorkspaceTreeNode) treeNode;
			setNodeIcon(renderer,node);
			return super.getTreeCellEditorComponent(tree, node.getName(), isSelected, expanded, leaf, row);	
		}
		return super.getTreeCellEditorComponent(tree, treeNode, isSelected, expanded, leaf, row);
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
		else if(event == null && WorkspaceController.getCurrentModeExtension().getView().getSelectionPath() != null) {
			return false;
		}
		return super.isCellEditable(event);
	}
	
	/**
	 * @param value
	 */
	protected void setNodeIcon(DefaultTreeCellRenderer renderer, AWorkspaceTreeNode wsNode) {
		renderer.setOpenIcon(DEFAULT_FOLDER_OPEN_ICON);
		renderer.setClosedIcon(DEFAULT_FOLDER_CLOSED_ICON);
		
		if(wsNode.setIcons(renderer)) {
			return;
		}		
		if(!wsNode.isLeaf() || wsNode instanceof AFolderNode) {
			renderer.setLeafIcon(DEFAULT_FOLDER_CLOSED_ICON);
		} 
		else {
			renderer.setLeafIcon(DEFAULT_ICON);
		}
		
		
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
