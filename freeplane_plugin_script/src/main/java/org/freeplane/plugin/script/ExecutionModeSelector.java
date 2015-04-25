package org.freeplane.plugin.script;

import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;

public class ExecutionModeSelector {

	private ExecutionMode mode;

	public void select(ExecutionMode mode) {
		this.mode = mode;
	}

}
