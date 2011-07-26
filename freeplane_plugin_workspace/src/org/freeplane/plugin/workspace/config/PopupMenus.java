package org.freeplane.plugin.workspace.config;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspacePreferences;
import org.freeplane.plugin.workspace.config.actions.TestAction;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

public class PopupMenus {

	private static final String WORKSPACE_POPUP_MENU_KEY = "/workspace_popup";
	private static final String WORKSPACE_NODE_POPUP_MENU_KEY = "/workspace_node_popup";
	private static final String WORKSPACE_POPUP_MENU_CONFIG = "/xml/popup_menus.xml";

	private JPopupMenu workspacePopupMenu;
	private JPopupMenu workspaceNodePopupMenu;
	private final MenuBuilder builder;
	
	public PopupMenus() {
		Controller.getCurrentModeController().addAction(new TestAction());
		
		builder = new MenuBuilder(Controller.getCurrentModeController());
		Set<String> emptySet = Collections.emptySet();
		
		addMenuEntries();
		builder.processMenuCategory(this.getClass().getResource(WORKSPACE_POPUP_MENU_CONFIG), emptySet);
		
	}  
	
	private void addMenuEntries() {
		workspacePopupMenu = new JPopupMenu("");
		workspaceNodePopupMenu = new JPopupMenu("");
		
		builder.addPopupMenu(workspacePopupMenu, WORKSPACE_POPUP_MENU_KEY);
		builder.addPopupMenu(workspaceNodePopupMenu, WORKSPACE_NODE_POPUP_MENU_KEY);

		Controller.getCurrentModeController().addMenuContributor(new IMenuContributor() {
			ResourceController resourceController = Controller.getCurrentController().getResourceController();

			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				builder.addCheckboxItem(WORKSPACE_POPUP_MENU_KEY, new CheckBoxAction(WorkspacePreferences.SHOW_WORKSPACE_TEXT,
						WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY), resourceController
						.getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY));
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
		System.out.println("FISH WorkspacePreferences: handleEvent");
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			Component component = (Component) event.getSource();			
			
			final ControllerPopupMenuListener popupListener = new ControllerPopupMenuListener();
			final JPopupMenu popupmenu = WorkspaceController.getCurrentWorkspaceController().getPopups().getWorkspacePopupMenu();
			if (popupmenu != null) {
                popupmenu.addHierarchyListener(popupListener);
                popupmenu.show(component, event.getX(), event.getY());
                //event.consume();
            }
			else {
				System.out.println("FISH popupmenu is null");
			}

		}
		System.out.println("Event: " + event);
	}
}