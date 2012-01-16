package org.freeplane.plugin.workspace.model.action;

import java.awt.event.ActionEvent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspacePreferences;

@SelectableAction(checkOnPropertyChange=WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY)
@EnabledAction(checkOnPopup = true)
public class WorkspaceHideAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceHideAction() {
		super("workspace.action.hide");
		setSelected(ResourceController.getResourceController().getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY));
	}

	public void actionPerformed(final ActionEvent e) {
		WorkspaceController.getController().showWorkspace(!this.isSelected());
	}
	
	public void setEnabled() {
		setEnabled(false);
	}

	public void setSelected() {
		setSelected(ResourceController.getResourceController().getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY));
	}
}
