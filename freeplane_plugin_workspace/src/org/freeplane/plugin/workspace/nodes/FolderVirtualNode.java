package org.freeplane.plugin.workspace.nodes;

import java.awt.Component;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class FolderVirtualNode extends AFolderNode implements IWorkspaceNodeActionListener
																, IWorkspaceTransferableCreator {
	
	private static final long serialVersionUID = 1L;
	private static final Icon DEFAULT_ICON = new ImageIcon(AWorkspaceTreeNode.class.getResource("/images/16x16/object-group-2.png"));
	
	private static WorkspacePopupMenu popupMenu = null;
	
	public FolderVirtualNode() {
		super(AFolderNode.FOLDER_TYPE_VIRTUAL);
	}

	public FolderVirtualNode(String type) {
		super(type);
	}

	public void initializePopup() {
		if (popupMenu == null) {			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
					"workspace.action.node.new.folder",
					"workspace.action.node.new.link",
					WorkspacePopupMenuBuilder.endSubMenu(),
					WorkspacePopupMenuBuilder.SEPARATOR, 
					"workspace.action.node.cut",
					"workspace.action.node.copy",
					"workspace.action.node.paste",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.rename",
					"workspace.action.node.remove",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.refresh"		
			});
		}
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}

	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {	
			showPopup( (Component) event.getBaggage(), event.getX(), event.getY());
		}
	}
	
	
	public AWorkspaceTreeNode clone() {
		FolderVirtualNode node = new FolderVirtualNode(getType());
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
	
	public WorkspaceTransferable getTransferable() {
		WorkspaceTransferable transferable = new WorkspaceTransferable();
		try {
			List<AWorkspaceTreeNode> objectList = new ArrayList<AWorkspaceTreeNode>();
			objectList.add(this);
			transferable.addData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR, objectList);
			return transferable;
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}		
		return null;
	}
	
	public void refresh() {
		getModel().reload(this);
	}
}
