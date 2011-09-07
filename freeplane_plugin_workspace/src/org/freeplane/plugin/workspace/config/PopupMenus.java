package org.freeplane.plugin.workspace.config;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.config.actions.AddExistingFilesystemFolderAction;
import org.freeplane.plugin.workspace.config.actions.AddNewFilesystemFolderAction;
import org.freeplane.plugin.workspace.config.actions.AddNewGroupAction;
import org.freeplane.plugin.workspace.config.actions.FileNodeAddNewMindmapAction;
import org.freeplane.plugin.workspace.config.actions.FileNodeCopyAction;
import org.freeplane.plugin.workspace.config.actions.FileNodeCutAction;
import org.freeplane.plugin.workspace.config.actions.FileNodeDeleteAction;
import org.freeplane.plugin.workspace.config.actions.FileNodePasteAction;
import org.freeplane.plugin.workspace.config.actions.FileNodeRenameAction;
import org.freeplane.plugin.workspace.config.actions.RemoveNodeFromWorkspaceAction;
import org.freeplane.plugin.workspace.config.actions.WorkspaceCollapseAction;
import org.freeplane.plugin.workspace.config.actions.WorkspaceExpandAction;
import org.freeplane.plugin.workspace.config.actions.WorkspaceHideAction;
import org.freeplane.plugin.workspace.config.actions.WorkspaceRefreshAction;
import org.freeplane.plugin.workspace.config.actions.WorkspaceSetLocationAction;
import org.freeplane.plugin.workspace.view.WorkspacePopupMenu;

public class PopupMenus {

	private static final String WORKSPACE_POPUP_MENU_KEY = "/workspace_popup";
	private static final String WORKSPACE_POPUP_MENU_CONFIG = "/xml/popup_menus.xml";
	private static final String WORKSPACE_PHYSICAL_NODE_POPUP_MENU_KEY = "/workspace_physical_node_popup";

	private HashMap<String, PopupObject> popupMap;

	public PopupMenus() {
		registerWorkspaceActions();

		initialize();
	}

