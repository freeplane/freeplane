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
import org.freeplane.plugin.workspace.config.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.config.node.WorkspaceRoot;
import org.freeplane.plugin.workspace.controller.AWorkspaceExpansionStateHandler;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceComponentHandler;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceDropHandler;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceExpansionStateHandler;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceKeyHandler;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceMouseHandler;
import org.freeplane.plugin.workspace.controller.IWorkspaceListener;
import org.freeplane.plugin.workspace.controller.WorkspaceEvent;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferHandler;
import org.freeplane.plugin.workspace.io.FileReadManager;
import org.freeplane.plugin.workspace.io.FileSystemAlterationMonitor;
import org.freeplane.plugin.workspace.io.FilesystemReader;
import org.freeplane.plugin.workspace.io.creator.AFileNodeCreator;
import org.freeplane.plugin.workspace.model.WorkspaceIndexedTreeModel;
import org.freeplane.plugin.workspace.view.TreeView;
import org.freeplane.plugin.workspace.view.WorkspaceSplitPaneUI;

public class WorkspaceController implements IFreeplanePropertyListener, IMapLifeCycleListener, ActionListener {
	public static final String WORKSPACE_RESOURCE_URL_PROTOCOL = "workspace";
	public static final String PROPERTY_RESOURCE_URL_PROTOCOL = "property";
	public static final String WORKSPACE_VERSION = "1.0";
		
	private static final WorkspaceController workspaceController = new WorkspaceController();
	private static final WorkspaceConfiguration configuration = new WorkspaceConfiguration();
	private static final FileSystemAlterationMonitor monitor = new FileSystemAlterationMonitor(5000);

	private final FilesystemReader fsReader;
	private final Vector<IWorkspaceListener> workspaceListener = new Vector<IWorkspaceListener>();

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

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	protected WorkspaceController() {
		LogUtils.info("Initializing WorkspaceEnvironment");
		registerToIMapLifeCycleListener();
		getPreferences();
		initTree();

		// this.popups = new PopupMenus();

		this.fsReader = new FilesystemReader(getFileTypeManager());
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	private void registerToIMapLifeCycleListener() {
		Controller.getCurrentModeController().getMapController().addMapLifeCycleListener(this);
	}
	
	public void initialStart() {
		if (getPreferences().getWorkspaceLocation() == null) {
			WorkspaceChooserDialog dialog = new WorkspaceChooserDialog();
			dialog.setVisible(true);
			if (getPreferences().getWorkspaceLocation() != null) {
				ResourceController.getResourceController().setProperty(WorkspacePreferences.LINK_PROPERTY_KEY,
						WorkspacePreferences.RELATIVE_TO_WORKSPACE);
			}
		}
		initializeConfiguration();
		initializeView();
		isInitialized = true;
	}

	public static WorkspaceController getController() {
		return workspaceController;
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

	public FilesystemReader getFilesystemReader() {
		return this.fsReader;
	}

	public void saveConfigurationAsXML(Writer writer) {
		getConfiguration().saveConfiguration(writer);
	}

	public WorkspaceTransferHandler getTransferHandler() {
		return transferHandler;
	}

	public void removeWorkspaceListener(IWorkspaceListener listener) {
		workspaceListener.remove(listener);
	}

	public void addWorkspaceListener(IWorkspaceListener listener) {
		this.workspaceListener.add(listener);
	}

	public void removeAllListeners() {
		this.workspaceListener.removeAllElements();
	}

	public void reloadWorkspace() {
		initTree();
		initializeConfiguration();		
		reloadView();
		getExpansionStateHandler().restoreExpansionStates();
	}

	public void refreshWorkspace() {
		getWorkspaceModel().reload();
		getExpansionStateHandler().restoreExpansionStates();
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
		String workspaceLocation = getPreferences().getWorkspaceLocation();
		if (workspaceLocation == null || workspaceLocation.trim().length() <= 0) {
			showWorkspace(false);
			return;
		}

		resetWorkspaceView();

		if (getConfiguration().reload()) {
			showWorkspace(true);
			UrlManager.getController().setLastCurrentDir(new File(preferences.getWorkspaceLocation()));
			dispatchWorkspaceEvent(new WorkspaceEvent(WorkspaceEvent.WORKSPACE_EVENT_TYPE_CHANGE, getConfiguration()));
		}
		else {
			showWorkspace(false);
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
			this.transferHandler = WorkspaceTransferHandler.configureDragAndDrop(this.view.getTreeView(),
					new DefaultWorkspaceDropHandler());
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
		getWorkspaceView().setVisible(width > 0);
		getWorkspaceView().setSize(width, 0);
		splitPane.setDividerLocation(width + 1);
//		if(!Controller.getCurrentController().getResourceController().getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY)) {
			splitPane.setDividerSize((width > 0 ? 7 : 0));
			splitPane.setEnabled(width > 0);
//		}
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
		showWorkspace(Controller.getCurrentController().getResourceController()
				.getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY));
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
				props.load(this.getClass().getResourceAsStream("filenodetypes.properties"));

				Class<?>[] args = {};
				for (Object key : props.keySet()) {
					try {
						// TODO: IMPLEMENT WITH REFLECTIONS - HAS TO WORK WITH
						// JAR FILES
						Class<?> clazz = org.freeplane.plugin.workspace.io.creator.DefaultFileNodeCreator.class;

						if (key.toString().equals("org.freeplane.plugin.workspace.io.creator.FolderFileNodeCreator")) {
							clazz = org.freeplane.plugin.workspace.io.creator.FolderFileNodeCreator.class;
						}
						else if (key.toString().equals("org.freeplane.plugin.workspace.io.creator.ImageFileNodeCreator")) {
							clazz = org.freeplane.plugin.workspace.io.creator.ImageFileNodeCreator.class;
						}
						else if (key.toString().equals("org.freeplane.plugin.workspace.io.creator.MindMapFileNodeCreator")) {
							clazz = org.freeplane.plugin.workspace.io.creator.MindMapFileNodeCreator.class;
						}

						// Class<?> clazz =
						// Thread.currentThread().getContextClassLoader().loadClass(key.toString());

						AFileNodeCreator handler = (AFileNodeCreator) clazz.getConstructor(args).newInstance();
						handler.setFileTypeList(props.getProperty(key.toString(), ""), "\\|");
						this.fileTypeManager.addFileHandler(handler);
					}
					// catch (ClassNotFoundException e) {
					// e.printStackTrace();
					// System.out.println("Class not found [" + key + "]");
					// }
					catch (ClassCastException e) {
						System.out.println("Class [" + key + "] is not of type: PhysicalNode");
					}
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return this.fileTypeManager;
	}

	private void dispatchWorkspaceEvent(WorkspaceEvent event) {
		switch (event.getType()) {
			case (WorkspaceEvent.WORKSPACE_EVENT_TYPE_CHANGE): {
				for (IWorkspaceListener listener : workspaceListener) {
					listener.workspaceChanged(event);
				}
				break;
			}
			case (WorkspaceEvent.WORKSPACE_EVENT_TYPE_TOOLBAR_EVENT): {
				for (IWorkspaceListener listener : workspaceListener) {
					listener.workspaceChanged(event);
				}
				break;
			}

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
		System.out.println(e);		
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
