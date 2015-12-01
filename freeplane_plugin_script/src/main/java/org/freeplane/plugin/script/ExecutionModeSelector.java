package org.freeplane.plugin.script;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.swing.JComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;

public class ExecutionModeSelector {
	private ExecutionMode mode = ExecutionMode.ON_SINGLE_NODE;
	private String[] scriptingMenuRoots = new String[] { "userScripts", "node_popup_scripting" };
	private EntryAccessor entryAccessor = new EntryAccessor();

	public void select(ExecutionMode mode) {
		this.mode = mode;
		updateMenus();
		ResourceController.getResourceController().setProperty("ScriptsRunToggleAction.default",
		    "ScriptsRunToggleAction." + mode);
	}

	public ExecutionMode getExecutionMode() {
		return mode;
	}

	public void updateMenus() {
		Entry rootEntry = Controller.getCurrentModeController().getUserInputListenerFactory().getGenericMenuStructure();
		for (String entryName : scriptingMenuRoots) {
			updateMenu(rootEntry.findEntry(entryName));
		}
	}

	private void updateMenu(Entry scriptingEntry) {
		LinkedHashMap<File, JComponent> scriptFileToDisabledComponentMap = new LinkedHashMap<File, JComponent>();
		HashSet<File> enabledScriptFiles = new HashSet<File>();
		for (Entry actionEntry : scriptingEntry.children()) {
			AFreeplaneAction action = entryAccessor.getAction(actionEntry);
			if (!(action instanceof ExecuteScriptAction))
				continue;
			ExecuteScriptAction scriptAction = (ExecuteScriptAction) action;
			JComponent component = (JComponent) entryAccessor.getComponent(actionEntry);
			boolean enabled = scriptAction.getExecutionMode() == mode;
			if (enabled)
				enabledScriptFiles.add(scriptAction.getScriptFile());
			else
				scriptFileToDisabledComponentMap.put(scriptAction.getScriptFile(), component);
			component.setVisible(enabled);
			component.setEnabled(true);
		}
		// ensure that every script is visible (even if disabled)
		for (java.util.Map.Entry<File, JComponent> mapEntry : scriptFileToDisabledComponentMap.entrySet()) {
			if (!enabledScriptFiles.contains(mapEntry.getKey())) {
				mapEntry.getValue().setVisible(true);
				mapEntry.getValue().setEnabled(false);
			}
		}
	}
}
