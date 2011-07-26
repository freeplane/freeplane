package org.freeplane.plugin.workspace.config;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspacePreferences;
import org.freeplane.plugin.workspace.config.actions.TestAction;

public class PopupMenus {

	private static final String WORKSPACE_POPUP_MENU_KEY = "/workspace_popup";
	private static final String WORKSPACE_POPUP_MENU_CONFIG = "/xml/popup_menus.xml";

	private JPopupMenu workspacePopupMenu;
	private final MenuBuilder builder;
	public PopupMenus() {
		Controller.getCurrentModeController().addAction(new TestAction());
		
		initReadManager();
		builder = new MenuBuilder(Controller.getCurrentModeController());
		Set<String> emptySet = Collections.emptySet();
		
		addMenuEntries();
		builder.processMenuCategory(this.getClass().getResource(WORKSPACE_POPUP_MENU_CONFIG), emptySet);
		
		
	}
	
	private void initReadManager() {
//		readManager.addElementHandler("workspace_structure", new WorkspaceCreator(tree));
//		readManager.addElementHandler("group", new GroupCreator(tree));
//		readManager.addElementHandler("filesystem_folder", new FilesystemFolderCreator(tree));
//		readManager.addElementHandler("filesystem_link", new FilesystemLinkCreator(tree));

	}

//	public void load(final URL xmlFile) {
//		final TreeXmlReader reader = new TreeXmlReader(readManager);
//		try {
//			reader.load(new InputStreamReader(new BufferedInputStream(xmlFile.openStream())));
//		}
//		catch (final IOException e) {
//			throw new RuntimeException(e);
//		}
//		catch (final XMLException e) {
//			throw new RuntimeException(e);
//		}
//	}

	

	private void addMenuEntries() {
		workspacePopupMenu = new JPopupMenu("workspace");
		builder.addPopupMenu(workspacePopupMenu, WORKSPACE_POPUP_MENU_KEY);

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
}