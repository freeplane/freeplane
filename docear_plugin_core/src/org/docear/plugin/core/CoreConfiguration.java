package org.docear.plugin.core;

import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;

import org.docear.plugin.core.actions.DocearLicenseAction;
import org.docear.plugin.core.actions.DocearOpenUrlAction;
import org.docear.plugin.core.actions.DocearQuitAction;
import org.docear.plugin.core.actions.SaveAction;
import org.docear.plugin.core.actions.SaveAsAction;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.features.DocearMapWriter;
import org.docear.plugin.core.features.DocearNodeModelExtensionController;
import org.docear.plugin.core.listeners.PropertyListener;
import org.docear.plugin.core.listeners.WorkspaceChangeListener;
import org.docear.plugin.core.workspace.actions.DocearChangeLibraryPathAction;
import org.docear.plugin.core.workspace.actions.DocearRenameAction;
import org.docear.plugin.core.workspace.actions.WorkspaceChangeLocationsAction;
import org.docear.plugin.core.workspace.creator.FolderTypeLibraryCreator;
import org.docear.plugin.core.workspace.creator.FolderTypeLiteratureRepositoryCreator;
import org.docear.plugin.core.workspace.creator.FolderTypeProjectsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeIncomingCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeLiteratureAnnotationsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeMyPublicationsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeReferencesCreator;
import org.docear.plugin.core.workspace.node.config.NodeAttributeObserver;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.FreeplaneActionCascade;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

public class CoreConfiguration extends ALanguageController {

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
	private static final String DOCEAR_FIRST_RUN_PROPERTY = "docear.already_initialized";
	

//	public static final String DOCUMENT_REPOSITORY_PATH = DocearController.DOCUMENT_REPOSITORY_PATH_PROPERTY;
	public static final String LIBRARY_PATH = "@@library_mindmaps@@"; 
//	public static final String BIBTEX_PATH = DocearController.BIBTEX_PATH_PROPERTY;
	
	private static final WorkspaceChangeListener WORKSPACE_CHANGE_LISTENER = new WorkspaceChangeListener();
		
	public static final NodeAttributeObserver projectPathObserver = new NodeAttributeObserver();
	public static final NodeAttributeObserver referencePathObserver = new NodeAttributeObserver();
	public static final NodeAttributeObserver repositoryPathObserver = new NodeAttributeObserver();
	private final boolean firstRun;
	
