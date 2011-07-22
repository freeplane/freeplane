package org.freeplane.plugin.workspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.plugin.workspace.config.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.FileReadManager;
import org.freeplane.plugin.workspace.io.FilesystemReader;
import org.freeplane.plugin.workspace.io.creator.DefaultFileNodeCreator;
import org.freeplane.plugin.workspace.io.creator.ImageFileNodeCreator;
import org.freeplane.plugin.workspace.view.TreeView;

public class WorkspaceEnvironment implements ComponentListener, MouseListener, IFreeplanePropertyListener {
	private WorkspaceConfiguration config;
	private static WorkspaceEnvironment currentWorkspace;
	private TreeView view;
	private Container oldContentPane;
	private Container WSContentPane;
	private SingleContentPane contentPane;
	private WorkspacePreferences preferences;
	private final FilesystemReader fsReader;
	private FileReadManager fileTypeManager;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceEnvironment() {
		LogUtils.info("Initializing WorkspaceEnvironment");
		initializeConfiguration();
		initializeView();
		initializePreferences();
		this.fsReader = new FilesystemReader(getFileTypeManager());
		currentWorkspace = this;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static WorkspaceEnvironment getCurrentWorkspaceEnvironment() {
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

	private void initializeConfiguration() {
		resetWorkspaceView();

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

	private Container getWorkspaceView() {
		if (this.view == null) {
			this.view = new TreeView(getConfig().getConfigurationRoot());
			this.view.addComponentListener(this);
			this.view.addTreeMouseListener(this);
		}
		return this.view;
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
				.getBooleanProperty(WorkspacePreferences.RESOURCE_SHOW_WORKSPACE));
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

	public void showWorkspaceView(boolean visible) {
		ResourceController resCtrl = Controller.getCurrentController().getResourceController();
		resCtrl.setProperty(WorkspacePreferences.RESOURCE_SHOW_WORKSPACE, visible);
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
	
	private FileReadManager getFileTypeManager() {
		if(this.fileTypeManager == null) {
			this.fileTypeManager = new FileReadManager();
			this.fileTypeManager.addFileHandler("*", new DefaultFileNodeCreator(getConfig().getTree()));
			this.fileTypeManager.addFileHandler(".gif", new ImageFileNodeCreator(getConfig().getTree()));
			this.fileTypeManager.addFileHandler(".jpg", new ImageFileNodeCreator(getConfig().getTree()));
		}
		return this.fileTypeManager;
	}

	private boolean canHandleEvent(Object object) {
		Class<?>[] interfaces = object.getClass().getInterfaces();
		for (Class<?> interf : interfaces) {
			if (IWorkspaceNodeEventListener.class.getName().equals(interf.getName())) {
				return true;
			}
		}
		return false;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		if (propertyName.equals(WorkspacePreferences.WORKSPACE_LOCATION_NEW)) {
			if (newValue != null && !newValue.isEmpty()) {
				Controller.getCurrentController().getResourceController()
						.setProperty(WorkspacePreferences.RESOURCE_SHOW_WORKSPACE, true);
				initializeConfiguration();
				reloadView();

			}
		}
	}

	public void componentResized(ComponentEvent e) {
		if (Controller.getCurrentController().getResourceController()
				.getBooleanProperty(WorkspacePreferences.RESOURCE_SHOW_WORKSPACE)
				&& e.getComponent() == getWorkspaceView()) {
			System.out.println("change width: " + e.getComponent().getWidth());
			ResourceController resCtrl = Controller.getCurrentController().getResourceController();
			resCtrl.setProperty(WorkspacePreferences.WORKSPACE_WIDTH_PROPERTY_KEY, String.valueOf(e.getComponent().getWidth()));
		}
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
		TreePath path = ((JTree) e.getComponent()).getPathForLocation(e.getX(), e.getY());
		if (path != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

			if (canHandleEvent(node.getUserObject())) {
				int eventType = 0;
				if (e.getButton() == MouseEvent.BUTTON1) {
					eventType += WorkspaceNodeEvent.MOUSE_LEFT;
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					eventType += WorkspaceNodeEvent.MOUSE_RIGHT;
				}
				if (e.getClickCount() % 2 == 0) {
					eventType += WorkspaceNodeEvent.MOUSE_DBLCLICK;
				}
				else {
					eventType += WorkspaceNodeEvent.MOUSE_CLICK;
				}
				((IWorkspaceNodeEventListener) node.getUserObject()).handleEvent(new WorkspaceNodeEvent(node, eventType,
						e.getX(), e.getY()));
			}

		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {
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
