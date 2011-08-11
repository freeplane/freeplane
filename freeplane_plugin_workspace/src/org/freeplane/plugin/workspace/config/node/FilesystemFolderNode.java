package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

public class FilesystemFolderNode extends AWorkspaceNode implements TreeExpansionListener, IWorkspaceNodeEventListener {

	private URI folderPath;
	private boolean isUpToDate = false;

	private static String POPUP_KEY = "filesystem_folder";

	public FilesystemFolderNode(String id) {
		super(id);
	}

	@ExportAsAttribute("path")
	public URI getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(URI folderPath) {
		this.folderPath = folderPath;
	}

	public void treeCollapsed(TreeExpansionEvent event) {
	}

	public void treeExpanded(TreeExpansionEvent event) {
		if (isUpToDate || getFolderPath() == null)
			return;
		refreshFolder((DefaultMutableTreeNode) event.getPath().getLastPathComponent());
	}

	public void refreshFolder(final DefaultMutableTreeNode node) {
		// if folder path is not correctly set
		if (getFolderPath() == null)
			return;

		File folder;
		try {
			URL absoluteUrl = getFolderPath().toURL().openConnection().getURL();
			
			folder = new File(absoluteUrl.getFile());
			if (folder.isDirectory()) {
				node.removeAllChildren();
				WorkspaceController.getCurrentWorkspaceController().getFilesystemReader()
						.scanFilesystem(node.getUserObject(), folder);
				WorkspaceController.getCurrentWorkspaceController().getViewModel().reload(node);
				isUpToDate = true;
			}
			
			
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
	}

	public String getTagName() {
		return "filesystem_folder";
	}

	private void initializePopup() {
		WorkspaceController.getCurrentWorkspaceController().getPopups().registerPopupMenu(POPUP_KEY);
	}

	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			initializePopup();
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
