package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenuBuilder;

public class VirtualFolderNode extends AFolderNode implements IWorkspaceNodeEventListener {
	
	private static final long serialVersionUID = 1L;
	private static final Icon DEFAULT_ICON = new ImageIcon(AWorkspaceTreeNode.class.getResource("/images/16x16/object-group-2.png"));
	
	private static WorkspacePopupMenu popupMenu = null;
	
	public VirtualFolderNode() {
		super(AFolderNode.FOLDER_TYPE_VIRTUAL);
	}

	public VirtualFolderNode(String type) {
		super(type);
	}

	public void initializePopup() {
		if (popupMenu == null) {			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
					"workspace.action.node.new.folder",
					"workspace.action.node.new.link",
					"workspace.action.node.new.directory",
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
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}

	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {	
			showPopup( (Component) event.getBaggage(), event.getX(), event.getY());
		}
		System.out.println("Event: " + event);

	}
	
	
	public AWorkspaceTreeNode clone() {
		VirtualFolderNode node = new VirtualFolderNode(getType());
		return clone(node);
	}
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		
		return popupMenu;
	}

	public URI getPath() {
		return null;
	}
}
