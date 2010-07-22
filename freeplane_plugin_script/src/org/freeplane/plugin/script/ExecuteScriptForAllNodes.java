package org.freeplane.plugin.script;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.common.map.NodeModel;

@ActionLocationDescriptor(locations = { "/menu_bar/extras/first/scripting" })
public class ExecuteScriptForAllNodes extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public ExecuteScriptForAllNodes() {
		super("ExecuteScriptForAllNodes");
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = Controller.getCurrentController().getMap().getRootNode();
		Controller.getCurrentController().getViewController().setWaitingCursor(true);
		try {
			ScriptingEngine.performScriptOperationRecursive(node);
		}
		finally {
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}
}
