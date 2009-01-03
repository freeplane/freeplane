/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Christian Foltin
 *  It is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.script;

import java.io.PrintStream;
import java.util.HashMap;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.ModeController;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.OptionPanelBuilder;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.Tools;
import org.freeplane.core.util.Tools.BooleanHolder;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapnode.pattern.IExternalPatternAction;
import org.freeplane.features.mindmapnode.pattern.Pattern;
import org.freeplane.features.mindmapnode.pattern.ScriptEditorProperty;

import plugins.script.ScriptEditorPanel.IScriptModel;
import plugins.script.ScriptEditorPanel.ScriptHolder;
import plugins.script.ScriptingEngine.IErrorHandler;

public class ScriptingRegistration implements IExternalPatternAction {
	final private class PatternScriptModel implements IScriptModel {
		final private String mOriginalScript;
		private String mScript;

		public PatternScriptModel(final String pScript) {
			mScript = pScript;
			mOriginalScript = pScript;
		}

		public int addNewScript() {
			return 0;
		}

		public ScriptEditorWindowConfigurationStorage decorateDialog(
		                                                             final ScriptEditorPanel pPanel,
		                                                             final String pWindow_preference_storage_property) {
			final String marshalled = Controller.getResourceController().getProperty(
			    pWindow_preference_storage_property);
			return ScriptEditorWindowConfigurationStorage.decorateDialog(marshalled, pPanel);
		}

		public void endDialog(final boolean pIsCanceled) {
			if (pIsCanceled) {
				mScript = mOriginalScript;
			}
		}

		public boolean executeScript(final int pIndex, final PrintStream pOutStream,
		                             final IErrorHandler pErrorHandler) {
			return ScriptingEngine.executeScript(modeController.getMapController()
			    .getSelectedNode(), new BooleanHolder(true), mScript, modeController,
			    pErrorHandler, pOutStream, getScriptCookies());
		}

		public int getAmountOfScripts() {
			return 1;
		}

		public String getScript() {
			return mScript;
		}

		public ScriptHolder getScript(final int pIndex) {
			return new ScriptHolder("Script", mScript);
		}

		public boolean isDirty() {
			return !Tools.safeEquals(mScript, mOriginalScript);
		}

		public void setScript(final int pIndex, final ScriptHolder pScript) {
			mScript = pScript.getScript();
		}

		public void storeDialogPositions(final ScriptEditorPanel pPanel,
		                                 final ScriptEditorWindowConfigurationStorage pStorage,
		                                 final String pWindow_preference_storage_property) {
			pStorage.storeDialogPositions(pPanel, pWindow_preference_storage_property);
		}
	}

	private static final String SEPARATOR = "OptionPanel.separator.plugins/scripting/separatorPropertyName";
	private static final String TAB = "OptionPanel.plugins/scripting/tab_name";
	final private MModeController modeController;
	final private HashMap mScriptCookies = new HashMap();
	private ScriptEditorProperty.IScriptEditorStarter mScriptEditorStarter;

	public ScriptingRegistration(final ModeController controller) {
		modeController = (MModeController) controller;
		register();
	}

	public void act(final NodeModel node, final Pattern pattern) {
		if (pattern.getPatternScript() != null && pattern.getPatternScript().getValue() != null) {
			ScriptingEngine.executeScript(node, new BooleanHolder(false), HtmlTools
			    .unescapeHTMLUnicodeEntity(pattern.getPatternScript().getValue()), modeController,
			    new IErrorHandler() {
				    public void gotoLine(final int pLineNumber) {
				    }
			    }, System.out, getScriptCookies());
		}
	}

	private void addPropertiesToOptionPanel() {
		final OptionPanelBuilder controls = modeController.getOptionPanelBuilder();
		controls.addTab(TAB);
		controls.addSeparator(TAB, SEPARATOR, IndexedTree.AS_CHILD);
		final String GROUP = TAB + "/" + SEPARATOR;
		controls.addBooleanProperty(GROUP,
		    ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION,
		    IndexedTree.AS_CHILD);
		controls.addBooleanProperty(GROUP,
		    ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION,
		    IndexedTree.AS_CHILD);
		controls.addBooleanProperty(GROUP,
		    ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION,
		    IndexedTree.AS_CHILD);
		controls.addBooleanProperty(GROUP, ResourceController.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED,
		    IndexedTree.AS_CHILD);
		controls.addStringProperty(GROUP,
		    ResourceController.RESOURCES_SCRIPT_USER_KEY_NAME_FOR_SIGNING, IndexedTree.AS_CHILD);
	}

	public HashMap getScriptCookies() {
		return mScriptCookies;
	}

	private void register() {
		modeController.addExtension(IExternalPatternAction.class, this);
		mScriptEditorStarter = new ScriptEditorProperty.IScriptEditorStarter() {
			public String startEditor(final String pScriptInput) {
				final PatternScriptModel patternScriptModel = new PatternScriptModel(pScriptInput);
				final ScriptEditorPanel scriptEditorPanel = new ScriptEditorPanel(
				    patternScriptModel, false);
				scriptEditorPanel.setVisible(true);
				return patternScriptModel.getScript();
			}
		};
		modeController.addExtension(ScriptEditorProperty.IScriptEditorStarter.class,
		    mScriptEditorStarter);
		addPropertiesToOptionPanel();
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory()
		    .getMenuBuilder();
		menuBuilder.addAnnotatedAction(new ScriptEditor(this));
		menuBuilder.addAnnotatedAction(new ScriptingEngine(this));
	}
}
