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

import org.freeplane.controller.resources.ResourceController;
import org.freeplane.main.HtmlTools;
import org.freeplane.main.Tools;
import org.freeplane.main.Tools.BooleanHolder;
import org.freeplane.map.pattern.mindmapnode.IExternalPatternAction;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.MModeController;

import plugins.script.ScriptEditorPanel.IScriptModel;
import plugins.script.ScriptEditorPanel.ScriptHolder;
import plugins.script.ScriptingEngine.IErrorHandler;
import deprecated.freemind.common.BooleanProperty;
import deprecated.freemind.common.ScriptEditorProperty;
import deprecated.freemind.common.SeparatorProperty;
import deprecated.freemind.common.StringProperty;
import deprecated.freemind.common.XmlBindingTools;
import deprecated.freemind.extensions.IHookRegistration;
import deprecated.freemind.preferences.IFreemindPropertyContributor;
import deprecated.freemind.preferences.layout.OptionPanel;
import freemind.controller.actions.generated.instance.Pattern;
import freemind.controller.actions.generated.instance.ScriptEditorWindowConfigurationStorage;

public class ScriptingRegistration implements IHookRegistration,
        IExternalPatternAction {
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
			return (ScriptEditorWindowConfigurationStorage) XmlBindingTools
			    .getInstance().decorateDialog(pPanel,
			        pWindow_preference_storage_property);
		}

		public void endDialog(final boolean pIsCanceled) {
			if (pIsCanceled) {
				mScript = mOriginalScript;
			}
		}

		public boolean executeScript(final int pIndex,
		                             final PrintStream pOutStream,
		                             final IErrorHandler pErrorHandler) {
			return ScriptingEngine.executeScript(controller.getSelectedNode(),
			    new BooleanHolder(true), mScript, controller, pErrorHandler,
			    pOutStream, getScriptCookies());
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

		public void storeDialogPositions(
		                                 final ScriptEditorPanel pPanel,
		                                 final ScriptEditorWindowConfigurationStorage pStorage,
		                                 final String pWindow_preference_storage_property) {
			controller.storeDialogPositions(pPanel, pStorage,
			    pWindow_preference_storage_property);
		}
	}

	private static final class ScriptingPluginPropertyContributor implements
	        IFreemindPropertyContributor {
		public ScriptingPluginPropertyContributor(
		                                          final MModeController modeController) {
		}

		public List getControls() {
			final Vector controls = new Vector();
			controls.add(new OptionPanel.NewTabProperty(
			    "plugins/scripting/tab_name"));
			controls.add(new SeparatorProperty(
			    "plugins/scripting/separatorPropertyName"));
			controls
			    .add(new BooleanProperty(
			        ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION
			                + ".tooltip",
			        ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION));
			controls
			    .add(new BooleanProperty(
			        ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION
			                + ".tooltip",
			        ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION));
			controls
			    .add(new BooleanProperty(
			        ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION
			                + ".tooltip",
			        ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION));
			controls.add(new BooleanProperty(
			    ResourceController.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED
			            + ".tooltip",
			    ResourceController.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED));
			controls.add(new StringProperty(
			    ResourceController.RESOURCES_SCRIPT_USER_KEY_NAME_FOR_SIGNING
			            + ".tooltip",
			    ResourceController.RESOURCES_SCRIPT_USER_KEY_NAME_FOR_SIGNING));
			return controls;
		}
	}

	final private MModeController controller;
	final private HashMap mScriptCookies = new HashMap();
	private ScriptEditorProperty.IScriptEditorStarter mScriptEditorStarter;
	private ScriptingPluginPropertyContributor mScriptingPluginPropertyContributor;

	public ScriptingRegistration(final ModeController controller) {
		this.controller = (MModeController) controller;
	}

	public void act(final NodeModel node, final Pattern pattern) {
		if (pattern.getPatternScript() != null
		        && pattern.getPatternScript().getValue() != null) {
			ScriptingEngine.executeScript(node, new BooleanHolder(false),
			    HtmlTools.unescapeHTMLUnicodeEntity(pattern.getPatternScript()
			        .getValue()), controller, new IErrorHandler() {
				    public void gotoLine(final int pLineNumber) {
				    }
			    }, System.out, getScriptCookies());
		}
	}

	public void deRegister() {
		controller.deregisterPlugin(this);
		controller.deregisterPlugin(mScriptEditorStarter);
		OptionPanel.removeContributor(mScriptingPluginPropertyContributor);
	}

	public HashMap getScriptCookies() {
		return mScriptCookies;
	}

	public void register() {
		controller.registerPlugin(this);
		mScriptEditorStarter = new ScriptEditorProperty.IScriptEditorStarter() {
			public String startEditor(final String pScriptInput) {
				final PatternScriptModel patternScriptModel = new PatternScriptModel(
				    pScriptInput);
				final ScriptEditorPanel scriptEditorPanel = new ScriptEditorPanel(
				    patternScriptModel, false);
				scriptEditorPanel.setVisible(true);
				return patternScriptModel.getScript();
			}
		};
		controller.registerPlugin(mScriptEditorStarter);
		mScriptingPluginPropertyContributor = new ScriptingPluginPropertyContributor(
		    controller);
		OptionPanel.addContributor(mScriptingPluginPropertyContributor);
	}
}
