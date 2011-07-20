package org.freeplane.plugin.workspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.plugin.workspace.config.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.view.TreeView;

public class WorkspaceEnvironment implements ComponentListener, MouseListener {
	private static String SHOW_WORKSPACE_PROPERTY_KEY = "show_workspace";
	private static String WORKSPACE_WIDTH_PROPERTY_KEY = "workspace_view_width";
	
	private class SingleContentPane extends JPanel {

		private static final long serialVersionUID = 1L;

		public void setComponent(Component comp) {
			this.removeAll();
			this.add(comp);
		}
	}
	
	private WorkspaceConfiguration config;
	private static WorkspaceEnvironment currentWorkspace;
	private TreeView view;
	private Container oldContentPane;
	private Container WSContentPane;
	private SingleContentPane contentPane;
	private WorkspacePreferences preferences;


	public WorkspaceEnvironment() {
		
		LogUtils.info("Initializing WorkspaceEnvironment");
		initializeConfiguration();
		initView();
		currentWorkspace = this;
		this.preferences = new WorkspacePreferences();
	}
	
	public static WorkspaceEnvironment getCurrentWorkspaceEnvironment() {
		return currentWorkspace;
	}
	
	public WorkspaceConfiguration getConfig() {
		return config;
	}

	public void setConfig(WorkspaceConfiguration config) {
		this.config = config;
	}
	
	private void initializeConfiguration() {
		final URL configURL = this.getClass().getResource("workspace_default.xml");
		setConfig(new WorkspaceConfiguration(configURL));
		
	}
	
	private Container getOldContentPane() {
		if(this.oldContentPane == null) {
			MModeController modeController = (MModeController)Controller.getCurrentModeController();
			this.oldContentPane = modeController.getController().getViewController().getJFrame().getContentPane();
		}
		return this.oldContentPane;
	}
	
	private Container getWSContentPane() {
		if(this.WSContentPane == null) {
			ResourceController resCtrl = Controller.getCurrentController().getResourceController();
			JSplitPane splitPane = new JSplitPane();
			splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			getWorkspaceView().setMinimumSize(new Dimension(resCtrl.getIntProperty(WORKSPACE_WIDTH_PROPERTY_KEY, 200), 1));
			splitPane.setLeftComponent(getWorkspaceView());
			splitPane.setRightComponent(getOldContentPane());
			this.WSContentPane = splitPane;
		}
		return this.WSContentPane;
	}
	
	private Container getWorkspaceView() {
		if(this.view == null) {
			this.view = new TreeView(getConfig().getConigurationRoot());		
			this.view.addComponentListener(this);
			this.view.addTreeMouseListener(this);
		}
		return this.view;
	}
	
	private void initView() {
		getOldContentPane();
		ViewController viewController = (ViewController)Controller.getCurrentController().getViewController();
		viewController.getJFrame().setContentPane(new JPanel(new BorderLayout()));
		viewController.getJFrame().getContentPane().add(getContentPane());
		showWorkspaceView(Controller.getCurrentController().getResourceController().getBooleanProperty(SHOW_WORKSPACE_PROPERTY_KEY));
	}
	
	private SingleContentPane getContentPane() {		
		if(this.contentPane == null) {
			this.contentPane = new SingleContentPane();
			this.contentPane.setLayout(new BorderLayout());
		}
		return this.contentPane;
	}
	
	public void showWorkspaceView(boolean visible) {
		ResourceController resCtrl = Controller.getCurrentController().getResourceController();
		
		resCtrl.setProperty(SHOW_WORKSPACE_PROPERTY_KEY, visible);
		if(visible) {
			getContentPane().setComponent(getWSContentPane());
			getContentPane().revalidate();
		}
		else {
			getContentPane().setComponent(getOldContentPane());
			getContentPane().revalidate();
		}
	}
	
	public DefaultTreeModel getViewModel() {
		return this.view.getTreeModel();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if(e.getComponent() == getWorkspaceView()) {
			ResourceController resCtrl = Controller.getCurrentController().getResourceController();
			resCtrl.setProperty(WORKSPACE_WIDTH_PROPERTY_KEY, String.valueOf(e.getComponent().getWidth()));
		}
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {	
	}

	private boolean canHandleEvent(Object object) {
		Class<?>[] interfaces = object.getClass().getInterfaces();
    	for(Class<?> interf : interfaces) {
    		if(WorkspaceNodeEventListener.class.getName().equals(interf.getName())) {
    			return true;
    		}
    	}
		return false;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		TreePath path = ((JTree)e.getComponent()).getPathForLocation(e.getX(), e.getY());
		if(path != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
						
			if(canHandleEvent(node.getUserObject())) {
				int eventType = 0;
				if(e.getButton()==MouseEvent.BUTTON1) {
					eventType += WorkspaceNodeEvent.MOUSE_LEFT;			
				}
				if (e.getButton()==MouseEvent.BUTTON3) {
					eventType += WorkspaceNodeEvent.MOUSE_RIGHT;		
				}
				if(e.getClickCount()%2==0) {
					eventType += WorkspaceNodeEvent.MOUSE_DBLCLICK;
				} 
				else {
					eventType += WorkspaceNodeEvent.MOUSE_CLICK;
				}
				((WorkspaceNodeEventListener)node.getUserObject()).handleEvent(new WorkspaceNodeEvent(node, eventType, e.getX(), e.getY()));
			}
			
		}
	}

	

	@Override
	public void mousePressed(MouseEvent e) {		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {		
	}
		
}
