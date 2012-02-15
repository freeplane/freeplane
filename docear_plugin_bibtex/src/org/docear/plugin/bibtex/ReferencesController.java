package org.docear.plugin.bibtex;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.sf.jabref.JabRefPreferences;

import org.docear.plugin.bibtex.actions.AddExistingReferenceAction;
import org.docear.plugin.bibtex.actions.AddNewReferenceAction;
import org.docear.plugin.bibtex.actions.CopyBibtexToClipboard;
import org.docear.plugin.bibtex.actions.ReferenceQuitAction;
import org.docear.plugin.bibtex.actions.RemoveReferenceAction;
import org.docear.plugin.bibtex.actions.ShowJabrefPreferencesAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesAllMapsAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesAllOpenMapsAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesCurrentMapAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesInLibrary;
import org.docear.plugin.bibtex.listeners.BibtexNodeDropListener;
import org.docear.plugin.bibtex.listeners.JabRefChangeListener;
import org.docear.plugin.bibtex.listeners.MapChangeListenerAdapter;
import org.docear.plugin.bibtex.listeners.NodeAttributeListener;
import org.docear.plugin.bibtex.listeners.NodeSelectionListener;
import org.docear.plugin.bibtex.listeners.SplmmMapsConvertListener;
import org.docear.plugin.core.ALanguageController;
import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.core.workspace.node.LinkTypeReferencesNode;
import org.docear.plugin.pdfutilities.util.MapConverter;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.FreeplaneActionCascade;
import org.freeplane.core.ui.IKeyStrokeInterceptor;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.INodeViewLifeCycleListener;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.view.swing.map.NodeView;

public class ReferencesController extends ALanguageController implements IDocearEventListener, ActionListener {

	//mapModel with reference which is currently changed
	private MapModel inChange = null;
	//MapModel with reference which is currently added
	private MapModel inAdd = null;
	
	private final static JabRefChangeListener jabRefChangeListener = new JabRefChangeListener();	
	
	private static ReferencesController referencesController = null;	
	
	JabrefWrapper jabrefWrapper;
	
	private JabRefAttributes jabRefAttributes;
	private SplmmAttributes splmmAttributes;
	
	private final NodeAttributeListener attributeListener = new NodeAttributeListener();
	private final SplmmMapsConvertListener splmmMapsConvertedListener = new SplmmMapsConvertListener();

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
	private static final String REMOVE_REFERENCE_LANG_KEY = "menu_remove_references";
	private static final String UPDATE_REFERENCES_IN_LIBRARY_LANG_KEY = "menu_update_references_in_library";
	private static final String UPDATE_REFERENCES_ALL_MAPS_LANG_KEY = "menu_update_references_all_maps";
	private static final String UPDATE_REFERENCES_ALL_OPEN_MAPS_LANG_KEY = "menu_update_references_all_open_maps";
	private static final String UPDATE_REFERENCES_CURRENT_MAP_LANG_KEY = "menu_update_references_current_map";
//	private static final String CONVERT_SPLMM_REFERENCES_LANG_KEY = "menu_update_splmm_references_current_map";
	private static final String COPY_BIBTEX_LANG_KEY = "menu_copy_bibtex";
	

	private ModeController modeController;
	private AFreeplaneAction UpdateReferencesCurrentMap = new UpdateReferencesCurrentMapAction(
			UPDATE_REFERENCES_CURRENT_MAP_LANG_KEY);
	private AFreeplaneAction UpdateReferencesAllOpenMaps = new UpdateReferencesAllOpenMapsAction(
			UPDATE_REFERENCES_ALL_OPEN_MAPS_LANG_KEY);
	private AFreeplaneAction UpdateReferencesInLibrary = new UpdateReferencesInLibrary(UPDATE_REFERENCES_IN_LIBRARY_LANG_KEY);
	private AFreeplaneAction UpdateReferencesAllMaps = new UpdateReferencesAllMapsAction(UPDATE_REFERENCES_ALL_MAPS_LANG_KEY);
//	private AFreeplaneAction ConvertSplmmReferences = new ConvertSplmmReferencesAction(CONVERT_SPLMM_REFERENCES_LANG_KEY);
	private AFreeplaneAction AddExistingReference = new AddExistingReferenceAction(ADD_EXISTING_REFERENCES_LANG_KEY);
	private AFreeplaneAction RemoveReference = new RemoveReferenceAction(REMOVE_REFERENCE_LANG_KEY);
	private AFreeplaneAction AddNewReference = new AddNewReferenceAction(ADD_NEW_REFERENCE_LANG_KEY);
	private AFreeplaneAction CopyBibtex = new CopyBibtexToClipboard(COPY_BIBTEX_LANG_KEY);
	
