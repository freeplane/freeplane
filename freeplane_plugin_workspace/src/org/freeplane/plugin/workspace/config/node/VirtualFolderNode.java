package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.PopupMenus;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

public class VirtualFolderNode extends AWorkspaceNode implements IWorkspaceNodeEventListener {
	private final static String POPUP_KEY = "/workspace_groupnode_popup";

	public VirtualFolderNode(String type) {
		super(type);
		initializePopup();
	}

	public void initializePopup() {
		PopupMenus popupMenu = WorkspaceController.getCurrentWorkspaceController().getPopups();
		popupMenu.registerPopupMenuNodeDefault(POPUP_KEY);
		popupMenu.registerPopupMenu(POPUP_KEY, POPUP_KEY);
		popupMenu.buildPopupMenu(POPUP_KEY);
	}

	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {			
			Component component = (Component) event.getSource();

			WorkspaceController.getCurrentWorkspaceController().getPopups()
					.showPopup(POPUP_KEY, component, event.getX(), event.getY());

		}
		System.out.println("Event: " + event);

	}

	public String getTagName() {
		return "group";
	}

}
