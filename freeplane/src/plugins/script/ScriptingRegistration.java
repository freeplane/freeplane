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
import java.util.List;
import java.util.Vector;

import org.freeplane.controller.Controller;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.controller.resources.ui.BooleanProperty;
import org.freeplane.controller.resources.ui.IFreemindPropertyContributor;
import org.freeplane.controller.resources.ui.OptionPanel;
import org.freeplane.controller.resources.ui.ScriptEditorProperty;
import org.freeplane.controller.resources.ui.SeparatorProperty;
import org.freeplane.controller.resources.ui.StringProperty;
import org.freeplane.controller.resources.ui.TabProperty;
import org.freeplane.main.HtmlTools;
import org.freeplane.main.Tools;
import org.freeplane.main.Tools.BooleanHolder;
import org.freeplane.map.pattern.mindmapnode.IExternalPatternAction;
import org.freeplane.map.pattern.mindmapnode.Pattern;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.ui.MenuBuilder;

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
			return ScriptingEngine.executeScript(modeController.getSelectedNode(),
			    new BooleanHolder(true), mScript, modeController, pErrorHandler, pOutStream,
			    getScriptCookies());
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

	private static final class ScriptingPluginPropertyContributor implements
	        IFreemindPropertyContributor {
		public ScriptingPluginPropertyContributor(final MModeController modeController) {
		}

		public List getControls() {
			final Vector controls = new Vector();
			controls.add(new TabProperty("OptionPanel.plugins/scripting/tab_name"));
			controls.add(new SeparatorProperty(
			    "OptionPanel.separator.plugins/scripting/separatorPropertyName"));
			controls.add(new BooleanProperty(
			    ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION));
			controls.add(new BooleanProperty(
			    ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION));
			controls.add(new BooleanProperty(
			    ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION));
			controls
			    .add(new BooleanProperty(ResourceController.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED));
			controls.add(new StringProperty(
			    ResourceController.RESOURCES_SCRIPT_USER_KEY_NAME_FOR_SIGNING));
			return controls;
		}
	}

	final private MModeController modeController;
	final private HashMap mScriptCookies = new HashMap();
	private ScriptEditorProperty.IScriptEditorStarter mScriptEditorStarter;
	private ScriptingPluginPropertyContributor mScriptingPluginPropertyContributor;

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
		mScriptingPluginPropertyContributor = new ScriptingPluginPropertyContributor(modeController);
		OptionPanel.addContributor(mScriptingPluginPropertyContributor);
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory()
		    .getMenuBuilder();
		menuBuilder.addAnnotatedAction(new ScriptEditor(this));
		menuBuilder.addAnnotatedAction(new ScriptingEngine(this));
	}
}