	private AFreeplaneAction ShowJabrefPreferences = new ShowJabrefPreferencesAction("show_jabref_preferences");
	
	private boolean isRunning = false;

	public ReferencesController(ModeController modeController) {
		super();
		setReferencesController(this);
		setPreferencesForDocear();
		this.modeController = modeController;
		LogUtils.info("starting DocearReferencesController(ModeController)"); //$NON-NLS-1$

		this.addPluginDefaults();
		this.addMenuEntries();
		this.registerListeners();
		
		FreeplaneActionCascade.addAction(new ReferenceQuitAction());
		this.initJabref();		
	}
	

	private void setPreferencesForDocear() {
		JabRefPreferences.getInstance().put("groupAutoShow", "false");
		JabRefPreferences.getInstance().put("searchPanelVisible", "false");
	}
	

	private void registerListeners() {
		MapConverter.addMapsConvertedListener(splmmMapsConvertedListener);
		
		this.modeController.addINodeViewLifeCycleListener(new INodeViewLifeCycleListener() {
			
			public void onViewCreated(Container nodeView) {
				NodeView node = (NodeView) nodeView;
				final DropTarget dropTarget = new DropTarget(node.getMainView(), new BibtexNodeDropListener());
				dropTarget.setActive(true);				
			}

			public void onViewRemoved(Container nodeView) {
			}
		});
		
		MapChangeListenerAdapter changeListenerAdapter = new MapChangeListenerAdapter();		
		this.modeController.getMapController().addNodeChangeListener(changeListenerAdapter);
		this.modeController.getMapController().addMapChangeListener(changeListenerAdapter);
		this.modeController.getMapController().addMapLifeCycleListener(changeListenerAdapter);
		
		DocearController.getController().addDocearEventListener(this);		
	}
	
	

