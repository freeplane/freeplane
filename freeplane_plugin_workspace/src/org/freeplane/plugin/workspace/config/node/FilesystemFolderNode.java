package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;
import java.io.File;
import java.net.URL;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

public class FilesystemFolderNode extends AWorkspaceNode implements TreeExpansionListener, IWorkspaceNodeEventListener {

	private URL folderPath;
	private boolean isUpToDate = false;
	
	private static String POPUP_KEY = "filesystem_folder";
	
	public FilesystemFolderNode(String id) {
		super(id);
	}
		
	public URL getFolderPath() {
		return folderPath;
	}
	
	@ExportAsAttribute("path")
	public String getFolderPathString() {
		if (folderPath == null || folderPath.getPath() == null) {
			return "";
		}
		return folderPath.getPath();
	}

	public void setFolderPath(URL folderPath) {
		this.folderPath = folderPath;
	}	
	
	public void treeCollapsed(TreeExpansionEvent event) {
	}
	
	public void treeExpanded(TreeExpansionEvent event) {
		if(isUpToDate||getFolderPath()==null) return;
		refreshFolder((DefaultMutableTreeNode)event.getPath().getLastPathComponent());
	}

	public void refreshFolder(final DefaultMutableTreeNode node) {
		// if folder path is not correctly set
		if (getFolderPath() == null)
			return;
		
		File folder = new File(getFolderPath().getFile());
		if(folder.isDirectory()) {			 
			node.removeAllChildren();
			WorkspaceController.getCurrentWorkspaceController().getFilesystemReader().scanFilesystem(node.getUserObject(), folder);
			WorkspaceController.getCurrentWorkspaceController().getViewModel().reload(node);
			isUpToDate = true;
		}
	}
	
	public String getTagName() {
		return "filesystem_folder";
	}
	
	private void initializePopup() {
		//if (!isInit) {
			WorkspaceController.getCurrentWorkspaceController().getPopups().registerPopupMenu(POPUP_KEY);
//			AFreeplaneAction action = WorkspaceController.getCurrentWorkspaceController().getPopups().new CheckBoxAction("BLUBB",
//					"BLUBB");
//			WorkspaceController.getCurrentWorkspaceController().getPopups()
//					.addCechkbox(POPUP_KEY, "/workspace_node_popup", action, true);
			
//			isInit = true;
	//	}
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
		return this.getClass().getSimpleName()+"[id="+this.getId()+";name="+this.getName()+";path="+this.getFolderPath()+"]";
	}
}
