/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.icon.mindmapmode;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.IPropertyControlCreator;
import org.freeplane.core.resources.components.KeyProperty;
import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.MenuSplitter;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.JAutoScrollBarPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.icon.IIconInformation;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconGroup;
import org.freeplane.features.icon.IconStore;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.IExtensionCopier;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.ViewController;

/**
 * @author Dimitry Polivaev
 */
public class MIconController extends IconController {
	public static enum Keys {
		ICONS
	};

	private static class ExtensionCopier implements IExtensionCopier {
		public void copy(final Object key, final NodeModel from, final NodeModel to) {
			if (!key.equals(Keys.ICONS)) {
				return;
			}
			copy(from, to);
		}

		public void copy(final NodeModel from, final NodeModel to) {
			final List<MindIcon> sourceIcons = from.getIcons();
			final List<MindIcon> targetIcons = to.getIcons();
			for (final MindIcon icon : sourceIcons) {
				if (targetIcons.contains(icon)) {
					continue;
				}
				to.addIcon(icon);
			}
		}

		public void remove(final Object key, final NodeModel from) {
			if (!key.equals(Keys.ICONS)) {
				return;
			}
			while (from.removeIcon() > 0) {
				;
			}
		}

		public void remove(final Object key, final NodeModel from, final NodeModel which) {
			if (!key.equals(Keys.ICONS)) {
				return;
			}
			final List<MindIcon> targetIcons = from.getIcons();
			final List<MindIcon> whichIcons = which.getIcons();
			for (final MindIcon icon : targetIcons) {
				if (!whichIcons.contains(icon)) {
					continue;
				}
				final int position = targetIcons.lastIndexOf(icon);
				if (position == -1) {
					continue;
				}
				from.removeIcon(position);
			}
		}
		public void resolveParentExtensions(Object key, NodeModel to) {
        }
	}

	private final Map<MindIcon, AFreeplaneAction> iconActions = new LinkedHashMap<MindIcon, AFreeplaneAction>();
	private final IconStore STORE = IconStoreFactory.create();
	private final JToolBar iconToolBar;
	private final JAutoScrollBarPane iconToolBarScrollPane;

	/**
	 * @param modeController
	 */
	public MIconController(final ModeController modeController) {
		super(modeController);
		modeController.registerExtensionCopier(new ExtensionCopier());
		iconToolBar = new FreeplaneToolBar("icon_toolbar", SwingConstants.VERTICAL);
		iconToolBarScrollPane = new JAutoScrollBarPane(iconToolBar);
		UITools.setScrollbarIncrement(iconToolBarScrollPane);
		UITools.addScrollbarIncrementPropertyListener(iconToolBarScrollPane);
		iconToolBarScrollPane.putClientProperty(ViewController.VISIBLE_PROPERTY_KEY, "leftToolbarVisible");
		createIconActions(modeController);
		createPreferences();
		modeController.addMenuContributor(new IMenuContributor() {
			public void updateMenus(final ModeController modeController, MenuBuilder builder) {
				MIconController.this.updateMenus(modeController, builder);
				updateIconToolbar(modeController);
			}
		});
	}

