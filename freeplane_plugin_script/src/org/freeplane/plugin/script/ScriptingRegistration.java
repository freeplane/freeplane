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
package org.freeplane.plugin.script;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.ComboBoxEditor;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IValidator;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.script.IScriptEditorStarter;
import org.freeplane.features.script.IScriptStarter;
import org.freeplane.main.addons.AddOnInstaller;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLParserFactory;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.ScriptEditorPanel.IScriptModel;
import org.freeplane.plugin.script.ScriptEditorPanel.ScriptHolder;
import org.freeplane.plugin.script.ScriptingConfiguration.ScriptMetaData;
import org.freeplane.plugin.script.ScriptingEngine.IErrorHandler;
import org.freeplane.plugin.script.addons.ManageAddOnsAction;
import org.freeplane.plugin.script.addons.ManageAddOnsDialog;
import org.freeplane.plugin.script.addons.ScriptAddOnProperties;
import org.freeplane.plugin.script.filter.ScriptConditionController;

class ScriptingRegistration {
	final private class ScriptModel implements IScriptModel {
		final private String mOriginalScript;
		private String mScript;

		public ScriptModel(final String pScript) {
			mScript = pScript;
			mOriginalScript = pScript;
		}

		public int addNewScript() {
			return 0;
		}

		public ScriptEditorWindowConfigurationStorage decorateDialog(final ScriptEditorPanel pPanel,
		                                                             final String pWindow_preference_storage_property) {
			final String marshalled = ResourceController.getResourceController().getProperty(
			    pWindow_preference_storage_property);
			return ScriptEditorWindowConfigurationStorage.decorateDialog(marshalled, pPanel);
		}

		public void endDialog(final boolean pIsCanceled) {
			if (pIsCanceled) {
				mScript = mOriginalScript;
			}
		}

		public Object executeScript(final int pIndex, final PrintStream pOutStream, final IErrorHandler pErrorHandler) {
			final ModeController modeController = Controller.getCurrentModeController();
			// the script is completely in the hand of the user -> no security issues.
			final ScriptingPermissions restrictedPermissions = ScriptingPermissions.getPermissiveScriptingPermissions();
			return ScriptingEngine.executeScript(modeController.getMapController().getSelectedNode(), mScript,
			    pErrorHandler, pOutStream, null, restrictedPermissions);
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
			return !StringUtils.equals(mScript, mOriginalScript);
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

	final private HashMap<String, Object> mScriptCookies = new HashMap<String, Object>();

	public ScriptingRegistration(ModeController modeController) {
		register(modeController);
	}

	private void addPropertiesToOptionPanel() {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null)
			throw new RuntimeException("cannot open preferences");
		Controller.getCurrentController().addOptionValidator(new IValidator() {
			public ValidationResult validate(Properties properties) {
				final ValidationResult result = new ValidationResult();
				final String readAccessString = properties
				    .getProperty(ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION);
				final String writeAccessString = properties
				.getProperty(ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_WRITE_RESTRICTION);
				final String classpath = properties.getProperty(ScriptingEngine.RESOURCES_SCRIPT_CLASSPATH);
				final boolean readAccess = readAccessString != null && Boolean.parseBoolean(readAccessString);
				final boolean writeAccess = writeAccessString != null && Boolean.parseBoolean(writeAccessString);
				final boolean classpathIsSet = classpath != null && classpath.length() > 0;
				if (classpathIsSet && !readAccess) {
					result.addError(TextUtils.getText("OptionPanel.validate_classpath_needs_readaccess"));
				}
				if (writeAccess && !readAccess) {
					result.addWarning(TextUtils.getText("OptionPanel.validate_write_without_read"));
				}
				return result;
			}
		});
		final MModeController modeController = (MModeController) Controller.getCurrentModeController();
		modeController.getOptionPanelBuilder().load(preferences);
	}

	public HashMap<String, Object> getScriptCookies() {
		return mScriptCookies;
	}

	private void register(ModeController modeController) {
		modeController.addExtension(IScriptEditorStarter.class, new IScriptEditorStarter() {
			public String startEditor(final String pScriptInput) {
				final ScriptModel scriptModel = new ScriptModel(pScriptInput);
				final ScriptEditorPanel scriptEditorPanel = new ScriptEditorPanel(scriptModel, false);
				scriptEditorPanel.setVisible(true);
				return scriptModel.getScript();
			}

			public ComboBoxEditor createComboBoxEditor(Dimension minimumSize) {
	            final ScriptComboBoxEditor scriptComboBoxEditor = new ScriptComboBoxEditor();
	            if(minimumSize != null)
	            	scriptComboBoxEditor.setMinimumSize(minimumSize);
				return scriptComboBoxEditor;
            }
		});
		modeController.addExtension(IScriptStarter.class, new IScriptStarter() {
			public void executeScript(NodeModel node, String script) {
				ScriptingEngine.executeScript(node, script);
			}
		});
		registerScriptAddOns();
		addPropertiesToOptionPanel();
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		modeController.addAction(new ScriptEditor());
		modeController.addAction(new ExecuteScriptForAllNodes());
		modeController.addAction(new ExecuteScriptForSelectionAction());
		final ManageAddOnsAction manageAddOnsAction = new ManageAddOnsAction();
		modeController.addAction(manageAddOnsAction);
		modeController.addExtension(AddOnInstaller.class, new AddOnInstaller() {
			public void install(final URL url) {
				final ManageAddOnsDialog dialog = manageAddOnsAction.getDialog();
				dialog.install(url);
            }
		});
		final ScriptingConfiguration configuration = new ScriptingConfiguration();
		ScriptingEngine.setClasspath(configuration.getClasspath());
		modeController.addMenuContributor(new IMenuContributor() {
			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				registerScripts(menuBuilder, configuration);
			}
		});
		createUserScriptsDirectory();
		FilterController.getCurrentFilterController().getConditionFactory().addConditionController(10,
		    new ScriptConditionController());
	}

