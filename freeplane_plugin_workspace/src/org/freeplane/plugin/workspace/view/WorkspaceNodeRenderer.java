package org.freeplane.plugin.workspace.view;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.FolderNode;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;

public class WorkspaceNodeRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Icon ACROBAT_ICON;
	private static Icon GRAPHICS_ICON;	
	private static Icon MINDMAP_ICON;
	private static Icon DEFAULT_ICON;
	private static Icon WEB_ICON;
	private static Icon DEFAULT_FOLDER_CLOSED_ICON;
	private static Icon DEFAULT_FOLDER_OPEN_ICON;

	public WorkspaceNodeRenderer() {
		if (ACROBAT_ICON == null) {
			DEFAULT_FOLDER_CLOSED_ICON = new ImageIcon(this.getClass().getResource("/images/16x16/folder-blue.png"));
			DEFAULT_FOLDER_OPEN_ICON = new ImageIcon(this.getClass().getResource("/images/16x16/folder-blue_open.png"));
			ACROBAT_ICON = new ImageIcon(this.getClass().getResource("/images/16x16/acrobat.png"));
			GRAPHICS_ICON = new ImageIcon(this.getClass().getResource("/images/16x16/image-x-generic.png"));
			DEFAULT_ICON = new ImageIcon(this.getClass().getResource("/images/16x16/text-x-preview.png"));
			WEB_ICON = new ImageIcon(this.getClass().getResource("/images/16x16/text-html-2.png"));
			if(ResourceController.getResourceController().getProperty("ApplicationName", "Freeplane").equals("Docear")) {
				MINDMAP_ICON = new ImageIcon(ResourceController.class.getResource("/images/docear16.png"));
			}
			else {
				MINDMAP_ICON = new ImageIcon(ResourceController.class.getResource("/images/Freeplane_frame_icon.png"));
			}			
		}
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		// super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
		// row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		setNodeIcon(renderer, node);
		Object obj = node.getUserObject();		
		JLabel label = (JLabel) renderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (obj != null) {
			label.setText(obj.toString());
			if (obj instanceof Boolean)
				label.setText("Retrieving data...");
			else if (obj instanceof AWorkspaceNode) {
				label.setText(((AWorkspaceNode) obj).getName());
			}
		}

		return label;
	}

	/**
	 * @param value
	 */
	protected void setNodeIcon(DefaultTreeCellRenderer renderer, DefaultMutableTreeNode node) {
		renderer.setOpenIcon(DEFAULT_FOLDER_OPEN_ICON);
		renderer.setClosedIcon(DEFAULT_FOLDER_CLOSED_ICON);
		Object userObject = node.getUserObject();
		if (userObject instanceof DefaultFileNode) {
			DefaultFileNode fileNode = (DefaultFileNode) userObject;
			if (fileNode.getFile().isFile()) {
				if (fileNode.getFileExtension().equalsIgnoreCase(".pdf")
						|| fileNode.getFileExtension().equalsIgnoreCase(".ps")) {
					renderer.setLeafIcon(ACROBAT_ICON);
				}
				else if (fileNode.getFileExtension().equalsIgnoreCase(".jpg")
						|| fileNode.getFileExtension().equalsIgnoreCase(".png")
						|| fileNode.getFileExtension().equalsIgnoreCase(".gif")
						|| fileNode.getFileExtension().equalsIgnoreCase(".bmp")
						|| fileNode.getFileExtension().equalsIgnoreCase(".jpeg")) {
					renderer.setLeafIcon(GRAPHICS_ICON);
				}
				else if (fileNode.getFileExtension().equalsIgnoreCase(".mm")
						|| fileNode.getFileExtension().equalsIgnoreCase(".dcr")) {
					renderer.setLeafIcon(MINDMAP_ICON);
				}
				else if (fileNode.getFileExtension().equalsIgnoreCase(".html")
						|| fileNode.getFileExtension().equalsIgnoreCase(".htm")
						|| fileNode.getFileExtension().equalsIgnoreCase(".css")
						|| fileNode.getFileExtension().equalsIgnoreCase(".xhtml")) {
					renderer.setLeafIcon(WEB_ICON);
				}			
				else {
					renderer.setLeafIcon(DEFAULT_ICON);
				}
				

			} else {
				renderer.setLeafIcon(DEFAULT_FOLDER_CLOSED_ICON);
			}
		}
		else if (userObject instanceof FolderNode) {
			renderer.setLeafIcon(DEFAULT_FOLDER_CLOSED_ICON);
		}
		else {
			renderer.setLeafIcon(DEFAULT_ICON);
		}
	}
	
}