	public void addIcon(final NodeModel node, final MindIcon icon) {
		final IActor actor = new IActor() {
			public void act() {
				node.addIcon(icon);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON, null, icon);
			}

			public String getDescription() {
				return "addIcon";
			}

			public void undo() {
				node.removeIcon();
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON, icon, null);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	public void addIcon(final NodeModel node, final MindIcon icon, final int position) {
		final IActor actor = new IActor() {
			public void act() {
				node.addIcon(icon, position);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON, null, icon);
			}

			public String getDescription() {
				return "addIcon";
			}

			public void undo() {
				node.removeIcon(position);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON, icon, null);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	private void addIconGroupToMenu(final MenuBuilder builder, final String category, final IconGroup group) {
		if (group.getIcons().size() < 1) {
			return;
		}
		final JMenuItem item = new JMenu();
		item.setIcon(group.getGroupIcon().getIcon());
		item.setText(group.getDescription());
		final String itemKey = category + "/" + group;
		builder.addMenuItem(category, item, itemKey, MenuBuilder.AS_CHILD);
		for (final MindIcon icon : group.getIcons()) {
			final String fileName = icon.getFileName();
			addAction(builder, itemKey, icon, fileName);
		}
	}

	private void addAction(final MenuBuilder builder, final String itemKey, final MindIcon icon, final String fileName) {
		final int separatorPosition = fileName.indexOf('/');
		if (separatorPosition == -1) {
			builder.addAction(itemKey, iconActions.get(icon), MenuBuilder.AS_CHILD);
			return;
		}
		final String submenuName = fileName.substring(0, separatorPosition);
		final String submenuKey = itemKey + "/" + submenuName;
		if (null == builder.get(submenuKey)) {
			final JMenu submenu = new JMenu(submenuName);
			builder.addMenuItem(itemKey, submenu, submenuKey, MenuBuilder.AS_CHILD);
		}
		addAction(builder, submenuKey, icon, fileName.substring(separatorPosition + 1));
	}

	private void addIconsToMenu(final ModeController modeController, final MenuBuilder builder, final String iconMenuString) {
		if(builder.get(iconMenuString) == null)
			return;
		for (final IconGroup iconGroup : STORE.getGroups()) {
			addIconGroupToMenu(builder, iconMenuString, iconGroup);
		}
	}

	private void createIconActions(final ModeController modeController) {
		modeController.addAction(new RemoveIconAction(0));
		modeController.addAction(new RemoveIconAction(-1));
		modeController.addAction(new RemoveAllIconsAction());
		for (final MindIcon icon : STORE.getMindIcons()) {
			final IconAction myAction = new IconAction(icon);
			iconActions.put(icon, myAction);
		}
	}

	private void createPreferences() {
		final MModeController modeController = (MModeController) Controller.getCurrentModeController();
		final OptionPanelBuilder optionPanelBuilder = modeController.getOptionPanelBuilder();
		final List<AFreeplaneAction> actions = new ArrayList<AFreeplaneAction>();
		actions.addAll(iconActions.values());
		actions.add(modeController.getAction("RemoveIcon_0_Action"));
		actions.add(modeController.getAction("RemoveIconAction"));
		actions.add(modeController.getAction("RemoveAllIconsAction"));
		
		for (final AFreeplaneAction iconAction : actions) {
			final IIconInformation info = (IIconInformation) iconAction;
			optionPanelBuilder.addCreator("Keystrokes/icons", new IPropertyControlCreator() {
				public IPropertyControl createControl() {
					final KeyProperty keyProperty = new KeyProperty(info.getShortcutKey());
					keyProperty.setLabelText(info.getDescription());
					keyProperty.setImageIcon(info.getIcon());
					keyProperty.disableModifiers();
					return keyProperty;
				}
			}, IndexedTree.AS_CHILD);
		}
	}

	public Collection<AFreeplaneAction> getIconActions() {
		return Collections.unmodifiableCollection(iconActions.values());
	}

	/**
	 * @return
	 */
	public JComponent getIconToolBarScrollPane() {
		return iconToolBarScrollPane;
	}

	public Collection<MindIcon> getMindIcons() {
		final List<MindIcon> iconInfoList = new ArrayList<MindIcon>();
		final Collection<AFreeplaneAction> iconActions = getIconActions();
		for (final Action action : iconActions) {
			final MindIcon info = ((IconAction) action).getMindIcon();
			iconInfoList.add(info);
		}
		return iconInfoList;
	}

	private JMenu getSubmenu( final IconGroup group) {
		final JMenu menu = new JMenu("\u25ba") {
			private static final long serialVersionUID = 1L;

			@Override
			protected Point getPopupMenuOrigin() {
				return new Point(getWidth(), 0);
			}
		};
		menu.setFont(menu.getFont().deriveFont(8F));
		menu.setMargin(new Insets(0, 0, 0, 0));
		menu.setIcon(group.getGroupIcon().getIcon());
		for (final MindIcon icon : group.getIcons()) {
			addActionToIconSubmenu(menu, icon, icon.getFileName());
		}
		menu.setToolTipText(group.getDescription());
		return menu;
	}

	private void addActionToIconSubmenu(final JMenu menu, final MindIcon icon, final String fileName) {
		final AFreeplaneAction myAction = iconActions.get(icon);
		final int separatorPosition = fileName.indexOf('/');
		if (separatorPosition == -1) {
			new MenuSplitter().addMenuComponent(menu, new JMenuItem(myAction),  menu.getItemCount());
			return;
		}
		final String submenuName = fileName.substring(0, separatorPosition);
		final int componentCount = menu.getItemCount();
		if (componentCount != 0) {
			final Component lastComponent = menu.getMenuComponent(componentCount - 1);
			if (lastComponent instanceof JMenu) {
				final JMenu lastSubmenu = (JMenu) lastComponent;
				if (lastSubmenu.getText().equals(submenuName)) {
					addActionToIconSubmenu(lastSubmenu, icon, fileName.substring(separatorPosition + 1));
					return;
				}
			}
		}
		final JMenu submenu = new JMenu(submenuName);
		menu.add(submenu);
		addActionToIconSubmenu(submenu, icon, fileName.substring(separatorPosition + 1));
	}

	private void insertSubmenus(final JToolBar iconToolBar) {
		final JMenuBar iconMenuBar = new JMenuBar() {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getMaximumSize() {
				final Dimension preferredSize = getPreferredSize();
				return new Dimension(Short.MAX_VALUE, preferredSize.height);
			}
		};
		iconMenuBar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		iconMenuBar.setLayout(new GridLayout(0, 1));
		for (final IconGroup iconGroup : STORE.getGroups()) {
			iconMenuBar.add(getSubmenu(iconGroup));
		}
		iconToolBar.add(iconMenuBar);
	}

	public void removeAllIcons(final NodeModel node) {
		final int size = node.getIcons().size();
		final MIconController iconController = (MIconController) IconController.getController();
		for (int i = 0; i < size; i++) {
			iconController.removeIcon(node, 0);
		}
	}

	public int removeIcon(final NodeModel node) {
		return removeIcon(node, -1);
	}

	public int removeIcon(final NodeModel node, final int position) {
		final int size = node.getIcons().size();
		final int index = position >= 0 ? position : size + position;
		if (size == 0 || size <= index) {
			return size;
		}
		final IActor actor = new IActor() {
			private final MindIcon icon = node.getIcon(index);

			public void act() {
				node.removeIcon(index);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON, icon, null);
			}

			public String getDescription() {
				return "removeIcon";
			}

			public void undo() {
				node.addIcon(icon, index);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON, null, icon);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
		return node.getIcons().size();
	}

	private void updateIconToolbar(ModeController modeController) {
		iconToolBar.removeAll();
		iconToolBar.add(modeController.getAction("RemoveIcon_0_Action"))
		    .setAlignmentX(JComponent.CENTER_ALIGNMENT);
		iconToolBar.add(modeController.getAction("RemoveIconAction")).setAlignmentX(JComponent.CENTER_ALIGNMENT);
		iconToolBar.add(modeController.getAction("RemoveAllIconsAction")).setAlignmentX(
		    JComponent.CENTER_ALIGNMENT);
		iconToolBar.addSeparator();
		if (ResourceController.getResourceController().getBooleanProperty("structured_icon_toolbar")) {
			insertSubmenus(iconToolBar);
			return;
		}
		final String[] fpIcons = ResourceController.getResourceController().getProperty("icons.list").split(";");
		for (final String icon : fpIcons) {
			final MindIcon mindIcon = STORE.getMindIcon(icon);
			final AFreeplaneAction iconAction = iconActions.get(mindIcon);
			iconToolBar.add(iconAction).setAlignmentX(JComponent.CENTER_ALIGNMENT);
		}
		final Collection<MindIcon> userIcons = STORE.getUserIcons();
		for (final MindIcon icon : userIcons) {
			final AFreeplaneAction iconAction = iconActions.get(icon);
			iconToolBar.add(iconAction).setAlignmentX(JComponent.CENTER_ALIGNMENT);
		}
	}

	private void updateMenus(ModeController modeController, final MenuBuilder builder) {
		addIconsToMenu(modeController, builder, "main_menu_icons");
		addIconsToMenu(modeController, builder, "popup_menu_icons");
	}
}
