package org.freeplane.plugin.workspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.main.application.ApplicationResourceController;
import org.freeplane.plugin.workspace.components.TreeView;
import org.freeplane.plugin.workspace.components.WorkspaceSplitPaneUI;
import org.freeplane.plugin.workspace.controller.AWorkspaceExpansionStateHandler;
import org.freeplane.plugin.workspace.controller.DefaultNodeTypeIconManager;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceComponentHandler;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceExpansionStateHandler;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceKeyHandler;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceMouseHandler;
import org.freeplane.plugin.workspace.controller.INodeTypeIconManager;
import org.freeplane.plugin.workspace.controller.IOController;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferHandler;
import org.freeplane.plugin.workspace.event.IWorkspaceEventListener;
import org.freeplane.plugin.workspace.event.WorkspaceEvent;
import org.freeplane.plugin.workspace.io.AFileNodeCreator;
import org.freeplane.plugin.workspace.io.FileReadManager;
import org.freeplane.plugin.workspace.io.FileSystemAlterationMonitor;
import org.freeplane.plugin.workspace.io.FilesystemManager;
import org.freeplane.plugin.workspace.model.WorkspaceIndexedTreeModel;
import org.freeplane.plugin.workspace.nodes.WorkspaceRoot;

public class WorkspaceController implements IFreeplanePropertyListener, IMapLifeCycleListener, ActionListener {
	public static final String WORKSPACE_RESOURCE_URL_PROTOCOL = "workspace";
	public static final String PROPERTY_RESOURCE_URL_PROTOCOL = "property";
	public static final String WORKSPACE_VERSION = "1.0";
		
	private static final WorkspaceController workspaceController = new WorkspaceController();
	private static final IOController workspaceIOController = new IOController();
	private static final WorkspaceConfiguration configuration = new WorkspaceConfiguration();
	private static final FileSystemAlterationMonitor monitor = new FileSystemAlterationMonitor(30000);

	private final FilesystemManager fsReader;
	private final Vector<IWorkspaceEventListener> workspaceListener = new Vector<IWorkspaceEventListener>();

	private TreeView view;
	private Container oldContentPane;
	private Container WSContentPane;
	private SingleContentPane contentPane;
	private WorkspacePreferences preferences;

	private FileReadManager fileTypeManager;

	private WorkspaceTransferHandler transferHandler;
	private WorkspaceIndexedTreeModel model = new WorkspaceIndexedTreeModel();
	
	String workspaceLocation;
	private boolean isInitialized = false;
	private DefaultWorkspaceExpansionStateHandler expansionStateHandler;
	private INodeTypeIconManager nodeTypeIconManager;
	protected static final boolean firstApplicationStart;
	static {
		final File userPreferencesFile = ApplicationResourceController.getUserPreferencesFile();
		firstApplicationStart = !userPreferencesFile.exists();
	}

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	protected WorkspaceController() {
		LogUtils.info("Initializing WorkspaceEnvironment");
		registerToIMapLifeCycleListener();
		getPreferences();
		initTree();
		this.fsReader = new FilesystemManager(getFileTypeManager());
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	private void registerToIMapLifeCycleListener() {
		Controller.getCurrentModeController().getMapController().addMapLifeCycleListener(this);
	}
	
	public static boolean isFirstApplicationStart() {
		return firstApplicationStart;
	}
	
	public void initialStart() {

		
		//initializeConfiguration();
		initializeView();
		isInitialized = true;		
	}

	public static WorkspaceController getController() {
		return workspaceController;
	}
	
	public static IOController getIOController() {
		return workspaceIOController;
	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public WorkspaceConfiguration getConfiguration() {
		return configuration;
	}

	public FileSystemAlterationMonitor getFileSystemAlterationMonitor() {
		return monitor;
	}

	public INodeTypeIconManager getNodeTypeIconManager() {
		if(nodeTypeIconManager == null) {
			nodeTypeIconManager = new DefaultNodeTypeIconManager();
		}
		return nodeTypeIconManager;
	}
	public JTree getWorkspaceViewTree() {
		return getWorkspaceView().getTreeView();
	}

	public void showWorkspace(boolean visible) {
		ResourceController resCtrl = Controller.getCurrentController().getResourceController();
		resCtrl.setProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY, visible);
		if (visible) {
			int width = resCtrl.getIntProperty(WorkspacePreferences.WORKSPACE_WIDTH_PROPERTY_KEY, 200);
			setWorkspaceWidth(width);
			getContentPane().revalidate();
		}
		else {
			setWorkspaceWidth(-1);
			getContentPane().revalidate();
		}
	}

	public FilesystemManager getFilesystemMgr() {
		return this.fsReader;
	}

	public void saveConfigurationAsXML(Writer writer) {
		getConfiguration().saveConfiguration(writer);
	}

	public WorkspaceTransferHandler getTransferHandler() {
		return transferHandler;
	}

	public void removeWorkspaceListener(IWorkspaceEventListener listener) {
		workspaceListener.remove(listener);
	}

	public void addWorkspaceListener(IWorkspaceEventListener listener) {
		this.workspaceListener.add(listener);
	}

	public void removeAllListeners() {
		this.workspaceListener.removeAllElements();
	}

	private boolean loadInProcess = false;
	public void loadWorkspace() {
		if(loadInProcess) {
			return;
		}
		loadInProcess = true;
		if (getPreferences().getWorkspaceLocation() == null) {
			WorkspaceUtils.showWorkspaceChooserDialog();
		}
		initTree();
		initializeConfiguration();
		reloadView();
		showWorkspace(Controller.getCurrentController().getResourceController().getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY));
		getExpansionStateHandler().restoreExpansionStates();
		fireWorkspaceReady(new WorkspaceEvent(null, getConfiguration()));
		loadInProcess = false;
	}

