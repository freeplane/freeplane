package org.freeplane.plugin.workspace;

import java.awt.Component;
import java.net.URL;

import javax.swing.JSplitPane;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.config.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.view.DirTree;

public class WorkspaceEnvironment {
	private WorkspaceConfiguration config;
	
	

	public WorkspaceEnvironment() {
		LogUtils.info("Initializing WorkspaceEnvironment");
		initializeConfiguration();
		prepareModel();
		hookIntoView();	
	}
	
	public WorkspaceConfiguration getConfig() {
		return config;
	}

	public void setConfig(WorkspaceConfiguration config) {
		this.config = config;
	}
	
	private void initializeConfiguration() {
		final URL configURL = this.getClass().getResource("preferences.xml");
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
		 splitPane.setLeftComponent(new DirTree());
		 for(Component component : components){
		   splitPane.setRightComponent(component);
		 }  
		 modeController.getController().getViewController().getJFrame().getContentPane().add(splitPane);
		 //*/
	}
		
}
