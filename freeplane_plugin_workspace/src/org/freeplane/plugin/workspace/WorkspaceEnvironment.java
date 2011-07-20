package org.freeplane.plugin.workspace;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.URL;

import javax.swing.JSplitPane;
import javax.swing.tree.DefaultTreeModel;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.config.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.view.TreeView;

public class WorkspaceEnvironment implements ComponentListener {
	private WorkspaceConfiguration config;
	private static WorkspaceEnvironment currentWorkspace;
	private TreeView view;

	public WorkspaceEnvironment() {
		LogUtils.info("Initializing WorkspaceEnvironment");
		initializeConfiguration();
		prepareModel();
		hookIntoView();
		currentWorkspace = this;
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
	
	private void prepareModel() {
		
	}
	
	private void hookIntoView() {
		MModeController modeController = (MModeController) Controller.getCurrentModeController();
		
		for(Component comp : Controller.getCurrentController().getViewController().getContentPane().getComponents()) {
			System.out.println(comp);
		}
		// Code for adding new Panels to Freeplane		  
		Component[] components = modeController.getController().getViewController().getJFrame().getContentPane().getComponents();
		  
		modeController.getController().getViewController().getJFrame().getContentPane().removeAll();
		JSplitPane splitPane = new JSplitPane();		
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		this.view = new TreeView(this.getConfig().getConigurationRoot());
		splitPane.setLeftComponent(this.view);
		this.view.addComponentListener(this);
		for(Component component : components){
			splitPane.setRightComponent(component);
		}  
		modeController.getController().getViewController().getJFrame().getContentPane().add(splitPane);
	}
	
	public DefaultTreeModel getViewModel() {
		return this.view.getTreeModel();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		System.out.println("Workspace: "+ e.getComponent().getWidth());		
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
		
}
