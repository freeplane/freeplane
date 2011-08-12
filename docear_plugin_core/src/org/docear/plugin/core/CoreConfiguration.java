package org.docear.plugin.core;

import java.net.URL;
import java.util.Enumeration;

import org.docear.plugin.core.actions.DocearLicenseAction;
import org.docear.plugin.core.actions.DocearOpenUrlAction;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.WorkspaceController;

public class CoreConfiguration extends ALanguageController implements IFreeplanePropertyListener {

	private static final String ABOUT_TEXT = "about_text";
	private static final String DOCEAR = "Docear";
	private static final String APPLICATION_NAME = "ApplicationName";
	private static final String LICENSE_ACTION = "LicenseAction";
	private static final String DOCUMENTATION_ACTION = "DocumentationAction";
	private static final String DOCEAR_WEB_DOCU_LOCATION = "docear_webDocuLocation";
	private static final String WEB_DOCU_LOCATION = "webDocuLocation";
	private static final String REQUEST_FEATURE_ACTION = "RequestFeatureAction";
	private static final String DOCEAR_FEATURE_TRACKER_LOCATION = "docear_featureTrackerLocation";
	private static final String FEATURE_TRACKER_LOCATION = "featureTrackerLocation";
	private static final String ASK_FOR_HELP = "AskForHelp";
	private static final String HELP_FORUM_LOCATION = "helpForumLocation";
	private static final String REPORT_BUG_ACTION = "ReportBugAction";
	private static final String DOCEAR_BUG_TRACKER_LOCATION = "docear_bugTrackerLocation";
	private static final String BUG_TRACKER_LOCATION = "bugTrackerLocation";
	private static final String OPEN_FREEPLANE_SITE_ACTION = "OpenFreeplaneSiteAction";
	private static final String WEB_DOCEAR_LOCATION = "webDocearLocation";
	private static final String WEB_FREEPLANE_LOCATION = "webFreeplaneLocation";

	private static final String WORKSPACE_PDF_LOCATION = "workspace_pdf_location";

	public CoreConfiguration(ModeController modeController) {
		addPropertyChangeListener();
		addPreferencesToOptionsPanel();
		LogUtils.info("org.docear.plugin.core.CoreConfiguration() initializing...");
		init(modeController);
	}
	
	private void addPropertyChangeListener() {
		ResourceController resCtrl = Controller.getCurrentController().getResourceController();
		resCtrl.addPropertyChangeListener(this);
	}
	

	private void addPreferencesToOptionsPanel() {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null)
			throw new RuntimeException("cannot open preferences");
		MModeController modeController = (MModeController) Controller.getCurrentModeController();

		modeController.getOptionPanelBuilder().load(preferences);

	}

	private void init(ModeController modeController) {
		addPluginDefaults();
		replaceFreeplaneStringsAndActions();
	}

	private void replaceFreeplaneStringsAndActions() {
		ResourceController resourceController = ResourceController.getResourceController();

		if (!resourceController.getProperty(APPLICATION_NAME, "").equals(DOCEAR)) {
			return;
		}

		replaceResourceBundleStrings();

		replaceActions();
	}

	private void replaceActions() {
		ResourceController resourceController = ResourceController.getResourceController();

		resourceController.setProperty(WEB_FREEPLANE_LOCATION, resourceController.getProperty(WEB_DOCEAR_LOCATION));
		replaceAction(OPEN_FREEPLANE_SITE_ACTION,
				new DocearOpenUrlAction(OPEN_FREEPLANE_SITE_ACTION, resourceController.getProperty(WEB_FREEPLANE_LOCATION)));
		resourceController.setProperty(BUG_TRACKER_LOCATION, resourceController.getProperty(DOCEAR_BUG_TRACKER_LOCATION));
		replaceAction(REPORT_BUG_ACTION,
				new DocearOpenUrlAction(REPORT_BUG_ACTION, resourceController.getProperty(BUG_TRACKER_LOCATION)));
		resourceController.setProperty(HELP_FORUM_LOCATION, resourceController.getProperty("docear_helpForumLocation"));
		replaceAction(ASK_FOR_HELP, new DocearOpenUrlAction(ASK_FOR_HELP, resourceController.getProperty(HELP_FORUM_LOCATION)));
		resourceController.setProperty(FEATURE_TRACKER_LOCATION, resourceController.getProperty(DOCEAR_FEATURE_TRACKER_LOCATION));
		replaceAction(REQUEST_FEATURE_ACTION,
				new DocearOpenUrlAction(REQUEST_FEATURE_ACTION, resourceController.getProperty(FEATURE_TRACKER_LOCATION)));
		resourceController.setProperty(WEB_DOCU_LOCATION, resourceController.getProperty(DOCEAR_WEB_DOCU_LOCATION));
		replaceAction(DOCUMENTATION_ACTION,
				new DocearOpenUrlAction(DOCUMENTATION_ACTION, resourceController.getProperty(WEB_DOCU_LOCATION)));
		replaceAction(LICENSE_ACTION, new DocearLicenseAction(LICENSE_ACTION));
	}

	private void replaceResourceBundleStrings() {
		ResourceController resourceController = ResourceController.getResourceController();
		ResourceBundles bundles = ((ResourceBundles) resourceController.getResources());
		Controller controller = Controller.getCurrentController();

		for (Enumeration<?> i = bundles.getKeys(); i.hasMoreElements();) {
			String key = i.nextElement().toString();
			String value = bundles.getResourceString(key);
			if (value.matches(".*[Ff][Rr][Ee][Ee][Pp][Ll][Aa][Nn][Ee].*")) {
				value = value.replaceAll("[Ff][Rr][Ee][Ee][Pp][Ll][Aa][Nn][Ee]", DOCEAR);
				bundles.putResourceString(key, value);
				if (key.matches(".*[.text]")) {
					key = key.replace(".text", "");
					AFreeplaneAction action = controller.getAction(key);
					if (action != null) {
						MenuBuilder.setLabelAndMnemonic(action, value);
					}
				}
			}
		}

		bundles.putResourceString(ABOUT_TEXT, "Docear About Text.\nDocear Version: 1.0 Alpha \nFreeplane Version: ");
	}

	private void replaceAction(String actionKey, AFreeplaneAction action) {
		Controller controller = Controller.getCurrentController();

		controller.removeAction(actionKey);
		controller.addAction(action);
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		System.out.println("DOCEAR propertyChanged: "+propertyName);
		if (propertyName.equals(WORKSPACE_PDF_LOCATION)) {
			WorkspaceController.getCurrentWorkspaceController().refreshWorkspace();
		}
	}

}
