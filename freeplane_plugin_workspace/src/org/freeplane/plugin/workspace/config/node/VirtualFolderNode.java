package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.PopupMenus;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class VirtualFolderNode extends AFolderNode implements IWorkspaceNodeEventListener {
	private static final Icon DEFAULT_ICON = new ImageIcon(AWorkspaceTreeNode.class.getResource("/images/16x16/object-group-2.png"));
	
	public final static String POPUP_KEY = "/workspace_groupnode_popup";

	public VirtualFolderNode(String type) {
		super(type);
	}

	public void initializePopup() {
		PopupMenus popupMenu = WorkspaceController.getController().getPopups();
		if (!isSystem()) {
			popupMenu.registerPopupMenuNodeDefault(POPUP_KEY);
		}
		popupMenu.registerPopupMenu(POPUP_KEY, POPUP_KEY);
		popupMenu.buildPopupMenu(POPUP_KEY);
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}

	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {			
			Component component = (Component) event.getBaggage();

			WorkspaceController.getController().getPopups()
					.showPopup(POPUP_KEY, component, event.getX(), event.getY());

		}
		System.out.println("Event: " + event);

	}
	
	
	public AWorkspaceTreeNode clone() {
		VirtualFolderNode node = new VirtualFolderNode(getType());
		return clone(node);
	}
}
