package org.docear.plugin.bibtex;

import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.docear.plugin.bibtex.actions.AddExistingReferenceAction;
import org.docear.plugin.bibtex.actions.AddNewReferenceAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesAllOpenMapsAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesCurrentMapAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesInLibrary;
import org.docear.plugin.core.ALanguageController;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.IDocearEventListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.controller.IWorkspaceListener;
import org.freeplane.plugin.workspace.controller.WorkspaceEvent;

public class ReferencesController extends ALanguageController implements IDocearEventListener, IWorkspaceListener {
	
	private static ReferencesController referencesController = null;
	private JabrefWrapper jabrefWrapper;

	public static final String MENU_BAR = "/menu_bar"; //$NON-NLS-1$
	public static final String NODE_POPUP_MENU = "/node_popup"; //$NON-NLS-1$
	public static final String NODE_FEATURES_MENU = "/node_features"; //$NON-NLS-1$
	public static final String TOOLS_MENU = "/extras"; //$NON-NLS-1$
	public static final String REFERENCE_MANAGEMENT_MENU = "/reference_management";
	public static final String UPDATE_REFERENCES_MENU = "/update_references";

	public static final String REFERENCE_MANAGEMENT_MENU_LANG_KEY = "menu_reference_management";
	public static final String UPDATE_REFERENCES_MENU_LANG_KEY = "menu_update_references";
	private static final String ADD_NEW_REFERENCE_LANG_KEY = "menu_add_new_reference";
	private static final String ADD_EXISTING_REFERENCES_LANG_KEY = "menu_add_existing_references";
	private static final String UPDATE_REFERENCES_IN_LIBRARY_LANG_KEY = "menu_update_references_in_library";
	private static final String UPDATE_REFERENCES_ALL_OPEN_MAPS_LANG_KEY = "menu_update_references_all_open_maps";
	private static final String UPDATE_REFERENCES_CURRENT_MAP_LANG_KEY = "menu_update_references_current_map";

	private ModeController modeController;
	private AFreeplaneAction UpdateReferencesCurrentMap = new UpdateReferencesCurrentMapAction(
			UPDATE_REFERENCES_CURRENT_MAP_LANG_KEY);
	private AFreeplaneAction UpdateReferencesAllOpenMaps = new UpdateReferencesAllOpenMapsAction(
			UPDATE_REFERENCES_ALL_OPEN_MAPS_LANG_KEY);
	private AFreeplaneAction UpdateReferencesInLibrary = new UpdateReferencesInLibrary(UPDATE_REFERENCES_IN_LIBRARY_LANG_KEY);
	private AFreeplaneAction AddExistingReference = new AddExistingReferenceAction(ADD_EXISTING_REFERENCES_LANG_KEY);
	private AFreeplaneAction AddNewReference = new AddNewReferenceAction(ADD_NEW_REFERENCE_LANG_KEY);
	private boolean isRunning = false;

	public ReferencesController(ModeController modeController) {
		setReferencesController(this);
		this.modeController = modeController;
		LogUtils.info("starting DocearReferencesController(ModeController)"); //$NON-NLS-1$

		this.addPropertiesToOptionPanel();
		this.addPluginDefaults();
		this.addMenuEntries();
		DocearController.getController().addDocearEventListener(this);
		WorkspaceController.getController().addWorkspaceListener(this);
		this.initJabref();
	}

	public static ReferencesController getReferencesController() {
		return referencesController;
	}

	public static void setReferencesController(ReferencesController referencesController) {
		ReferencesController.referencesController = referencesController;
	}

	private void createOptionPanel(JPanel comp) {
		try {
			System.out.println("JabrefPane: " + modeController);
			final JTabbedPane tabs = (JTabbedPane) modeController.getUserInputListenerFactory().getToolBar("/format")
					.getComponent(1);
			tabs.add(TextUtils.getText("jabref"), comp);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initJabref() {
		if(WorkspaceController.getController().isInitialized() && !isRunning) {
			
			final ClassLoader classLoader = getClass().getClassLoader();
			isRunning  = true;
			Thread thread = new Thread() {
				
				public void run() {
					Thread.currentThread().setContextClassLoader(classLoader);
					URI uri = DocearController.getController().getLibrary().getBibtexDatabase();
					
					if (uri != null) {
						jabrefWrapper = new JabrefWrapper(Controller.getCurrentController().getViewController().getJFrame(), new File(WorkspaceUtils.absoluteURI(uri)));						
					}
					else {
						jabrefWrapper = new JabrefWrapper(Controller.getCurrentController().getViewController().getJFrame());
					}
					createOptionPanel(jabrefWrapper.getJabrefFrame());
				}
			};
	
			thread.start();
		}
	}
	
	public JabrefWrapper getJabrefWrapper() {
		return jabrefWrapper;
	}

	public void setJabrefWrapper(JabrefWrapper jabrefWrapper) {
		this.jabrefWrapper = jabrefWrapper;
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE); //$NON-NLS-1$
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	private void addPropertiesToOptionPanel() {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null)
			throw new RuntimeException("cannot open docear.bibtex plugin preferences"); //$NON-NLS-1$

		((MModeController) modeController).getOptionPanelBuilder().load(preferences);
	}

	private void addMenuEntries() {

		this.modeController.addMenuContributor(new IMenuContributor() {

			public void updateMenus(ModeController modeController, MenuBuilder builder) {

				builder.addMenuItem(MENU_BAR + TOOLS_MENU, new JMenu(TextUtils.getText(REFERENCE_MANAGEMENT_MENU_LANG_KEY)),
						MENU_BAR + REFERENCE_MANAGEMENT_MENU, MenuBuilder.BEFORE);

				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, AddNewReference, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, AddExistingReference, MenuBuilder.AS_CHILD);

				builder.addMenuItem(MENU_BAR + REFERENCE_MANAGEMENT_MENU,
						new JMenu(TextUtils.getText(UPDATE_REFERENCES_MENU_LANG_KEY)), MENU_BAR + REFERENCE_MANAGEMENT_MENU
								+ UPDATE_REFERENCES_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesCurrentMap,
						MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesAllOpenMaps,
						MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesInLibrary,
						MenuBuilder.AS_CHILD);

				builder.addMenuItem(NODE_POPUP_MENU + NODE_FEATURES_MENU,
						new JMenu(TextUtils.getText(REFERENCE_MANAGEMENT_MENU_LANG_KEY)), NODE_POPUP_MENU
								+ REFERENCE_MANAGEMENT_MENU, MenuBuilder.BEFORE);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU, AddNewReference, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU, AddExistingReference, MenuBuilder.AS_CHILD);
				builder.addMenuItem(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU,
						new JMenu(TextUtils.getText(UPDATE_REFERENCES_MENU_LANG_KEY)), NODE_POPUP_MENU
								+ REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						UpdateReferencesCurrentMap, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						UpdateReferencesAllOpenMaps, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						UpdateReferencesInLibrary, MenuBuilder.AS_CHILD);

			}
		});
	}
	
	

	public void handleEvent(DocearEvent event) {
		System.out.println("JabrefWrapper DocearEvent: "+ event);
	}

	public void workspaceChanged(WorkspaceEvent event) {
		// TODO Auto-generated method stub
		
	}
}