	private void registerScriptAddOns() {
		File[] addonXmlFiles = AddOnsController.getController().getAddOnsDir().listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".script.xml");
			}
		});
		final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		for (File file : addonXmlFiles) {
			BufferedInputStream inputStream = null;
			try {
				inputStream = new BufferedInputStream(new FileInputStream(file));
				final IXMLReader reader = new StdXMLReader(inputStream);
				parser.setReader(reader);
				final ScriptAddOnProperties addOn = new ScriptAddOnProperties((XMLElement) parser.parse());
				addOn.setAddOnPropertiesFile(file);
				AddOnsController.getController().registerInstalledAddOn(addOn);
			}
			catch (final Exception e) {
				LogUtils.warn("error parsing " + file, e);
			}
			finally {
				FileUtils.silentlyClose(inputStream);
			}
		}
	}

	private void createUserScriptsDirectory() {
		final File scriptDir = ScriptingEngine.getUserScriptDir();
		if (!scriptDir.exists()) {
			LogUtils.info("creating user scripts directory " + scriptDir);
			scriptDir.mkdirs();
		}
	}

	private void registerScripts(final MenuBuilder menuBuilder, ScriptingConfiguration configuration) {
		final HashSet<String> registeredLocations = new HashSet<String>();
		for (final String scriptsParentLocation : ScriptingConfiguration.getScriptsParentLocations()) {
			final String scriptsLocation = ScriptingConfiguration.getScriptsLocation(scriptsParentLocation);
			addSubMenu(menuBuilder, scriptsParentLocation, scriptsLocation, TextUtils.getText("ExecuteScripts.text"));
			registeredLocations.add(scriptsLocation);
			if (configuration.getNameScriptMap().isEmpty()) {
				final String message = "<html><body><em>" + TextUtils.getText("ExecuteScripts.noScriptsAvailable")
				        + "</em></body></html>";
				menuBuilder.addElement(scriptsLocation, new JMenuItem(message), 0);
			}
			for (final Entry<String, String> entry : configuration.getNameScriptMap().entrySet()) {
				final String scriptName = entry.getKey();
				final ScriptMetaData metaData = configuration.getNameScriptMetaDataMap().get(scriptName);
				// in the worst case three actions will cache a script - should not matter that much since it's unlikely
				// that one script is used in multiple modes by the same user
				for (final ExecutionMode executionMode : metaData.getExecutionModes()) {
					final String titleKey;
					final String scriptLocation;
					String location = metaData.getMenuLocation(executionMode);
					// FIXME: reduce code duplication (VB)
					if (location == null) {
						location = scriptsLocation + "/" + scriptName;
						if (!registeredLocations.contains(location)) {
							final String parentMenuTitle = pimpMenuTitle(metaData.getScriptName());
							addSubMenu(menuBuilder, parentLocation(location), location, parentMenuTitle);
							registeredLocations.add(location);
						}
						titleKey = metaData.getTitleKey(executionMode);
						scriptLocation = location + "/" + titleKey;
					}
					else {
						if (!registeredLocations.contains(location)) {
							addSubMenu(menuBuilder, parentLocation(location), location, getMenuTitle(location));
							registeredLocations.add(location);
						}
						titleKey = metaData.getTitleKey(executionMode);
						scriptLocation = location + "/" + titleKey;
					}
					if (!registeredLocations.contains(scriptLocation)) {
						addMenuItem(menuBuilder, location, entry, executionMode, titleKey, metaData);
						registeredLocations.add(scriptLocation);
					}
				}
			}
		}
	}

	// location might be something like /menu_bar/edit/editGoodies
	private String getMenuTitle(final String location) {
	    int index = location.lastIndexOf('/');
	    final String lastKey = location.substring(index + 1);
	    return TextUtils.getText(lastKey, TextUtils.getText("addons." + lastKey, lastKey));
    }

	private String parentLocation(String location) {
	    return location.replaceFirst("/[^/]*$", "");
    }

	private void addSubMenu(final MenuBuilder menuBuilder, final String parentLocation, final String location,
	                        String menuTitle) {
		if (menuBuilder.get(location) == null) {
			final JMenu menuItem = new JMenu();
			MenuBuilder.setLabelAndMnemonic(menuItem, menuTitle);
			menuBuilder.addMenuItem(parentLocation, menuItem, location, MenuBuilder.AS_CHILD);
		}
	}

	private void addMenuItem(final MenuBuilder menuBuilder, final String location, final Entry<String, String> entry,
	                         final ExecutionMode executionMode, final String titleKey, ScriptMetaData metaData) {
		final String scriptName = entry.getKey();
		final String translation = TextUtils.getText(titleKey, titleKey.replace('_', ' '));
		final String menuName = translation.contains("{0}") ? MessageFormat.format(translation,
		    pimpMenuTitle(scriptName)) : translation;
		menuBuilder.addAction(location, new ExecuteScriptAction(scriptName, menuName, entry.getValue(), executionMode,
		    metaData.cacheContent(), metaData.getPermissions()), MenuBuilder.AS_CHILD);
	}

	/** menuTitle may either be a scriptName or a translation key. */
	private String pimpMenuTitle(final String menuTitle) {
		final String translation = TextUtils.getText(menuTitle, null);
		// convert CamelCase to Camel Case
		return translation != null ? translation : menuTitle.replaceAll("([a-z])([A-Z])", "$1 $2");
	}
}
