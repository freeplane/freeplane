package org.freeplane.plugin.script;

import static org.freeplane.plugin.script.ScriptingMenuUtils.noScriptsAvailableMessage;
import static org.freeplane.plugin.script.ScriptingMenuUtils.scriptNameToMenuItemTitle;

import java.util.Map;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.util.ActionUtils;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.ScriptingConfiguration.ScriptMetaData;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

public class ScriptingMenuEntryVisitor implements EntryVisitor {
	private ModeController modeController;
	private ScriptingConfiguration configuration;
	private ExecutionModeSelector modeSelector;

	public ScriptingMenuEntryVisitor(ModeController modeController, ScriptingConfiguration configuration,
	                                 ExecutionModeSelector modeSelector) {
		this.modeController = modeController;
		this.configuration = configuration;
		this.modeSelector = modeSelector;
	}

	@Override
	public void visit(Entry target) {
		if (configuration.getMenuTitleToPathMap().isEmpty()) {
//			target.addChild(createNoScriptsAvailableAction());
		}
		else {
			// add entry for all scripts but disable scripts that don't support selected exec mode  
			final ExecutionMode executionMode = modeSelector.getExecutionMode();
			for (final Map.Entry<String, String> entry : configuration.getMenuTitleToPathMap().entrySet()) {
				// for every action
				// FIXME: construct ExecuteScriptAction here (cached?)
				AFreeplaneAction action = null;
				new EntryAccessor().addChildAction(target, action);
				target.addChild(createScriptEntry(entry.getKey(), entry.getValue(), executionMode));
			}
		}
	}

	private Entry createScriptEntry(final String scriptName, final String scriptPath, ExecutionMode executionMode) {
		final ScriptMetaData metaData = configuration.getMenuTitleToMetaDataMap().get(scriptName);
		final String title = scriptNameToMenuItemTitle(scriptName);
		AFreeplaneAction action = new ExecuteScriptAction(scriptName, title, scriptPath, executionMode,
		    metaData.cacheContent(), metaData.getPermissions());
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		final JCommandMenuButton scriptEntry = new JCommandMenuButton(title, icon);
//		scriptEntry.setActionRichTooltip(createRichTooltip(title, metaData));
		scriptEntry.addActionListener(action);
		scriptEntry.setFocusable(false);
		scriptEntry.setEnabled(metaData.getExecutionModes().contains(executionMode));
		return null;
	}
//
//	private Entry createNoScriptsAvailableAction() {
//		return new JCommandMenuButton(noScriptsAvailableMessage(), null);
//	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}
}
