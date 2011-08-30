package org.freeplane.plugin.workspace.view;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.FolderNode;

public class WorkspaceNodeRenderer extends DefaultTreeCellRenderer {

	private int highlightedRow = -1;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Icon DEFAULT_ICON = new ImageIcon(WorkspaceNodeRenderer.class.getResource("/images/16x16/text-x-preview.png"));
	private static final Icon DEFAULT_FOLDER_CLOSED_ICON = new ImageIcon(WorkspaceNodeRenderer.class.getResource("/images/16x16/folder-blue.png"));
	private static final Icon DEFAULT_FOLDER_OPEN_ICON = new ImageIcon(WorkspaceNodeRenderer.class.getResource("/images/16x16/folder-blue_open.png"));
	
	public WorkspaceNodeRenderer() {
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		setNodeIcon(renderer, node);
		Object obj = node.getUserObject();		
		JLabel label = (JLabel) renderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (obj != null) {
			
			if (obj instanceof AWorkspaceNode) {
				label.setText(((AWorkspaceNode) obj).getName());
			} else {
				label.setText(obj.toString());
			}
		}
		if(row == this.highlightedRow) {
			label.setBorder(BorderFactory.createLineBorder(UIManager.getColor(borderSelectionColor), 1));
		} 
		//itemIndex.put(row, label);
		return label;
	}

	/**
	 * @param value
	 */
	protected void setNodeIcon(DefaultTreeCellRenderer renderer, DefaultMutableTreeNode node) {
		renderer.setOpenIcon(DEFAULT_FOLDER_OPEN_ICON);
		renderer.setClosedIcon(DEFAULT_FOLDER_CLOSED_ICON);
		Object userObject = node.getUserObject();
		if(userObject instanceof AWorkspaceNode) {
			AWorkspaceNode wsNode = (AWorkspaceNode) userObject;
			if(wsNode.setIcons(renderer)) {
				return;
			}
		}
		if(!node.isLeaf() || userObject instanceof FolderNode) {
			renderer.setLeafIcon(DEFAULT_FOLDER_CLOSED_ICON);
		} 
		else {
			renderer.setLeafIcon(DEFAULT_ICON);
		}
		
		
	}
	
	public void highlightRow(int row) {
		this.highlightedRow = row;
	}
}
