package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;

import javax.swing.JPopupMenu;

import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.PopupMenus;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

public class GroupNode extends WorkspaceNode implements IWorkspaceNodeEventListener {

	public GroupNode(String id) {
		super(id);
	}

	@Override
	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			Component component = (Component) event.getSource();			
			
			final ControllerPopupMenuListener popupListener = new ControllerPopupMenuListener();
			final JPopupMenu popupmenu = WorkspaceController.getCurrentWorkspaceController().getPopups().getWorkspacePopupMenu();
			if (popupmenu != null) {
				System.out.println("FISH popupmenu is not null: ");
				
                popupmenu.addHierarchyListener(popupListener);
                popupmenu.show(component, event.getX(), event.getY());
                //event.consume();
            }
			else {
				System.out.println("FISH popupmenu is null");
			}

		}
		System.out.println("Event: " + event);

	}
	

	public String getTagName() {
		return "group";
	}

}
