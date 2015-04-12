package org.freeplane.plugin.script;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;

@SuppressWarnings("serial")
public class ScriptsRunToggleAction extends AFreeplaneAction {

	private ExecutionMode mode;
	private ExecutionModeSelector modeSelector;

	public ScriptsRunToggleAction(ExecutionModeSelector modeSelector, ExecutionMode mode) {
		super(createKey(mode));
		this.modeSelector = modeSelector;
		this.mode = mode;
	}

	private static String createKey(ExecutionMode mode) {
		return "ScriptsRunToggleAction." + mode;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		modeSelector.select(mode);
	}

}
