package org.freeplane.plugin.script;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

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
        catch (ExecuteScriptException ex) {
			 LogUtils.warn(ex);
             ScriptingEngine.showScriptExceptionErrorMessage(ex);
        }
		finally {
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}
}
