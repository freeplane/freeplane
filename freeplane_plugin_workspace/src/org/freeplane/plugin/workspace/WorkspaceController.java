package org.freeplane.plugin.workspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.plugin.workspace.config.PopupMenus;
import org.freeplane.plugin.workspace.config.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceComponentHandler;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceDropHandler;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceKeyHandler;
import org.freeplane.plugin.workspace.controller.DefaultWorkspaceMouseHandler;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferHandler;
import org.freeplane.plugin.workspace.io.FileReadManager;
import org.freeplane.plugin.workspace.io.FilesystemReader;
import org.freeplane.plugin.workspace.io.creator.AFileNodeCreator;
import org.freeplane.plugin.workspace.io.xml.ConfigurationWriter;
import org.freeplane.plugin.workspace.view.TreeView;

public class WorkspaceController implements IFreeplanePropertyListener {
	public static final String WORKSPACE_RESOURCE_URL_PROTOCOL = "workspace";
	public static final String PROPERTY_RESOURCE_URL_PROTOCOL = "property";
	private WorkspaceConfiguration config;
	private static WorkspaceController currentWorkspace;
	private TreeView view;
	private Container oldContentPane;
	private Container WSContentPane;
	private SingleContentPane contentPane;
	private WorkspacePreferences preferences;
	private final FilesystemReader fsReader;
	private FileReadManager fileTypeManager;
	private ConfigurationWriter configWriter;
	private final PopupMenus popups;
	private WorkspaceTransferHandler transferHandler;
	private IndexedTree tree;	
	
	private String workspaceLocation;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceController() {
		
		currentWorkspace = this;
		LogUtils.info("Initializing WorkspaceEnvironment");	
		initTree();
		initializePreferences();
		
		this.popups = new PopupMenus();
		
		initializeConfiguration();
		initializeView();		
		this.fsReader = new FilesystemReader(getFileTypeManager());
		this.configWriter = new ConfigurationWriter(this);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static WorkspaceController getCurrentWorkspaceController() {
		return currentWorkspace;
	}

	public WorkspaceConfiguration getConfig() {
		if (this.config != null) {
			return this.config;
		}
		else
			setConfig(new WorkspaceConfiguration());
		return config;
	}

	public void setConfig(WorkspaceConfiguration config) {
		this.config = config;
	}

	public String getWorkspaceLocation() {
		if (this.workspaceLocation == null) {
			this.workspaceLocation = ResourceController.getResourceController().getProperty(WorkspacePreferences.WORKSPACE_LOCATION);
		}
		return workspaceLocation;
	}

	public void setWorkspaceLocation(String workspaceLocation) {
		this.workspaceLocation = workspaceLocation;		
		ResourceController.getResourceController().setProperty(WorkspacePreferences.WORKSPACE_LOCATION_NEW,
				workspaceLocation);
		Controller.getCurrentController().getResourceController()
				.setProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY, true);
		WorkspaceController.getCurrentWorkspaceController().refreshWorkspace();
			
	}

	public TreeView getWorkspaceView() {
		if (this.view == null) {
			this.view = new TreeView(getWorkspaceRoot());
			this.view.addComponentListener(new DefaultWorkspaceComponentHandler());
			this.view.getTree().addMouseListener(new DefaultWorkspaceMouseHandler());
			this.view.getTree().addKeyListener(new DefaultWorkspaceKeyHandler());
			this.transferHandler = WorkspaceTransferHandler.configureDragAndDrop(this.view.getTree(), new DefaultWorkspaceDropHandler());
		}
		return this.view;
	}
	
	public DefaultMutableTreeNode getWorkspaceRoot() {
		return getTree().getRoot();
	}

	public JTree getWorspaceTree() {
		return this.getWorkspaceView().getTree();
	}
	
