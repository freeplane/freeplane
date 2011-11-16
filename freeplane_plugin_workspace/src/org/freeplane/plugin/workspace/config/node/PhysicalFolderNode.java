package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;
import java.io.File;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.actions.FileNodeNewDirectoryAction;
import org.freeplane.plugin.workspace.config.actions.FileNodeNewFileAction;
import org.freeplane.plugin.workspace.config.actions.FileNodeNewMindmapAction;
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
	
	public PhysicalFolderNode() {
		this(AFolderNode.FOLDER_TYPE_PHYSICAL);
	}

	public PhysicalFolderNode(String id) {
		super(id);
	}

	@ExportAsAttribute("path")
	public URI getPath() {
		return folderPath;
	}

	public void setPath(URI folderPath) {
		this.folderPath = folderPath;
	}

	public void initializePopup() {
		if (popupMenu == null) {
			ModeController modeController = Controller.getCurrentModeController();
			modeController.addAction(new FileNodeNewDirectoryAction());
			modeController.addAction(new FileNodeNewMindmapAction());
			modeController.addAction(new FileNodeNewFileAction());
			
			if (popupMenu == null) {			
				popupMenu = new WorkspacePopupMenu();
				WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
						WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
						"workspace.action.file.new.directory",
						"workspace.action.file.new.mindmap",
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.file.new.file",
						WorkspacePopupMenuBuilder.endSubMenu(),
						WorkspacePopupMenuBuilder.SEPARATOR, 
						"workspace.action.node.paste",
						"workspace.action.node.copy",
						"workspace.action.node.cut",
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.rename",
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.refresh",
						"workspace.action.node.delete"		
				});
			}
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
				+ this.getPath() + "]";
	}

	public void refresh() {
		File folder;
		try {
			folder = WorkspaceUtils.resolveURI(getPath());
			if (folder.isDirectory()) {
				WorkspaceUtils.getModel().removeAllElements(this);
				WorkspaceController.getController().getFilesystemReader().scanFileSystem(this, folder);
				WorkspaceUtils.getModel().reload(this);
				WorkspaceController.getController().getExpansionStateHandler().restoreExpansionState();				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	protected AWorkspaceTreeNode clone(PhysicalFolderNode node) {		
		node.setPath(getPath());		
		return super.clone(node);
	}
	
	public WorkspacePopupMenu getContextMenu() {
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
