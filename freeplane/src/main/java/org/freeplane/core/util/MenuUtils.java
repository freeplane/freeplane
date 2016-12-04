/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Volker Boerchers
 *
 *  This file's author is Volker Boerchers
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
 *  along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package org.freeplane.core.util;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionAcceleratorManager;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.IndexedTree.Node;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.FreeplaneResourceAccessor;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

/** Utilities for dealing with the Freeplane menu: In scripts available as "global variable" menuUtils. */
public class MenuUtils {
	/** The userObject type for createMenuEntryTree(). */
	public static class MenuEntry {
		private final String key;
		private final String label;
		private final String iconKey;
		private final KeyStroke keyStroke;
		private final String toolTipText;

		public MenuEntry(final String key, final String label, final String iconKey, final KeyStroke keyStroke,
		                 final String toolTipText) {
			this.key = key;
			this.label = label;
			this.iconKey = iconKey;
			this.keyStroke = keyStroke;
			this.toolTipText = toolTipText;
		}

		public MenuEntry(String key, String label) {
			this(key, label, null, null, null);
		}

		public String getKey() {
			return key;
		}

		public String getLabel() {
			return label;
		}

		public String getIconKey() {
			return iconKey;
		}

		public KeyStroke getKeyStroke() {
			return keyStroke;
		}

		public String getToolTipText() {
			return toolTipText;
		}

		public MindIcon createMindIcon() {
			String resource = ResourceController.getResourceController().getProperty(iconKey, null);
			if (resource == null) {
				// this is the regular case: most MenuEntries (i.e. actions) will have the iconKey set
				// but only for a few of these Icons are available
				return null;
			}
			return new MindIcon(resource.replaceAll("/images/(.*).png", "../$1"));
		}

		@Override
		public String toString() {
			return label;
		}
	}

	public static class MenuEntryTreeBuilder {
		private final ActionAcceleratorManager acceleratorManager;

		private MenuEntryTreeBuilder() {
			acceleratorManager = ResourceController.getResourceController().getAcceleratorManager();
		}

		private DefaultMutableTreeNode build(final String menuRootKey) {
			Entry entry = genericMenuStructure().findEntry(menuRootKey);
			if (entry == null)
				throw new IllegalArgumentException("not found: menuRootKey=" + menuRootKey);
			final DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode(menuNode2menuEntryNode(entry));
			addChildrenRecursively(treeRoot, entry.children());
			return treeRoot;
		}

		private void addChildrenRecursively(final DefaultMutableTreeNode treeNode, final Iterable<Entry> menuElements) {
			for (Entry childMenu : menuElements) {
				final DefaultMutableTreeNode treeChild = menuNode2menuEntryNode(childMenu);
				// the tree of Entrys contains pseudo elements like builder nodes that have to be skipped
				if (treeChild != null) {
					addChildrenRecursively(treeChild, childMenu.children());
					if (entryIsActionOrIsSubmenu(childMenu, treeChild))
						treeNode.add(treeChild);
				}
				else {
					addChildrenRecursively(treeNode, childMenu.children());
				}
			}
		}

		private boolean entryIsActionOrIsSubmenu(Entry childMenu, final DefaultMutableTreeNode treeChild) {
			return !treeChild.isLeaf() || childMenu.isLeaf();
		}

		private DefaultMutableTreeNode menuNode2menuEntryNode(Entry menuItem) {
			final EntryAccessor entryAccessor = new EntryAccessor(new FreeplaneResourceAccessor());
			final AFreeplaneAction action = entryAccessor.getAction(menuItem);
			final String name = menuItem.getName();
			if (menuItem.hasChildren() && StringUtils.isNotEmpty(name)) {
				// the tree of Entrys contains pseudo elements like builder nodes that have to be skipped
				String text = TextUtils.removeMnemonic(entryAccessor.getText(menuItem));
				final DefaultMutableTreeNode node = new DefaultMutableTreeNode(new MenuEntry(name, text));
				if (action != null) {
					final MenuEntry menuEntry = menuEntry(action);
					node.add(new DefaultMutableTreeNode(menuEntry));
				}
				return node;
			}
			else if (action != null) {
				final MenuEntry menuEntry = menuEntry(action);
				return new DefaultMutableTreeNode(menuEntry);
			}
			else {
				return null;
			}
		}

