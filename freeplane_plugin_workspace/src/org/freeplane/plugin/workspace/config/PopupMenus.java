package org.freeplane.plugin.workspace.config;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspacePreferences;
import org.freeplane.plugin.workspace.config.actions.CollapseWorkspaceTree;
import org.freeplane.plugin.workspace.config.actions.ExpandWorkspaceTree;
import org.freeplane.plugin.workspace.config.actions.HideWorkspace;
import org.freeplane.plugin.workspace.config.actions.SetWorkspaceLocation;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

public class PopupMenus {

	private static final String WORKSPACE_POPUP_MENU_KEY = "/workspace_popup";
	private static final String WORKSPACE_NODE_POPUP_MENU_KEY = "/workspace_node_popup";
	private static final String WORKSPACE_POPUP_MENU_CONFIG = "/xml/popup_menus.xml";

	private JPopupMenu workspacePopupMenu;
	private JPopupMenu workspaceNodePopupMenu;
	private final MenuBuilder builder;
	private final HashMap<String, PopupObject> popupMap;
	
	public PopupMenus() {
		Controller.getCurrentModeController().addAction(new ExpandWorkspaceTree());
		Controller.getCurrentModeController().addAction(new CollapseWorkspaceTree());
		Controller.getCurrentModeController().addAction(new HideWorkspace());
		Controller.getCurrentModeController().addAction(new SetWorkspaceLocation());
		
		popupMap = new HashMap<String, PopupMenus.PopupObject>();
		
		builder = new MenuBuilder(Controller.getCurrentModeController());
		Set<String> emptySet = Collections.emptySet();
		
		//addMenuEntries();
		//builder.processMenuCategory(this.getClass().getResource(WORKSPACE_POPUP_MENU_CONFIG), emptySet);
		
	}
	
	public void registerPopupMenu(final String key) {
		if(!this.popupMap.containsKey(key)) {
			PopupObject popObj = new PopupObject(new JPopupMenu(), new MenuBuilder(Controller.getCurrentModeController()));
			
			this.popupMap.put(key, popObj);
			
			popObj.builder.addPopupMenu(popObj.popMenu, WORKSPACE_NODE_POPUP_MENU_KEY);
			
			final ControllerPopupMenuListener popupListener = new ControllerPopupMenuListener();
			popObj.popMenu.addHierarchyListener(popupListener);
			
			Set<String> emptySet = Collections.emptySet();
			popObj.builder.processMenuCategory(this.getClass().getResource(WORKSPACE_POPUP_MENU_CONFIG), emptySet);			
		}
	}
	
	public void addMenuEntry(final String popupKey, final String menuKey, AFreeplaneAction action) {
		PopupObject popObj = this.popupMap.get(popupKey);
		popObj.builder.addAction(menuKey, action, MenuBuilder.AS_CHILD);
	}
	
	private void addMenuEntries() {
		workspacePopupMenu = new JPopupMenu("");
		workspaceNodePopupMenu = new JPopupMenu("");
		
		builder.addPopupMenu(workspacePopupMenu, WORKSPACE_POPUP_MENU_KEY);
		builder.addPopupMenu(workspaceNodePopupMenu, WORKSPACE_NODE_POPUP_MENU_KEY);
		
		
		builder.addCheckboxItem(WORKSPACE_POPUP_MENU_KEY, new CheckBoxAction(WorkspacePreferences.SHOW_WORKSPACE_TEXT,
				WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY), Controller.getCurrentController().getResourceController()
				.getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY));

	}

	public class CheckBoxAction extends AFreeplaneAction {

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
				WorkspaceController.getCurrentWorkspaceController().showWorkspaceView(true);
			}
			else {
				WorkspaceController.getCurrentWorkspaceController().showWorkspaceView(false);
			}
		}
	}

	public JPopupMenu getWorkspacePopupMenu() {
		return workspacePopupMenu;
	}
	
	public JPopupMenu getWorkspaceNodePopupMenu() {
		return workspaceNodePopupMenu;
	}
		
	public void openPopup(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			Component component = (Component) event.getSource();			
			
			final ControllerPopupMenuListener popupListener = new ControllerPopupMenuListener();
			final JPopupMenu popupmenu = WorkspaceController.getCurrentWorkspaceController().getPopups().getWorkspacePopupMenu();
			if (popupmenu != null) {
                popupmenu.addHierarchyListener(popupListener);
                popupmenu.show(component, event.getX(), event.getY());            
            }
			
		}
		System.out.println("Event: " + event);
	}
	
	public void showPopup(String popupKey, Component component, int x, int y) {
			PopupObject popObj = this.popupMap.get(popupKey);
			final JPopupMenu popupmenu = popObj.popMenu;
			if (popupmenu != null) {                
                popupmenu.show(component, x, y);            
            }
	}
	
	private class PopupObject {
		public JPopupMenu popMenu;
		public MenuBuilder builder;
		
		public PopupObject(final JPopupMenu jPopupMenu, final MenuBuilder menuBuilder) {
			this.builder = menuBuilder;
			this.popMenu = jPopupMenu;
		}
		
	}
}