package org.freeplane.plugin.script;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class ExecuteScriptForSelectionAction extends AMultipleNodeAction {
	private static final long serialVersionUID = 1L;
	private boolean success;

	public ExecuteScriptForSelectionAction() {
		super("ExecuteScriptForSelectionAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		success = true;
		Controller.getCurrentController().getViewController().setWaitingCursor(true);
		try {
			super.actionPerformed(e);
		}
		finally {
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		if (!success) {
			return;
		}
		try {
	        ScriptingEngine.performScriptOperation(node);
        }
        catch (ExecuteScriptException ex) {
			LogUtils.warn(ex);
			ScriptingEngine.showScriptExceptionErrorMessage(ex);
			success = false;
        }
	}
}
