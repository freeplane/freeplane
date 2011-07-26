package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspacePreferences;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

public class GroupNode extends WorkspaceNode implements IWorkspaceNodeEventListener {
	private final static String POPUP_KEY = "group_popup";
	private static boolean isInit = false;

	public GroupNode(String id) {
		super(id);
	}

	private void initializePopup() {
		if (!isInit) {
			WorkspaceController.getCurrentWorkspaceController().getPopups().registerPopupMenu(POPUP_KEY);
			AFreeplaneAction action = WorkspaceController.getCurrentWorkspaceController().getPopups().new CheckBoxAction(
					WorkspacePreferences.SHOW_WORKSPACE_TEXT, WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY);
			WorkspaceController.getCurrentWorkspaceController().getPopups()
					.addMenuEntry(POPUP_KEY, "/workspace_node_popup", action);
			isInit = true;
		}
	}

	@Override
	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			initializePopup();
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