		private MenuEntry menuEntry(final AFreeplaneAction action) {
			String text = ActionUtils.getActionTitle(action);
			String iconKey = action.getIconKey();
			String tooltip = (String) action.getValue(Action.LONG_DESCRIPTION);
			KeyStroke accelerator = acceleratorManager.getAccelerator(action);
			final MenuEntry menuEntry = new MenuEntry(action.getKey(), text, iconKey, accelerator, tooltip);
			return menuEntry;
		}
	}

	/**
	 * returns a tree of all <code>JMenuItem</code> nodes the menu contains (omitting Strings and Separators).
	 * The tree is build from <code>DefaultMutableTreeNode</code> nodes having <code>MenuEntry</code> objects as
	 * their userObjects. 
	 * 
	 * Note that the root node may have null as userObject if the menu item at <code>menuRootKey</code> doesn't
	 * contain a <code>JMenuItem</code>!
	 * 
	 * @param menuRootKey the key of the node that should form the root of the output.
	 * @throws IllegalArgumentException if the menuRootKey does not point to an entry in the menu tree
	 */
	public static DefaultMutableTreeNode createMenuEntryTree(final String menuRootKey) {
		return new MenuEntryTreeBuilder().build(menuRootKey);
	}

	/** Used as the basis for dynamic generation of hotkey list.
	 * Same as {@link #createMenuEntryTree(String)} but all MenuEntries without associated accelerator
	 * and (then) empty submenus are removed from the result.
	 * @throws IllegalArgumentException if the menuRootKey does not point to an entry in the menu tree
	 */
	public static DefaultMutableTreeNode createAcceleratebleMenuEntryTree(final String menuRootKey) {
		final DefaultMutableTreeNode menuEntryTreeNode = MenuUtils.createMenuEntryTree(menuRootKey);
		final DefaultMutableTreeNode result = new DefaultMutableTreeNode(menuEntryTreeNode.getUserObject());
		addAcceleratableChildrenRecursively(result, menuEntryTreeNode.children());
		return result;
	}

	/** Could be (but currently isn't) used to generate a mindmap representation of the menu.
	 * @param children Enumeration of DefaultMutableTreeNode from the menu tree. */
	@SuppressWarnings("rawtypes")
	public static void insertAsNodeModelRecursively(final NodeModel nodeModel, final Enumeration children,
	                                                final MapController mapController) {
		while (children.hasMoreElements()) {
			final DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
			final NodeModel newNodeModel = insertAsNodeModel(nodeModel, child, mapController);
			if (!child.isLeaf()) {
				insertAsNodeModelRecursively(newNodeModel, child.children(), mapController);
			}
		}
	}

	private static NodeModel insertAsNodeModel(final NodeModel nodeModel, final DefaultMutableTreeNode treeNode,
	                                           final MapController mapController) {
		final MenuEntry menuEntry = (MenuEntry) treeNode.getUserObject();
		final String text = menuEntry.getKeyStroke() == null ? menuEntry.getLabel() : menuEntry.getLabel() + ": "
		        + MenuUtils.formatKeyStroke(menuEntry.getKeyStroke());
		final NodeModel newNodeModel = mapController.newNode(text, nodeModel.getMap());
		if (!treeNode.isLeaf()) {
			newNodeModel.setFolded(true);
		}
		if (menuEntry.getIconKey() != null) {
			final MindIcon mindIcon = menuEntry.createMindIcon();
			if (mindIcon != null)
				newNodeModel.addIcon(mindIcon);
		}
		nodeModel.insert(newNodeModel);
		return newNodeModel;
	}

