package org.freeplane.plugin.workspace;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class WorkspacePreferences {

	private static final String DEFAULT_LANGUAGE = "en";
	public static final String RELATIVE_TO_WORKSPACE = "relative_to_workspace";
	public static final String LINK_PROPERTY_KEY = "links";
	
	public static final String VIEW_ACTION = "viewaction";
	public static final String MENU_BAR = "/menu_bar";
	public static final String VIEW_MENU = "/view";

	public static final String SHOW_WORKSPACE_MENUITEM = "show_workspace";
	public static final String SHOW_WORKSPACE_PROPERTY_KEY = "workspace.enabled";
	public static final String COLLAPSE_WORKSPACE_PROPERTY_KEY = "workspace.collapsed";
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
	}
	
	public String getWorkspaceProfile() {
		return Controller.getCurrentController().getResourceController().getProperty(WORKSPACE_PROFILE, WORKSPACE_PROFILE_DEFAULT);
	}
	
	public String getWorkspaceProfileHome() {
		return getWorkspaceProfilesRoot() + getWorkspaceProfile();
	}
	
	public String getWorkspaceProfilesRoot() {
		return "_data/profile/";
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

	private void addMenuEntries() {
		//Controller.getCurrentModeController().addAction(new CheckBoxAction(SHOW_WORKSPACE_MENUITEM));
		
		this.modeController.addMenuContributor(new IMenuContributor() {
			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				builder.addAction(MENU_BAR + VIEW_MENU,	new CheckBoxAction(SHOW_WORKSPACE_MENUITEM), IndexedTree.AS_CHILD);				
			}
		});
	}

	public String getWorkspaceLocation() {
		return Controller.getCurrentController().getResourceController().getProperty(WorkspacePreferences.WORKSPACE_LOCATION);
	}
	
	public void setNewWorkspaceLocation(URI newWorkspaceLocation) {
		if (newWorkspaceLocation == null) {
			ResourceController.getResourceController().getProperties().remove(WORKSPACE_LOCATION);
			return;
		}
		
		File f = WorkspaceUtils.resolveURI(newWorkspaceLocation);
		if (f != null) {
			if (!f.exists()) {				
				if (!f.mkdirs()) {
					JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
							TextUtils.getText("error_create_workspace_folder") + " " + f.getAbsolutePath(),
							TextUtils.getText("error_create_workspace_folder_title"), JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			ResourceController.getResourceController().setProperty(WorkspacePreferences.WORKSPACE_LOCATION, f.getAbsolutePath());			
		}
	}
	

	@SelectableAction(checkOnPropertyChange=SHOW_WORKSPACE_PROPERTY_KEY, checkOnPopup = true)
	private class CheckBoxAction extends AFreeplaneAction {

		private static final long serialVersionUID = 1256514415353330887L;

		public CheckBoxAction(String key) {
			super(key);
		}

		public void actionPerformed(ActionEvent e) {
			if (!isSelected()) {
				String currentLocation = getWorkspaceLocation();
				if (currentLocation == null || currentLocation.length()==0) {
					WorkspaceUtils.showWorkspaceChooserDialog();
				}
				WorkspaceController.getController().showWorkspace(true);
			}
			else {
				WorkspaceController.getController().showWorkspace(false);
			}
		}
		
		public void setSelected() {
			setSelected(ResourceController.getResourceController().getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY));
		}
	}

}
