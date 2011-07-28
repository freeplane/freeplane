package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.PopupMenus;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

public class GroupNode extends WorkspaceNode implements IWorkspaceNodeEventListener {
	private final static String POPUP_KEY = "group_popup";

	public GroupNode(String id) {
		super(id);
		initializePopup();
	}

	private void initializePopup() {
		PopupMenus popupMenu = WorkspaceController.getCurrentWorkspaceController().getPopups();
		if (popupMenu.registerPopupMenu(POPUP_KEY)) {
//			AFreeplaneAction action = popupMenu.new CheckBoxAction("BLUBB", "BLUBB");
//			popupMenu.addCechkbox(POPUP_KEY, "/workspace_node_popup", action, true);
		}
	}

	@Override
	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			// initializePopup();
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
