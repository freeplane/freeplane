package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.PopupMenus;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

public class VirtualFolderNode extends FolderNode implements IWorkspaceNodeEventListener {
	private static final Icon FOLDER_OPEN_ICON = new ImageIcon(AWorkspaceNode.class.getResource("/images/16x16/folder-blue_open.png"));
	private static final Icon FOLDER_CLOSED_ICON = new ImageIcon(AWorkspaceNode.class.getResource("/images/16x16/folder-blue.png"));
	
	public final static String POPUP_KEY = "/workspace_groupnode_popup";

	public VirtualFolderNode(String type) {
		super(type);
		initializePopup();
	}

	public void initializePopup() {
		PopupMenus popupMenu = WorkspaceController.getController().getPopups();
		popupMenu.registerPopupMenuNodeDefault(POPUP_KEY);
		popupMenu.registerPopupMenu(POPUP_KEY, POPUP_KEY);
		popupMenu.buildPopupMenu(POPUP_KEY);
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(FOLDER_OPEN_ICON);
		renderer.setClosedIcon(FOLDER_CLOSED_ICON);
		renderer.setLeafIcon(FOLDER_CLOSED_ICON);
		return true;
	}

	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {			
			Component component = (Component) event.getSource();

			WorkspaceController.getController().getPopups()
					.showPopup(POPUP_KEY, component, event.getX(), event.getY());

		}
		System.out.println("Event: " + event);

	}
}
