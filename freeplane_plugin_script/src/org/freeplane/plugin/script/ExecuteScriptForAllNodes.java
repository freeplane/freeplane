package org.freeplane.plugin.script;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;

@ActionLocationDescriptor(locations = { "/menu_bar/extras/first/scripting" })
public class ExecuteScriptForAllNodes extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public ExecuteScriptForAllNodes(final Controller controller) {
		super("ExecuteScriptForAllNodes");
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = getController().getMap().getRootNode();
		getController().getViewController().setWaitingCursor(true);
		try {
			ScriptingEngine.performScriptOperationRecursive((MModeController) getModeController(), node);
		}
		finally {
			getController().getViewController().setWaitingCursor(false);
		}
	}
}
