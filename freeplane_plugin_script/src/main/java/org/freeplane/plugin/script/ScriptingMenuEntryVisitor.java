package org.freeplane.plugin.script;

import static org.freeplane.plugin.script.ScriptingMenuUtils.noScriptsAvailableMessage;
import static org.freeplane.plugin.script.ScriptingMenuUtils.scriptNameToMenuItemTitle;

import java.awt.event.ActionEvent;
import java.util.Map;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.util.ActionUtils;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.ScriptingConfiguration.ScriptMetaData;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

public class ScriptingMenuEntryVisitor implements EntryVisitor {
	private ScriptingConfiguration configuration;
	private ExecutionModeSelector modeSelector;

	public ScriptingMenuEntryVisitor(ScriptingConfiguration configuration, ExecutionModeSelector modeSelector) {
		this.configuration = configuration;
		this.modeSelector = modeSelector;
	}

	@Override
	public void visit(Entry target) {
		if (configuration.getMenuTitleToPathMap().isEmpty()) {
			target.addChild(createNoScriptsAvailableAction());
		}
		else {
			// add entry for all scripts but disable scripts that don't support selected exec mode  
			final ExecutionMode executionMode = modeSelector.getExecutionMode();
			for (final Map.Entry<String, String> entry : configuration.getMenuTitleToPathMap().entrySet()) {
				target.addChild(createScriptEntry(entry.getKey(), entry.getValue(), executionMode));
			}
		}
	}

	private Entry createNoScriptsAvailableAction() {
		final Entry entry = new Entry();
		entry.setName("NoScriptsAvailableAction");
		@SuppressWarnings("serial")
		final AFreeplaneAction noScriptsAvailableAction = new AFreeplaneAction("NoScriptsAvailableAction", noScriptsAvailableMessage(), null) {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		};
		new  EntryAccessor().setAction(entry, noScriptsAvailableAction);
		return entry;
	}

	private Entry createScriptEntry(final String scriptName, final String scriptPath, ExecutionMode executionMode) {
		final ScriptMetaData metaData = configuration.getMenuTitleToMetaDataMap().get(scriptName);
		final String title = scriptNameToMenuItemTitle(scriptName);
		AFreeplaneAction action = new ExecuteScriptAction(scriptName, title, scriptPath, executionMode,
		    metaData.cacheContent(), metaData.getPermissions());
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		final Entry scriptEntry = new Entry();
		action.setEnabled(metaData.getExecutionModes().contains(executionMode));
//		Object tooltip = createRichTooltip(title, metaData);
//		action.putValue(Action.SHORT_DESCRIPTION, tooltip);
//		action.putValue(Action.LONG_DESCRIPTION, tooltip);
		final EntryAccessor entryAccessor = new EntryAccessor();
		entryAccessor.addChildAction(scriptEntry, action);
		entryAccessor.setIcon(scriptEntry, icon);

		return scriptEntry;
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}
}
