package org.freeplane.plugin.workspace;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;

import javax.swing.JCheckBoxMenuItem;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class WorkspacePreferences {

	private static final String DEFAULT_LANGUAGE = "en";
	public static final String VIEW_ACTION = "viewaction";
	public static final String MENU_BAR = "/menu_bar";
	public static final String VIEW_MENU = "/view";

	public static final String SHOW_WORKSPACE_TEXT = "show_workspace";
	public static final String SHOW_WORKSPACE_PROPERTY_KEY = "show_workspace";
	public static final String WORKSPACE_WIDTH_PROPERTY_KEY = "workspace_view_width";
	public static final String WORKSPACE_LOCATION = "workspace_location";
	public static final String WORKSPACE_LOCATION_NEW = "workspace_location_new";
	public static final String WORKSPACE_PROFILE = "workspace.profile";
	public static final String WORKSPACE_PROFILE_DEFAULT = "default";

	private ModeController modeController;

	public WorkspacePreferences() {
		LogUtils.info("WorkspacePreferences");
		this.modeController = Controller.getCurrentModeController();
		addLanguageResources();
		addMenuEntries();
		addDefaultPreferences();
		addPreferencesToOptionsPanel();
	}
	
	public String getWorkspaceProfile() {
		return Controller.getCurrentController().getResourceController().getProperty(WORKSPACE_PROFILE, WORKSPACE_PROFILE_DEFAULT);
	}
	
	public void setWorkspaceProfile(String profile) {
		Controller.getCurrentController().getResourceController().setProperty(WORKSPACE_PROFILE, (profile == null || profile.trim().length() <= 0 ) ? WORKSPACE_PROFILE_DEFAULT : profile);
	}

	private void addDefaultPreferences() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	private void addLanguageResources() {
		ResourceBundles resBundle = ((ResourceBundles)Controller.getCurrentModeController().getController().getResourceController().getResources());
		String lang = resBundle.getLanguageCode();
		if (lang == null || lang.equals(ResourceBundles.LANGUAGE_AUTOMATIC)) {
			lang = DEFAULT_LANGUAGE;
		}
		
		URL res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
		if (res == null) {
			lang = DEFAULT_LANGUAGE;
			res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
		}
		
		File f = new File(res.getPath());
		if (!f.exists()) {
			lang = DEFAULT_LANGUAGE;
			res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
		}
				
		resBundle.addResources(resBundle.getLanguageCode(), res);
		
	}

	private void addPreferencesToOptionsPanel() {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null)
			throw new RuntimeException("cannot open preferences");
		MModeController modeController = (MModeController) Controller.getCurrentModeController();

		modeController.getOptionPanelBuilder().load(preferences);

	}

	private void addMenuEntries() {

		this.modeController.addMenuContributor(new IMenuContributor() {
			ResourceController resourceController = Controller.getCurrentController().getResourceController();

			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				builder.addCheckboxItem(MENU_BAR + VIEW_MENU,
						new CheckBoxAction(SHOW_WORKSPACE_TEXT, SHOW_WORKSPACE_PROPERTY_KEY),
						resourceController.getBooleanProperty(SHOW_WORKSPACE_PROPERTY_KEY));
			}
		});
	}

	private class CheckBoxAction extends AFreeplaneAction {

		private static final long serialVersionUID = 1256514415353330887L;
		private String propertyKey;

		public CheckBoxAction(String key, String propertyKey) {
			super(key);
			this.propertyKey = propertyKey;
		}

		public void actionPerformed(ActionEvent e) {
			boolean checked = ((JCheckBoxMenuItem) e.getSource()).isSelected();
			Controller.getCurrentController().getResourceController().setProperty(this.propertyKey, checked);

			if (checked) {
				String currentLocation = WorkspaceController.getController().getWorkspaceLocation();
				if (currentLocation == null || currentLocation.length()==0) {
					LocationDialog locationDialog = new LocationDialog();
					locationDialog.setVisible(true);
				}
				WorkspaceController.getController().showWorkspace(true);
			}
			else {
				WorkspaceController.getController().showWorkspace(false);
			}
		}
	}

}