	public void showWorkspaceView(boolean visible) {
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

	public DefaultTreeModel getViewModel() {
		return this.view.getTreeModel();
	}

	public FilesystemReader getFilesystemReader() {
		return this.fsReader;
	}

	public void saveConfigurationAsXML(Writer writer) {
		try {
			this.configWriter.writeConfigurationAsXml(writer);
		}
		catch (final IOException e) {
			LogUtils.severe(e);
		}
	}

	public PopupMenus getPopups() {
		return popups;
	}

	public WorkspaceTransferHandler getTransferHandler() {
		return transferHandler;
	}

	public void refreshWorkspace() {
		initTree();
		initializeConfiguration();		
		reloadView();
	}

	public IndexedTree getTree() {
		return tree;
	}

	
	private void initTree() {
		this.tree = new IndexedTree(null);
	}
	
	private void initializeConfiguration() {
		resetWorkspaceView();
		setConfig(new WorkspaceConfiguration());
		if (!getConfig().isConfigValid()) {
			LocationDialog locationDialog = new LocationDialog();
			locationDialog.setVisible(true);
		}
			
		if (!getConfig().isConfigValid()) {			
			showWorkspaceView(false);
			return;
		}

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
			JSplitPane splitPane = new JSplitPane();
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
		splitPane.setDividerSize((width > 0 ? 4 : 0));
		splitPane.setEnabled(width > 0);

	}	
	
	private void resetWorkspaceView() {
		this.config = null;
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
		showWorkspaceView(Controller.getCurrentController().getResourceController()
				.getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY));
	}

	private SingleContentPane getContentPane() {
		if (this.contentPane == null) {
			this.contentPane = new SingleContentPane();
			this.contentPane.setLayout(new BorderLayout());
		}
		return this.contentPane;
	}

	private WorkspacePreferences initializePreferences() {
		if (this.preferences == null) {
			this.preferences = new WorkspacePreferences();
			ResourceController resCtrl = Controller.getCurrentController().getResourceController();
			resCtrl.addPropertyChangeListener(this);
		}
		return this.preferences;
	}

	private FileReadManager getFileTypeManager() {
		if (this.fileTypeManager == null) {
			this.fileTypeManager = new FileReadManager();
			Properties props = new Properties();
			try {
				props.load(this.getClass().getResourceAsStream("filenodetypes.properties"));
				
				Class<?>[] args = { };
				for (Object key : props.keySet()) {
					try {
						//TODO: IMPLEMENT WITH REFLECTIONS - HAS TO WORK WITH JAR FILES
						Class<?> clazz =  org.freeplane.plugin.workspace.io.creator.DefaultFileNodeCreator.class;
						
						if (key.toString().equals("org.freeplane.plugin.workspace.io.creator.FolderFileNodeCreator")) {
							clazz = org.freeplane.plugin.workspace.io.creator.FolderFileNodeCreator.class;
						}
						else if (key.toString().equals("org.freeplane.plugin.workspace.io.creator.ImageFileNodeCreator")) {
							clazz = org.freeplane.plugin.workspace.io.creator.ImageFileNodeCreator.class;
						}
						else if (key.toString().equals("org.freeplane.plugin.workspace.io.creator.MindMapFileNodeCreator")) {
							clazz = org.freeplane.plugin.workspace.io.creator.MindMapFileNodeCreator.class;
						}
							
//						Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(key.toString());
						
						AFileNodeCreator handler = (AFileNodeCreator) clazz.getConstructor(args).newInstance();
						handler.setFileTypeList(props.getProperty(key.toString(), ""), "\\|");
						this.fileTypeManager.addFileHandler(handler);
					}
//					catch (ClassNotFoundException e) {
//						e.printStackTrace();
//						System.out.println("Class not found [" + key + "]");
//					}
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

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void propertyChanged(String propertyName, String newValue, String oldValue) {		
		if (propertyName.equals(WorkspacePreferences.WORKSPACE_LOCATION_NEW)) {
			if (newValue != null && newValue.trim().length()>0) {
				Controller.getCurrentController().getResourceController()
						.setProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY, true);
				refreshWorkspace();
			}
		}
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
	

}
