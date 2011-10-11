package org.docear.plugin.bibtex;

import java.net.URL;

import javax.swing.JMenu;

import org.docear.plugin.bibtex.actions.AddExistingReferenceAction;
import org.docear.plugin.bibtex.actions.AddNewReferenceAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesAllOpenMapsAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesCurrentMapAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesInLibrary;
import org.docear.plugin.core.ALanguageController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;


public class ReferencesController extends ALanguageController{
	
	
	public static final String MENU_BAR = "/menu_bar"; //$NON-NLS-1$
	public static final String NODE_POPUP_MENU = "/node_popup"; //$NON-NLS-1$
	public static final String NODE_FEATURES_MENU = "/node_features"; //$NON-NLS-1$
	public static final String STYLES_MENU = "/styles"; //$NON-NLS-1$
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
	private AFreeplaneAction UpdateReferencesCurrentMap = new UpdateReferencesCurrentMapAction(UPDATE_REFERENCES_CURRENT_MAP_LANG_KEY);
	private AFreeplaneAction UpdateReferencesAllOpenMaps = new UpdateReferencesAllOpenMapsAction(UPDATE_REFERENCES_ALL_OPEN_MAPS_LANG_KEY);
	private AFreeplaneAction UpdateReferencesInLibrary = new UpdateReferencesInLibrary(UPDATE_REFERENCES_IN_LIBRARY_LANG_KEY);
	private AFreeplaneAction AddExistingReference = new AddExistingReferenceAction(ADD_EXISTING_REFERENCES_LANG_KEY);
	private AFreeplaneAction AddNewReference = new AddNewReferenceAction(ADD_NEW_REFERENCE_LANG_KEY);

	public ReferencesController(ModeController modeController) {		
		this.modeController = modeController;
		LogUtils.info("starting DocearReferencesController(ModeController)"); //$NON-NLS-1$
		
		this.addPropertiesToOptionPanel();
		this.addPluginDefaults();
		this.addMenuEntries();
		this.initJabref();
	}

	private void initJabref() {
		
		/*final ClassLoader classLoader =  getClass().getClassLoader();

		     Thread thread = new Thread() {
		       public void run() {
		         Thread.currentThread().setContextClassLoader(classLoader);
		         JabrefWrapper wrapper = new JabrefWrapper(new String[]{ "-s" });   
		       }
		     };

		     thread.start();
		
		*/
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
		
		((MModeController)modeController).getOptionPanelBuilder().load(preferences);		
	}
	
private void addMenuEntries() {
		
		this.modeController.addMenuContributor(new IMenuContributor() {

			public void updateMenus(ModeController modeController, MenuBuilder builder) {				

				builder.addMenuItem(MENU_BAR + STYLES_MENU, new JMenu(TextUtils.getText(REFERENCE_MANAGEMENT_MENU_LANG_KEY)), MENU_BAR
						+ REFERENCE_MANAGEMENT_MENU, MenuBuilder.AFTER);				
				
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, AddNewReference, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, AddExistingReference, MenuBuilder.AS_CHILD);
								
				builder.addMenuItem(MENU_BAR + REFERENCE_MANAGEMENT_MENU,
						new JMenu(TextUtils.getText(UPDATE_REFERENCES_MENU_LANG_KEY)), MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesCurrentMap, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesAllOpenMaps, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesInLibrary, MenuBuilder.AS_CHILD);
				
				builder.addMenuItem(NODE_POPUP_MENU + NODE_FEATURES_MENU,
						new JMenu(TextUtils.getText(REFERENCE_MANAGEMENT_MENU_LANG_KEY)), NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU,
						MenuBuilder.BEFORE);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU, AddNewReference, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU, AddExistingReference, MenuBuilder.AS_CHILD);				
				builder.addMenuItem(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU, new JMenu(TextUtils.getText(UPDATE_REFERENCES_MENU_LANG_KEY)), NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesCurrentMap, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesAllOpenMaps, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesInLibrary, MenuBuilder.AS_CHILD);
										
			}
		});
	}

}