	public void refreshWorkspace() {
		getWorkspaceModel().reload();
	}
	
	public WorkspaceIndexedTreeModel getWorkspaceModel() {
		if(model == null) {
			model = new WorkspaceIndexedTreeModel();
		}
		return model;
	}

	public WorkspacePreferences getPreferences() {
		if (this.preferences == null) {
			this.preferences = new WorkspacePreferences();
			ResourceController resCtrl = Controller.getCurrentController().getResourceController();
			resCtrl.addPropertyChangeListener(this);
		}
		return this.preferences;
	}

	public AWorkspaceExpansionStateHandler getExpansionStateHandler() {
		if (expansionStateHandler == null) {
			expansionStateHandler = new DefaultWorkspaceExpansionStateHandler();
		}
		return expansionStateHandler;
	}

	private void initTree() {
		getWorkspaceModel().resetIndex();
		getWorkspaceModel().setRoot(new WorkspaceRoot());		
	}

	private void initializeConfiguration() {
		ResourceController resCtrl = Controller.getCurrentController().getResourceController();
		
		String workspaceLocation = getPreferences().getWorkspaceLocation();
		if (workspaceLocation == null || workspaceLocation.trim().length() <= 0) {
			resCtrl.setProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY, false);
			showWorkspace(false);
			return;
		}
		resetWorkspaceView();
		fireConfigurationBeforeLoading(new WorkspaceEvent(null, getConfiguration()));
		if (getConfiguration().load()) {
			fireConfigurationLoaded(new WorkspaceEvent(null, getConfiguration()));
			showWorkspace(Controller.getCurrentController().getResourceController()
					.getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY));
			UrlManager.getController().setLastCurrentDir(new File(preferences.getWorkspaceLocation()));			
			fireWorkspaceChanged(new WorkspaceEvent(WorkspaceEvent.WORKSPACE_CHANGED, getConfiguration()));
		}
		else {
			showWorkspace(Controller.getCurrentController().getResourceController()
					.getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY));
			getPreferences().setNewWorkspaceLocation(null);
		}
	}

	private TreeView getWorkspaceView() {
		if (this.view == null) {
			this.view = new TreeView();
			this.view.getTreeView().setModel(getWorkspaceModel());
			this.view.addComponentListener(new DefaultWorkspaceComponentHandler(this.view));
			DefaultWorkspaceMouseHandler mouseHandler = new DefaultWorkspaceMouseHandler();
			this.view.getTreeView().addMouseListener(mouseHandler);
			this.view.getTreeView().addMouseMotionListener(mouseHandler);
			this.view.getTreeView().addKeyListener(new DefaultWorkspaceKeyHandler());
			this.view.getTreeView().setRowHeight(18);
			this.view.getTreeView().addTreeExpansionListener((DefaultWorkspaceExpansionStateHandler) getExpansionStateHandler());
			this.transferHandler = WorkspaceTransferHandler.configureDragAndDrop(this.view.getTreeView());
		}
		return this.view;
	}

	private Container getOldContentPane() {
		if (this.oldContentPane == null) {
			MModeController modeController = (MModeController) Controller.getCurrentModeController();
			this.oldContentPane = new JPanel(new BorderLayout());
			for (Component comp : modeController.getController().getViewController().getJFrame().getContentPane().getComponents()) {
				this.oldContentPane.add(comp);
			}

		}
		return this.oldContentPane;
	}
	
	private Container getWSContentPane() {
		if (this.WSContentPane == null) {
			this.WSContentPane = new JPanel(new BorderLayout());
			this.WSContentPane.setMinimumSize(new Dimension(0, 0));
			final JSplitPane splitPane = new JSplitPane();
			splitPane.setDividerSize(7);
			splitPane.setUI(new WorkspaceSplitPaneUI());
			splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);			
			splitPane.setAutoscrolls(true);
			splitPane.setLeftComponent(getWorkspaceView());
			splitPane.setRightComponent(getOldContentPane());
			this.WSContentPane.add(splitPane);
		}
		return this.WSContentPane;
	}
 
	private void setWorkspaceWidth(int width) {
		JSplitPane splitPane = (JSplitPane) (getWSContentPane().getComponent(0));
		if(width != -1) {
			getWorkspaceView().setVisible(true);
			getWorkspaceView().setSize(width, 0);
			splitPane.setDividerLocation(width);
			splitPane.setDividerSize(7);
			splitPane.setEnabled(true);
		} 
		else {
			getWorkspaceView().setVisible(false);
			splitPane.setEnabled(false);
			splitPane.setDividerSize(0);
		}
	}

	private void resetWorkspaceView() {
		this.view = null;
		this.WSContentPane = null;
	}

	private void initializeView() {
		getOldContentPane();
		ViewController viewController = (ViewController) Controller.getCurrentController().getViewController();
		viewController.getJFrame().setContentPane(new JPanel(new BorderLayout()));
		viewController.getJFrame().getContentPane().add(getContentPane());
		reloadView();
	}

	private void reloadView() {
		getContentPane().setComponent(getWSContentPane());		
	}

	private SingleContentPane getContentPane() {
		if (this.contentPane == null) {
			this.contentPane = new SingleContentPane();
			this.contentPane.setLayout(new BorderLayout());
		}
		return this.contentPane;
	}

	private FileReadManager getFileTypeManager() {
		if (this.fileTypeManager == null) {
			this.fileTypeManager = new FileReadManager();
			Properties props = new Properties();
			try {
				props.load(this.getClass().getResourceAsStream("/conf/filenodetypes.properties"));

				Class<?>[] args = {};
				for (Object key : props.keySet()) {
					try {
						Class<?> clazz = org.freeplane.plugin.workspace.creator.DefaultFileNodeCreator.class;
						
						clazz = this.getClass().getClassLoader().loadClass(key.toString());

						AFileNodeCreator handler = (AFileNodeCreator) clazz.getConstructor(args).newInstance();
						handler.setFileTypeList(props.getProperty(key.toString(), ""), "\\|");
						this.fileTypeManager.addFileHandler(handler);
					}
					catch (ClassNotFoundException e) {
						LogUtils.warn("Class not found [" + key + "]", e);
					}
					catch (ClassCastException e) {
						LogUtils.warn("Class [" + key + "] is not of type: PhysicalNode", e);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.fileTypeManager;
	}

//	protected void dispatchWorkspaceEvent(WorkspaceEvent event) {
//		for (IWorkspaceEventListener listener : workspaceListener) {
//			//listener.processEvent(event);
//		}
//	}
	
	protected void fireOpenWorkspace(WorkspaceEvent event) {
		for (IWorkspaceEventListener listener : workspaceListener) {
			listener.openWorkspace(event);
			//listener.processEvent(event);
		}
	}
	
	protected void fireCloseWorkspace(WorkspaceEvent event) {
		for (IWorkspaceEventListener listener : workspaceListener) {
			listener.closeWorkspace(event);
		}
	}
	
	protected void fireWorkspaceChanged(WorkspaceEvent event) {
		for (IWorkspaceEventListener listener : workspaceListener) {
			listener.workspaceChanged(event);
			//listener.processEvent(event);
		}
	}
	
	protected void fireWorkspaceReady(WorkspaceEvent event) {
		for (IWorkspaceEventListener listener : workspaceListener) {
			listener.workspaceReady(event);
		}
	}
	
	protected void fireConfigurationLoaded(WorkspaceEvent event) {
		for (IWorkspaceEventListener listener : workspaceListener) {
			listener.configurationLoaded(event);
		}
	}
	
	protected void fireConfigurationBeforeLoading(WorkspaceEvent event) {
		for (IWorkspaceEventListener listener : workspaceListener) {
			listener.configurationBeforeLoading(event);
		}
	}
	
	protected void fireToolBarChanged(WorkspaceEvent event) {
		for (IWorkspaceEventListener listener : workspaceListener) {
			listener.toolBarChanged(event);
		}
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		// if (propertyName.equals(WorkspacePreferences.WORKSPACE_LOCATION_NEW))
		// {
		// if (newValue != null && newValue.trim().length() > 0) {
		// Controller.getCurrentController().getResourceController()
		// .setProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY, true);
		// reloadWorkspace();
		// }
		// }
	}
	
	public void actionPerformed(ActionEvent e) {		
	}


	/***********************************************************************************
	 * INTERNAL CLASSES
	 **********************************************************************************/

	private class SingleContentPane extends JPanel {
		
		private static final long serialVersionUID = 1L;
		
		public void setComponent(Component comp) {
			this.removeAll();
			this.add(comp);
		}
	}

	public void onCreate(MapModel map) {
		
	}

	public void onRemove(MapModel map) {
		
	}

	public void onSavedAs(MapModel map) {
		refreshWorkspace();
	}

	public void onSaved(MapModel map) {
		
	}
}
