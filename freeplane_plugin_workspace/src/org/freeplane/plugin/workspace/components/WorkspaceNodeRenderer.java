package org.freeplane.plugin.workspace.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.ClipboardOwner;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.plugin.workspace.dnd.DnDController;
import org.freeplane.plugin.workspace.dnd.IWorspaceClipboardOwner;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.AFolderNode;

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

	public Component getTreeCellRendererComponent(JTree tree, Object treeNode, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if(treeNode != null && treeNode instanceof AWorkspaceTreeNode ) {
			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
			AWorkspaceTreeNode node = (AWorkspaceTreeNode) treeNode;
			setNodeIcon(renderer, node);
			setToolTip(renderer, node);
			JLabel label = (JLabel) renderer.getTreeCellRendererComponent(tree, treeNode, sel, expanded, leaf, row, hasFocus);			
			if(row == this.highlightedRow) {
				try {
				label.setBorder(BorderFactory.createLineBorder(UIManager.getColor(borderSelectionColor), 1));
				} 
				catch (Exception e) {
					label.setBorder(BorderFactory.createLineBorder(label.getForeground(), 1));
				}
			}
			label.setText(node.getName());
			if(isCut(node)) {
				//WORKSPACE - ToDo: make the item transparent (including the icon?)
				int alpha = new Double(255 * 0.5).intValue();
				label.setForeground(new Color(label.getForeground().getRed(), label.getForeground().getGreen(), label.getForeground().getBlue(), alpha));
			}
			return label;
		}
		return super.getTreeCellRendererComponent(tree, treeNode, sel, expanded, leaf, row, hasFocus);
	}
	
	private boolean isCut(AWorkspaceTreeNode node) {
		ClipboardOwner owner = DnDController.getSystemClipboardController().getClipboardOwner();
		if(owner != null && owner instanceof IWorspaceClipboardOwner) {
			if(!((IWorspaceClipboardOwner) owner).getTransferable().isCopy() && ((IWorspaceClipboardOwner) owner).getTransferable().contains(node)) {
				return true;
			}
		}
		return false;
	}

	private void setToolTip(DefaultTreeCellRenderer renderer, AWorkspaceTreeNode node) {
		if(node instanceof IFileSystemRepresentation) {
			renderer.setToolTipText(((IFileSystemRepresentation) node).getFile().getPath());
		}
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
	
	public void highlightRow(int row) {
		this.highlightedRow = row;
	}
}