	public static ReferencesController getController() {
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
			Dimension fixSize =  new Dimension(tabs.getComponent(0).getWidth(), 32000);
			comp.setPreferredSize(fixSize);
			tabs.add(TextUtils.getText("jabref"), comp);
			tabs.setSelectedComponent(comp);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initJabref() {		
		if(WorkspaceController.getController().isInitialized() && !isRunning) {
			this.jabRefAttributes = new JabRefAttributes();
			this.splmmAttributes = new SplmmAttributes();
			
			NodeSelectionListener nodeSelectionListener = new NodeSelectionListener();
			nodeSelectionListener.init();
			
			final ClassLoader classLoader = getClass().getClassLoader();
			isRunning  = true;
			try {
				SwingUtilities.invokeAndWait( new Runnable() {
//			Thread thread = new Thread() {
					public void run() {
						Thread.currentThread().setContextClassLoader(classLoader);
						URI uri = null;
						if(DocearController.getController().getLibrary() != null) {
							uri = DocearController.getController().getLibrary().getBibtexDatabase();
						}
						
						if (uri != null) {						
							jabrefWrapper = new JabrefWrapper(Controller.getCurrentController().getViewController().getJFrame(), CoreUtils.resolveURI(uri));						
						}
						else {
							jabrefWrapper = new JabrefWrapper(Controller.getCurrentController().getViewController().getJFrame());
						}
						//TODO: DOCEAR - (ticket #225) refactor with separate class  
						modeController.getUserInputListenerFactory().getMenuBar().addKeyStrokeInterceptor(new KeyBindInterceptor());
						createOptionPanel(jabrefWrapper.getJabrefFrame());					
					}
				});
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//	
//			thread.start();
		}
	}
	
	public JabRefAttributes getJabRefAttributes() {
		return jabRefAttributes;
	}
	
	public SplmmAttributes getSplmmAttributes() {
		return splmmAttributes;
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
	
	private void addMenuEntries() {

		this.modeController.addMenuContributor(new IMenuContributor() {

			public void updateMenus(ModeController modeController, MenuBuilder builder) {

				builder.addMenuItem(MENU_BAR + TOOLS_MENU, new JMenu(TextUtils.getText(REFERENCE_MANAGEMENT_MENU_LANG_KEY)),
						MENU_BAR + REFERENCE_MANAGEMENT_MENU, MenuBuilder.BEFORE);

				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, CopyBibtex,	MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, AddNewReference, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, AddExistingReference, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, RemoveReference, MenuBuilder.AS_CHILD);

				builder.addMenuItem(MENU_BAR + REFERENCE_MANAGEMENT_MENU,
						new JMenu(TextUtils.getText(UPDATE_REFERENCES_MENU_LANG_KEY)), MENU_BAR + REFERENCE_MANAGEMENT_MENU
								+ UPDATE_REFERENCES_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesCurrentMap,
						MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesAllOpenMaps,
						MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesInLibrary,
						MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesAllMaps,
						MenuBuilder.AS_CHILD);
//				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, ConvertSplmmReferences,
//						MenuBuilder.AS_CHILD);
				
				
				builder.addMenuItem(NODE_POPUP_MENU /*+ NODE_FEATURES_MENU*/,
						new JMenu(TextUtils.getText(REFERENCE_MANAGEMENT_MENU_LANG_KEY)), NODE_POPUP_MENU
								+ REFERENCE_MANAGEMENT_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU, CopyBibtex, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU, AddNewReference, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU, AddExistingReference, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU, RemoveReference, MenuBuilder.AS_CHILD);
				builder.addMenuItem(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU,
						new JMenu(TextUtils.getText(UPDATE_REFERENCES_MENU_LANG_KEY)), NODE_POPUP_MENU
								+ REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						UpdateReferencesCurrentMap, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						UpdateReferencesAllOpenMaps, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						UpdateReferencesInLibrary, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						UpdateReferencesAllMaps, MenuBuilder.AS_CHILD);
//				builder.addAction(NODE_POPUP_MENU + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, 
//						ConvertSplmmReferences,	MenuBuilder.AS_CHILD);
				
				builder.addAction(MENU_BAR + TOOLS_MENU, ShowJabrefPreferences, MenuBuilder.AS_CHILD);

			}
		});
	}
	
	

	public void handleEvent(DocearEvent event) {
		System.out.println("JabrefWrapper DocearEvent: "+ event);
		if(event.getType() == DocearEventType.LIBRARY_NEW_REFERENCES_INDEXING_REQUEST && event.getEventObject() instanceof LinkTypeReferencesNode) {
			final File file = WorkspaceUtils.resolveURI(CoreConfiguration.referencePathObserver.getUri());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ReferencesController contr = ReferencesController.getController();
					JabrefWrapper wrapper = contr.getJabrefWrapper();
					wrapper.replaceDatabase(file, true);					
				}
			});
		}
	}

	public NodeAttributeListener getAttributeListener() {
		return attributeListener;
	}
	
	public static JabRefChangeListener getJabRefChangeListener() {
		return jabRefChangeListener;
	}


	public MapModel getInChange() {
		return inChange;
	}


	public void setInChange(MapModel inChange) {
		this.inChange = inChange;
	}


	public MapModel getInAdd() {
		return inAdd;
	}


	public void setInAdd(MapModel inAdd) {
		this.inAdd = inAdd;
	}



	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ShowJabrefPreferences.getKey())) {
			ShowJabrefPreferences.actionPerformed(e);
		}		
	}
	
	private class KeyBindInterceptor implements IKeyStrokeInterceptor {
		
		public boolean interceptKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
			Object source = e.getSource();
			if(hasPackageNameOrAncestor(source, "net.sf.jabref")) {
				if(jabrefWrapper.getJabrefFrame().getMenuBar().processKeyBinding(ks, e, condition, pressed)) {
					e.consume();
					
				}
				return true;
			}
			return false;
		}
		
		private boolean hasPackageNameOrAncestor(Object obj, String packageName) {
			if(obj == null || packageName == null) {
				return false;
			}
			String str = obj.getClass().getPackage().getName();
			if(str.startsWith(packageName)) {
				return true;
			} 
			else {
				if(obj instanceof Component) {
					return hasPackageNameOrAncestor(((Component) obj).getParent(), packageName);
				}
			}
			return false;
		}
	}

}
