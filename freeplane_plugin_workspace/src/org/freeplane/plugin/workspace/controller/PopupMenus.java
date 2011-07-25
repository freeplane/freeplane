package org.freeplane.plugin.workspace.controller;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceEnvironment;
import org.freeplane.plugin.workspace.WorkspacePreferences;

public class PopupMenus {
	public static final String WORKSPACE_POPUP_MENU = "/workspace_popup";
	
	private JPopupMenu workspacePopupMenu;
	
//	public static JPopupMenu getWorkspacePopupMenu() {
//		if (workspacePopupMenu == null) {
//			loadWorkspacePopupMenu();
//		}
//		return workspacePopupMenu;
//	}
	
	public PopupMenus() {
		addMenuEntries();
		
	}
	
	private void addMenuEntries() {
		MenuBuilder menuBuilder = Controller.getCurrentModeController().getUserInputListenerFactory().getMenuBuilder();
		workspacePopupMenu = new JPopupMenu("workspace");
		menuBuilder.addPopupMenu(workspacePopupMenu, WORKSPACE_POPUP_MENU);
		
		Controller.getCurrentModeController().addMenuContributor(new IMenuContributor() {
			ResourceController resourceController = Controller.getCurrentController().getResourceController();

			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				builder.addCheckboxItem(WORKSPACE_POPUP_MENU, new CheckBoxAction(WorkspacePreferences.SHOW_WORKSPACE_TEXT, WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY),
						resourceController.getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY));				
			}
		});
	}
	
	private class CheckBoxAction extends AFreeplaneAction {

		private static final long serialVersionUID = 1L;
		private String propertyKey;

		public CheckBoxAction(String key, String propertyKey) {
			super(key);
			this.propertyKey = propertyKey;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean checked = ((JCheckBoxMenuItem) e.getSource()).isSelected();
			Controller.getCurrentController().getResourceController().setProperty(this.propertyKey, checked);

			if (checked) {
				WorkspaceEnvironment.getCurrentWorkspaceEnvironment().showWorkspaceView(true);
			}
			else {
				WorkspaceEnvironment.getCurrentWorkspaceEnvironment().showWorkspaceView(false);
			}
		}
	}

	public JPopupMenu getWorkspacePopupMenu() {
		
		return workspacePopupMenu;
	}
	
//	private static void loadWorkspacePopupMenu() {
//		Controller controller = Controller.getCurrentController();
//		final Set<String> plugins = Collections.emptySet();
//		
//		MenuBuilder menuBuilder = Controller.getCurrentModeController().getUserInputListenerFactory().getMenuBuilder();
//		
//		final URL menuStructure = PopupMenus.class.getResource("/xml/workspace_popups.xml");
//		System.out.println("FISH menuStructure.getFile(): "+menuStructure.getFile());
//		if (menuStructure != null) {
//			menuBuilder.processMenuCategory(menuStructure, plugins);
//		}
//		
//        workspacePopupMenu = new JPopupMenu();
//        
//        workspacePopupMenu.add(new JMenuItem());
//        menuBuilder.addPopupMenu(workspacePopupMenu, WORKSPACE_POPUP_MENU);
        
        //final ViewController viewController = Controller.getCurrentController().getViewController();
        //viewController.updateMenus(menuBuilder);
        
        
        
        
        
        //popup = Controller.getCurrentModeController().getUserInputListenerFactory().getMapPopup();
        
		
//		
//		Controller controller = Controller.getCurrentController();
//		final Set<String> plugins = new TreeSet<String>();
//		plugins.add("org.freeplane.plugin.workspace");
//		
//		controller.getModeController(MModeController.MODENAME).updateMenus("/workspace_popups.xml", plugins);
//		
//		workspacePopupMenu = new JPopupMenu();
//		MenuBuilder menuBuilder = Controller.getCurrentModeController().getUserInputListenerFactory().getMenuBuilder();
//		menuBuilder.addPopupMenu(workspacePopupMenu, FreeplaneMenuBar.MAP_POPUP_MENU);
//		final ViewController viewController = Controller.getCurrentController().getViewController();
//		viewController.updateMenus(menuBuilder);
//	}

//	@Override
//	public void updateMenus(ModeController modeController, MenuBuilder builder) {
//		// TODO Auto-generated method stub
//		
//	}
}
