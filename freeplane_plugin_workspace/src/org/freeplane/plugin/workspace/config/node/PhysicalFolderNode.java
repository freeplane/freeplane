package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;
import java.io.File;
import java.net.URI;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.PopupMenus;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

public class PhysicalFolderNode extends AWorkspaceNode implements IWorkspaceNodeEventListener {
	private String folderPathProperty;
	private URI folderPath;

	private static String POPUP_KEY = "/filesystem_folder";

	public PhysicalFolderNode(String id) {
		super(id);
		initializePopup();
	}

	@ExportAsAttribute("pathProperty")
	public String getFolderPathProperty() {
		return folderPathProperty;
	}

	public void setFolderPathProperty(String pathProperty) {
		this.folderPathProperty = pathProperty;
	}

	@ExportAsAttribute("path")
	public URI getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(URI folderPath) {
		this.folderPath = folderPath;
	}

	public static void refresh(DefaultMutableTreeNode treeNode) {
		// if folder path is not correctly set
		if(treeNode.getUserObject() instanceof PhysicalFolderNode) {
			PhysicalFolderNode node = (PhysicalFolderNode) treeNode.getUserObject();
			if (node.getFolderPath() == null) {
				return;
			}
	
			File folder;
			WorkspaceController controller = WorkspaceController.getCurrentWorkspaceController();
			try {
				folder = WorkspaceUtils.resolveURI(node.getFolderPath());
				if (folder.isDirectory()) {
					treeNode.removeAllChildren();
					controller.getFilesystemReader().scanFilesystem(node, folder);
					if(controller.getViewModel() != null) {
						controller.getViewModel().reload(treeNode);
					}
				}
	
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getTagName() {
		return "filesystem_folder";
	}

	private void initializePopup() {
		PopupMenus popupMenu = WorkspaceController.getCurrentWorkspaceController().getPopups();
		popupMenu.registerPopupMenuNodeDefault(POPUP_KEY);
//		popupMenu.registerPopupMenu(POPUP_KEY, POPUP_KEY);
		popupMenu.buildPopupMenu(POPUP_KEY);
	}

	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {			
			Component component = (Component) event.getSource();

			WorkspaceController.getCurrentWorkspaceController().getPopups()
					.showPopup(POPUP_KEY, component, event.getX(), event.getY());

		}
	}

	public String toString() {
		return this.getClass().getSimpleName() + "[id=" + this.getId() + ";name=" + this.getName() + ";path="
				+ this.getFolderPath() + "]";
	}
}