	private void registerWorkspaceActions() {
		ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new WorkspaceCollapseAction());
		modeController.addAction(new WorkspaceSetLocationAction());
		modeController.addAction(new FileNodeAddNewMindmapAction());
		modeController.addAction(new FileNodeCutAction());
		modeController.addAction(new WorkspaceHideAction());
		modeController.addAction(new FileNodeRenameAction());
		modeController.addAction(new FileNodeDeleteAction());
		modeController.addAction(new FileNodeCopyAction());
		modeController.addAction(new WorkspaceExpandAction());
		modeController.addAction(new FileNodePasteAction());
		modeController.addAction(new WorkspaceRefreshAction());
		modeController.addAction(new AddNewFilesystemFolderAction());
		modeController.addAction(new AddExistingFilesystemFolderAction());
		modeController.addAction(new RemoveNodeFromWorkspaceAction());
		modeController.addAction(new AddNewGroupAction());
	}

	// public boolean registerPopupMenu(final String key) {
	// if (popupMap.containsKey(key)) {
	// return false;
	// }
	// registerPopupMenu(key, "/"+key, WORKSPACE_POPUP_MENU_CONFIG);
	// return true;
	// }
	//
	public void registerPopupMenuNodeDefault(String key) {		
		registerPopupMenu(key, "/workspace_node_default");
	}

	public void registerPopupMenu(final String key, final String xmlKey) {
		PopupObject popObj;
		if (!this.popupMap.containsKey(key)) {
			popObj = new PopupObject(new WorkspacePopupMenu(), new MenuBuilder(Controller.getCurrentModeController()));

			this.popupMap.put(key, popObj);
		}
		else {
			popObj = this.popupMap.get(key);
		}

		if (!popObj.menuBuilder.contains(xmlKey)) {
			popObj.menuBuilder.addPopupMenu(popObj.popupMenu, xmlKey);
		}
	}

	public void buildPopupMenu(final String key) {
		PopupObject popObj = this.popupMap.get(key);
		if (popObj != null && !popObj.isBuilt) {
			final ControllerPopupMenuListener popupListener = new ControllerPopupMenuListener();
			popObj.popupMenu.addHierarchyListener(popupListener);

			Set<String> emptySet = Collections.emptySet();
			popObj.menuBuilder.processMenuCategory(this.getClass().getResource(WORKSPACE_POPUP_MENU_CONFIG), emptySet);
			popObj.isBuilt = true;
		}
	}

	// public void extendPopupMenu(final String key, final String xmlKey) {
	// PopupObject popObj = this.popupMap.get(key);
	// popObj.menuBuilder.addPopupMenu(popObj.popupMenu, xmlKey);
	//
	// final ControllerPopupMenuListener popupListener = new
	// ControllerPopupMenuListener();
	// popObj.popupMenu.addHierarchyListener(popupListener);
	//
	// Set<String> emptySet = Collections.emptySet();
	// popObj.menuBuilder.processMenuCategory(this.getClass().getResource(WORKSPACE_POPUP_MENU_CONFIG),
	// emptySet);
	// }

	public void addAction(final String popupKey, final String menuKey, AFreeplaneAction action) {
		PopupObject popObj = this.popupMap.get(popupKey);
		popObj.menuBuilder.addAction(menuKey, action, MenuBuilder.AS_CHILD);
	}

	public void addCechkbox(final String popupKey, final String menuKey, AFreeplaneAction action, boolean isSelected) {
		PopupObject popObj = this.popupMap.get(popupKey);
		// if (!popObj.menuBuilder.contains(menuKey+"/"+action.getKey())) {
		popObj.menuBuilder.addCheckboxItem(menuKey, menuKey + "/" + action.getKey(), action, isSelected);
		// }
	}

	public class CheckBoxAction extends AFreeplaneAction {

		private static final long serialVersionUID = 1L;
		private String propertyKey;

		public CheckBoxAction(String key, String propertyKey) {
			super(key);
			this.propertyKey = propertyKey;
		}

		public void actionPerformed(ActionEvent e) {
			System.out.println("PopupMenus.actionPerformed: " + e.getActionCommand() + " " + this.propertyKey);
			boolean checked = ((JCheckBoxMenuItem) e.getSource()).isSelected();
			Controller.getCurrentController().getResourceController().setProperty(this.propertyKey, checked);

			// if (checked) {
			// WorkspaceController.getCurrentWorkspaceController().showWorkspaceView(true);
			// }
			// else {
			// WorkspaceController.getCurrentWorkspaceController().showWorkspaceView(false);
			// }
		}
	}

	public void showWorkspacePopup(Component component, int x, int y) {
		showPopup(WORKSPACE_POPUP_MENU_KEY, component, x, y);
	}

	public void showPhysicalNodePopup(Component component, int x, int y) {
		showPopup(WORKSPACE_PHYSICAL_NODE_POPUP_MENU_KEY, component, x, y);
	}

	public void showPopup(String popupKey, Component component, int x, int y) {
		PopupObject popObj = this.popupMap.get(popupKey);
		if (popObj==null) {			
			//no popup menu registered
			return;
		}
		
		final WorkspacePopupMenu popupMenu = popObj.popupMenu;
		popupMenu.setInvokerLocation(new Point(x, y));
		if (popupMenu != null) {
			popupMenu.show(component, x, y);
		}
	}

	private class PopupObject {
		public WorkspacePopupMenu popupMenu;
		public MenuBuilder menuBuilder;
		public boolean isBuilt = false;

		public PopupObject(final WorkspacePopupMenu popupMenu, final MenuBuilder menuBuilder) {
			this.menuBuilder = menuBuilder;
			this.popupMenu = popupMenu;
		}

	}
	
	public void initialize() {
		popupMap = new HashMap<String, PopupMenus.PopupObject>();

		// Default Node Popups
		registerPopupMenu(WORKSPACE_POPUP_MENU_KEY, WORKSPACE_POPUP_MENU_KEY);
		buildPopupMenu(WORKSPACE_POPUP_MENU_KEY);
		registerPopupMenu(WORKSPACE_PHYSICAL_NODE_POPUP_MENU_KEY, WORKSPACE_PHYSICAL_NODE_POPUP_MENU_KEY);
		buildPopupMenu(WORKSPACE_PHYSICAL_NODE_POPUP_MENU_KEY);
	}
}
