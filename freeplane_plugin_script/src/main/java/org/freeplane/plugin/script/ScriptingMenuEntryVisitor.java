package org.freeplane.plugin.script;

import static org.freeplane.plugin.script.ScriptingMenuUtils.noScriptsAvailableMessage;
import static org.freeplane.plugin.script.ScriptingMenuUtils.scriptNameToMenuItemTitle;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Map;

import javax.swing.Action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.BuildPhaseListener;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryNavigator;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.ScriptingGuiConfiguration.ScriptMetaData;

public class ScriptingMenuEntryVisitor implements EntryVisitor, BuildPhaseListener {
	private ScriptingGuiConfiguration configuration;
	private final HashSet<String> registeredLocations = new HashSet<String>();
	private ModeController modeController;

	public ScriptingMenuEntryVisitor(ScriptingGuiConfiguration configuration, ModeController modeController) {
		this.configuration = configuration;
		this.modeController = modeController;
	}

	/** builds menu entries for scripts without a special menu location. */
	@Override
	public void visit(Entry target) {
		for (final Map.Entry<String, String> entry : configuration.getMenuTitleToPathMap().entrySet()) {
			String scriptName = entry.getKey();
			final ScriptMetaData metaData = configuration.getMenuTitleToMetaDataMap().get(scriptName);
			if (!metaData.hasMenuLocation()) {
				for (final ExecutionMode executionMode : metaData.getExecutionModes()) {
					target.addChild(createEntry(scriptName, entry.getValue(), executionMode));
				}
			}
			// else: see buildPhaseFinished
		}
		if (target.isLeaf()) {
			target.addChild(createNoScriptsAvailableAction());
		}
	}

	@Override
	public void buildPhaseFinished(Phase actions, Entry target) {
		if (target.getParent() == null && actions == Phase.ACTIONS) {
			buildEntriesWithSpecialMenuLocation(target);
		}
	}

	private void buildEntriesWithSpecialMenuLocation(Entry target) {
		for (final Map.Entry<String, String> entry : configuration.getMenuTitleToPathMap().entrySet()) {
			final ScriptMetaData metaData = configuration.getMenuTitleToMetaDataMap().get(entry.getKey());
			if (metaData.hasMenuLocation()) {
				addEntryForGivenLocation(target.getRoot(), metaData, entry.getValue());
			}
			// else: see visit
		}
	}

	private void addEntryForGivenLocation(Entry rootEntry, final ScriptMetaData metaData, String scriptPath) {
		for (final ExecutionMode executionMode : metaData.getExecutionModes()) {
			final String location = metaData.getMenuLocation(executionMode);
			if (registeredLocations.add(location + "/" + metaData.getScriptName())) {
				Entry parentEntry = findOrCreateEntry(rootEntry, location);
				if (parentEntry == null)
					throw new RuntimeException("internal error: cannot add entry for " + location);
				Entry entry = createEntry(metaData.getScriptName(), scriptPath, executionMode);
				parentEntry.addChild(entry);;
			}
		}
	}

	private Entry findOrCreateEntry(Entry rootEntry, final String path) {
		EntryNavigator entryNavigator = EntryNavigator.instance();
		Entry entry = entryNavigator.findChildByPath(rootEntry, path);
		if (entry == null) {
			// System.err.println("creating submenu " + path);
			Entry parent = findOrCreateEntry(rootEntry, ScriptingMenuUtils.parentLocation(path));
			Entry menuEntry = new Entry();
			menuEntry.setName(lastPathElement(path));
			menuEntry.setAttribute("text", scriptNameToMenuItemTitle(lastPathElement(path)));
			parent.addChild(menuEntry);
			return menuEntry;
		}
		return entry;
	}

    private String lastPathElement(String path) {
    	int indexOfSlash = path.lastIndexOf('/');
    	// even works if not found (-1 + 1 = 0)
	    return path.substring(indexOfSlash + 1);
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

	private Entry createEntry(final String scriptName, final String scriptPath, ExecutionMode executionMode) {
		final ScriptMetaData metaData = configuration.getMenuTitleToMetaDataMap().get(scriptName);
		final String title = scriptNameToMenuItemTitle(scriptName);
		return createEntry(createAction(scriptName, scriptPath, executionMode, metaData, title));
	}

	private Entry createEntry(AFreeplaneAction action) {
	    final EntryAccessor entryAccessor = new EntryAccessor();
		final Entry scriptEntry = new Entry();
		scriptEntry.setName(action.getKey());
		// System.err.println("registering " + scriptEntry.getName());
		entryAccessor.setAction(scriptEntry, action);
		return scriptEntry;
    }

	private AFreeplaneAction createAction(final String scriptName, final String scriptPath,
                                          ExecutionMode executionMode, final ScriptMetaData metaData, final String title) {
		final String key = ExecuteScriptAction.makeMenuItemKey(scriptName, executionMode);
		final AFreeplaneAction alreadyRegisteredAction = modeController.getAction(key);
		if (alreadyRegisteredAction == null) {
			String longTitle = createTooltip(title, executionMode);
			String menuItemTitle = hasMultipleExcecutionModes(metaData) ? longTitle : title;
			AFreeplaneAction action = new ExecuteScriptAction(scriptName, menuItemTitle, scriptPath, executionMode,
				metaData.getPermissions());
			action.putValue(Action.SHORT_DESCRIPTION, longTitle);
			action.putValue(Action.LONG_DESCRIPTION, longTitle);
			modeController.addAction(action);
			return action;
		}
		else {
			return alreadyRegisteredAction;
		}
    }

	private boolean hasMultipleExcecutionModes(ScriptMetaData metaData) {
		return metaData.getExecutionModes().size() > 1;
	}

	private String createTooltip(String title, ExecutionMode mode) {
		return TextUtils.format(executionMode2TranslationProperty(mode), title);
	}

	private String executionMode2TranslationProperty(ExecutionMode mode) {
		switch (mode) {
			case ON_SINGLE_NODE:
				return "ExecuteScriptOnSingleNode.text";
			case ON_SELECTED_NODE_RECURSIVELY:
				return "ExecuteScriptOnSelectedNodeRecursively.text";
			default:
				return "ExecuteScriptOnSelectedNode.text";
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}
}