	// filters out non-acceleratable menu entries
	@SuppressWarnings("rawtypes")
	private static void addAcceleratableChildrenRecursively(final DefaultMutableTreeNode target,
	                                                        final Enumeration sourceChildren) {
		while (sourceChildren.hasMoreElements()) {
			final DefaultMutableTreeNode sourceChild = (DefaultMutableTreeNode) sourceChildren.nextElement();
			final MenuEntry menuEntry = (MenuEntry) sourceChild.getUserObject();
			if (sourceChild.isLeaf()) {
				if (menuEntry.getKeyStroke() != null) {
					target.add(new DefaultMutableTreeNode(menuEntry));
				}
			}
			else {
				final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(menuEntry);
				target.add(newNode);
				addAcceleratableChildrenRecursively(newNode, sourceChild.children());
				if (newNode.isLeaf()) {
					target.remove(newNode);
				}
			}
		}
	}

	/** pretty print a keystroke. */
	public static String formatKeyStroke(final KeyStroke keyStroke) {
		final String keyModifiersText = KeyEvent.getKeyModifiersText(keyStroke.getModifiers());
		final String keyText = KeyEvent.getKeyText(keyStroke.getKeyCode());
		return keyModifiersText.length() == 0 ? keyText : keyModifiersText + "+" + keyText;
	}

	/** there are little reasons to use this in scripts. */
	public static Node findAssignedMenuItemNodeRecursively(final DefaultMutableTreeNode menubarNode,
	                                                       final KeyStroke keystroke) {
		final Enumeration<?> children = menubarNode.children();
		while (children.hasMoreElements()) {
			final Node child = (Node) children.nextElement();
			final Object childUserObject = child.getUserObject();
			if (childUserObject instanceof JMenuItem) {
				final JMenuItem childMenuItem = (JMenuItem) childUserObject;
				if (keystroke.equals(childMenuItem.getAccelerator())) {
					return child;
				}
			}
			// recurse
			final Node assignedMenuItemNode = findAssignedMenuItemNodeRecursively(child, keystroke);
			if (assignedMenuItemNode != null)
				return assignedMenuItemNode;
		}
		return null;
	}
	/**
	 * to be used from scripts to execute menu items. Find out the menuItemKey
	 * of a menu item with the devtools add-on. It contains a tool for that.
	 */
	public static void executeMenuItems(final List<String> menuItemKeys) {
		LogUtils.info("menu items to execute: " + menuItemKeys);
		final Entry genericMenuStructure = genericMenuStructure();
		final EntryAccessor entryAccessor = new EntryAccessor(new FreeplaneResourceAccessor());
		for (String menuItemKey : menuItemKeys) {
			Entry menuItem = genericMenuStructure.findEntry(menuItemKey);
			final AFreeplaneAction action = menuItem != null ? entryAccessor.getAction(menuItem) : null;
			if (action == null) {
				UITools.errorMessage(TextUtils.format("MenuUtils.invalid_menuitem", menuItemKey));
				return;
			}
			LogUtils.info("executing " + ActionUtils.getActionTitle(action) + "(" + menuItemKey + ")");
			ActionEvent e = new ActionEvent(menuItem, 0, null);
			action.actionPerformed(e);
		}
	}

	/** returns the icon for a menuItemKey or null if it has none. */
	public static Icon getMenuItemIcon(String menuItemKey) {
		Entry menuItem = genericMenuStructure().findEntry(menuItemKey);
		if (menuItem == null)
			return null;
		final EntryAccessor entryAccessor = new EntryAccessor(new FreeplaneResourceAccessor());
		final AFreeplaneAction action = entryAccessor.getAction(menuItem);
		return (Icon) action.getValue(Action.SMALL_ICON);
	}

	private static IUserInputListenerFactory userInputFactory() {
		ModeController  modeController = Controller.getCurrentModeController();
		return modeController.getUserInputListenerFactory();
	}

	private static Entry genericMenuStructure() {
		return userInputFactory().getGenericMenuStructure();
	}
}