	public CoreConfiguration(ModeController modeController) {
		try {
			if (WorkspaceController.getController().isInitialized()) {
				//showLocationDialogIfNeeded();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
				
		LogUtils.info("org.docear.plugin.core.CoreConfiguration() initializing...");
		firstRun = !ResourceController.getResourceController().getBooleanProperty(DOCEAR_FIRST_RUN_PROPERTY);
		ResourceController.getResourceController().setProperty(DOCEAR_FIRST_RUN_PROPERTY, true);
		init(modeController);
	}
	
	public boolean isDocearFirstStart() {
		return firstRun;
	}
	
	private void init(ModeController modeController) {
		// set up context menu for workspace
		WorkspaceController.getController().addWorkspaceListener(WORKSPACE_CHANGE_LISTENER);
		
		modeController.addAction(new WorkspaceChangeLocationsAction());
		modeController.addAction(new DocearChangeLibraryPathAction());
		modeController.addAction(new DocearRenameAction());
		
		addPluginDefaults();
		prepareWorkspace();
		
		replaceFreeplaneStringsAndActions();
		DocearMapModelController.install(new DocearMapModelController(modeController));
		
				
		modifyContextMenus();
		
		setDocearMapWriter();
		
		registerController(modeController);
		URI uri = CoreConfiguration.projectPathObserver.getUri();
		if (uri != null) {
			UrlManager.getController().setLastCurrentDir(WorkspaceUtils.resolveURI(CoreConfiguration.projectPathObserver.getUri()));
		}
	}
	
	private void setDocearMapWriter() {
		DocearMapWriter mapWriter = new DocearMapWriter(Controller.getCurrentModeController().getMapController());
		mapWriter.setMapWriteHandler();		
	}

	private void registerController(ModeController modeController) {
		DocearNodeModelExtensionController.install(new DocearNodeModelExtensionController(modeController));		
	}
	
	private void prepareWorkspace() {
		WorkspaceController controller = WorkspaceController.getController();
		controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, FolderTypeLibraryCreator.FOLDER_TYPE_LIBRARY, new FolderTypeLibraryCreator());
		controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, FolderTypeLiteratureRepositoryCreator.FOLDER_TYPE_LITERATUREREPOSITORY, new FolderTypeLiteratureRepositoryCreator());
		controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, FolderTypeProjectsCreator.FOLDER_TYPE_PROJECTS, new FolderTypeProjectsCreator());
		controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, LinkTypeMyPublicationsCreator.LINK_TYPE_MYPUBLICATIONS , new LinkTypeMyPublicationsCreator());
		controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, LinkTypeReferencesCreator.LINK_TYPE_REFERENCES , new LinkTypeReferencesCreator());
		controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, LinkTypeLiteratureAnnotationsCreator.LINK_TYPE_LITERATUREANNOTATIONS , new LinkTypeLiteratureAnnotationsCreator());
		controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, LinkTypeIncomingCreator.LINK_TYPE_INCOMING , new LinkTypeIncomingCreator());
		
		controller.reloadWorkspace();
		controller.getConfiguration().linkWelcomeMindmapAfterWorkspaceCreation();
	}

	private void replaceFreeplaneStringsAndActions() {
		ResourceController resourceController = ResourceController.getResourceController();
		
		//replace this actions if docear_core is present
		Controller.getCurrentModeController().removeAction("SaveAsAction");
		Controller.getCurrentModeController().addAction(new SaveAsAction());
		Controller.getCurrentModeController().removeAction("SaveAction");
		Controller.getCurrentModeController().addAction(new SaveAction());

		if (!resourceController.getProperty(APPLICATION_NAME, "").equals(DOCEAR)) {
			return;
		}

		//replace if application name is docear
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
		String programmer = resourceController.getProperty("docear_programmer");
		String copyright = resourceController.getProperty("docear_copyright");
		String version	= resourceController.getProperty("docear_version");
		String status	= resourceController.getProperty("docear_status");
		
		String aboutText = TextUtils.getRawText("docear_about");
		MessageFormat formatter;
        try {
            formatter = new MessageFormat(aboutText);
            aboutText = formatter.format(new Object[]{ version+" "+status, copyright, programmer});
        }
        catch (IllegalArgumentException e) {
            LogUtils.severe("wrong format " + aboutText + " for property " + "docear_about", e);
        }		
		bundles.putResourceString(ABOUT_TEXT, aboutText);
	}

	private void replaceAction(String actionKey, AFreeplaneAction action) {
		Controller controller = Controller.getCurrentController();

		controller.removeAction(actionKey);
		controller.addAction(action);
	}

	private void addPluginDefaults() {
		
		ResourceController resController = Controller.getCurrentController().getResourceController();
		if (resController.getProperty("ApplicationName").equals("Docear")) {
			resController.setProperty("first_start_map", "/doc/docear-welcome.mm");
			resController.setProperty("tutorial_map", "/doc/docear-welcome.mm");
		}
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
		if (resController.getProperty("ApplicationName").equals("Docear") && isDocearFirstStart()) {
			Controller.getCurrentController().getResourceController().setProperty("selection_method", "selection_method_by_click");
			Controller.getCurrentController().getResourceController().setProperty("links", "relative_to_workspace");
			Controller.getCurrentController().getResourceController().setProperty("save_folding", "always_save_folding");
			Controller.getCurrentController().getResourceController().setProperty("leftToolbarVisible", "false");			
			Controller.getCurrentController().getResourceController().setProperty("styleScrollPaneVisible", "true");
			Controller.getCurrentController().getResourceController().setProperty(DOCEAR_FIRST_RUN_PROPERTY, true);
		}
		Controller.getCurrentController().getResourceController().addPropertyChangeListener(new PropertyListener());
		FreeplaneActionCascade.addAction(new DocearQuitAction());		
	}
	
	private void modifyContextMenus() {		
		AWorkspaceTreeNode root =  (AWorkspaceTreeNode) WorkspaceUtils.getModel().getRoot();
		WorkspacePopupMenuBuilder.insertAction(root.getContextMenu(), "workspace.action.docear.locations.change", 3);
	}
	
}
