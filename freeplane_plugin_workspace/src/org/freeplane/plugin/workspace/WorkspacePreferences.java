package org.freeplane.plugin.workspace;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.JCheckBoxMenuItem;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class WorkspacePreferences {

	private class CheckBoxAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1256514415353330887L;
		private String propertyKey;

		public CheckBoxAction(String key, String propertyKey) {
			super(key);
			this.propertyKey = propertyKey;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean checked = ((JCheckBoxMenuItem) e.getSource()).isSelected();
			Controller.getCurrentController().getResourceController().setProperty(this.propertyKey, checked);

			if (checked) {
				// TODO: setVisible
			}
			else {
				// TODO: setInvisible
			}
		}

	}

	public static final String VIEW_ACTION = "viewaction";
	public static final String MENU_BAR = "/menu_bar";
	public static final String VIEW_MENU = "/view";

	public static final String SHOW_WORKSPACE_TEXT = "show_workspace";
	public static final String SHOW_WORKSPACE_RESOURCE = "show_workspace";
	
	private ModeController modeController;

	public WorkspacePreferences() {
		LogUtils.info("WorkspacePreferences");
		this.modeController = Controller.getCurrentModeController();
		addLanguageResources();
		addMenuEntries();
	}
	
	private void addLanguageResources() {
		ResourceBundles resBundle = ((ResourceBundles) modeController
				.getController().getResourceController().getResources());

		String lang = resBundle.getLanguageCode();
		if (lang == null || lang.equals(ResourceBundles.LANGUAGE_AUTOMATIC)) {
			lang = "en";
		}

		final URL res = this.getClass().getResource(
				"/translations/Resources_" + lang + ".properties");
		resBundle.addResources(resBundle.getLanguageCode(), res);
	}

	private void addMenuEntries() {

		this.modeController.addMenuContributor(new IMenuContributor() {
			ResourceController resourceController = Controller.getCurrentController().getResourceController();

			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				builder.addCheckboxItem(MENU_BAR + VIEW_MENU, new CheckBoxAction(SHOW_WORKSPACE_TEXT, SHOW_WORKSPACE_RESOURCE),
						resourceController.getBooleanProperty(SHOW_WORKSPACE_RESOURCE));

			}
		});
	}
}
