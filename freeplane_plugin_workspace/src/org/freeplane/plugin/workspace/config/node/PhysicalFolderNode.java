package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;
import java.io.File;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.actions.FileNodeAddNewMindmapAction;
import org.freeplane.plugin.workspace.config.actions.FileNodeCopyAction;
import org.freeplane.plugin.workspace.config.actions.FileNodeCutAction;
import org.freeplane.plugin.workspace.config.actions.FileNodeDeleteAction;
import org.freeplane.plugin.workspace.config.actions.FileNodePasteAction;
import org.freeplane.plugin.workspace.config.actions.FileNodeRenameAction;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenuBuilder;

public class PhysicalFolderNode extends AFolderNode implements IWorkspaceNodeEventListener {
	
	private static final long serialVersionUID = 1L;
	private static Icon FOLDER_OPEN_ICON = new ImageIcon(PhysicalFolderNode.class.getResource("/images/16x16/folder-orange_open.png"));
	private static final Icon FOLDER_CLOSED_ICON = new ImageIcon(PhysicalFolderNode.class.getResource("/images/16x16/folder-orange.png"));
	
	private static WorkspacePopupMenu popupMenu = null;
	
	private URI folderPath;
	
	

	public PhysicalFolderNode(String id) {
		super(id);
	}

	@ExportAsAttribute("path")
	public URI getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(URI folderPath) {
		this.folderPath = folderPath;
	}

	public static void refresh(AWorkspaceTreeNode node) {
		// if folder path is not correctly set
		if(node instanceof PhysicalFolderNode) {
			if (((PhysicalFolderNode) node).getFolderPath() == null) {
				return;
			}
	
			File folder;
			WorkspaceController controller = WorkspaceController.getController();
			try {
				folder = WorkspaceUtils.resolveURI(((PhysicalFolderNode)node).getFolderPath());
				if (folder.isDirectory()) {
					node.removeAllChildren();
					controller.getFilesystemReader().scanFileSystem(node, folder);
					controller.getWorkspaceModel().reload(node);
					
				}
	
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void initializePopup() {
		if (popupMenu == null) {
			ModeController modeController = Controller.getCurrentModeController();
			modeController.addAction(new FileNodeAddNewMindmapAction());
			modeController.addAction(new FileNodeCutAction());
			modeController.addAction(new FileNodeRenameAction());
			modeController.addAction(new FileNodeDeleteAction());
			modeController.addAction(new FileNodeCopyAction());
			modeController.addAction(new FileNodePasteAction());
			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					"FileNodeAddNewMindmapAction",
					WorkspacePopupMenuBuilder.SEPARATOR, 
					"FileNodePasteAction",
					"FileNodeCopyAction",
					"FileNodeCutAction",
					//"FileNodeDeleteAction",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"FileNodeRenameAction",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.refresh",
					"workspace.action.node.delete"
			});
		}
	}

	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			showPopup( (Component) event.getBaggage(), event.getX(), event.getY());
		}
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(FOLDER_OPEN_ICON);
		renderer.setClosedIcon(FOLDER_CLOSED_ICON);
		renderer.setLeafIcon(FOLDER_CLOSED_ICON);
		return true;
	}

	public String toString() {
		return this.getClass().getSimpleName() + "[id=" + this.getId() + ";name=" + this.getName() + ";path="
				+ this.getFolderPath() + "]";
	}

	public void refresh() {	
		if(getKey() == null) {
			return;
		}
		refresh(this);
		
	}
	
	protected AWorkspaceTreeNode clone(PhysicalFolderNode node) {		
		node.setFolderPath(getFolderPath());		
		return super.clone(node);
	}
	
	protected WorkspacePopupMenu getPopupMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}
	
	public AWorkspaceTreeNode clone() {
		PhysicalFolderNode node = new PhysicalFolderNode(getType());
		return clone(node);
	}
}
