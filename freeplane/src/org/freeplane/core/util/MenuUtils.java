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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IFreeplaneAction;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.IndexedTree.Node;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
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

	/**
	 * returns a tree of all <code>JMenuItem</code> nodes the menu contains (omitting Strings and Separators).
	 * The tree is build from <code>DefaultMutableTreeNode</code> nodes having <code>MenuEntry</code> objects as
	 * their userObjects. 
	 * 
	 * Note that the root node may have null as userObject if the menu item at <code>menuRootKey</code> doesn't
	 * contain a <code>JMenuItem</code>!
	 * 
	 * @param menuRootKey the key of the node that should form the root of the output.
	 * @param menuBuilder access point for the menu(s).
	 */
	public static DefaultMutableTreeNode createMenuEntryTree(final String menuRootKey, final MenuBuilder menuBuilder) {
		final HashMap<String, KeyStroke> menuKeyToKeyStrokeMap = MenuUtils.invertAcceleratorMap(menuBuilder
		    .getAcceleratorMap());
		final DefaultMutableTreeNode menuRoot = menuBuilder.get(menuRootKey);
		final DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode(MenuUtils.menuNode2menuEntryNode(menuRoot,
		    menuKeyToKeyStrokeMap));
		MenuUtils.addChildrenRecursively(treeRoot, menuRoot.children(), menuKeyToKeyStrokeMap);
		return treeRoot;
	}

	@SuppressWarnings("rawtypes")
	private static void addChildrenRecursively(final DefaultMutableTreeNode treeNode, final Enumeration menuChildren,
	                                           final HashMap<String, KeyStroke> menuKeyToKeyStrokeMap) {
		while (menuChildren.hasMoreElements()) {
			final DefaultMutableTreeNode childMenu = (DefaultMutableTreeNode) menuChildren.nextElement();
			final DefaultMutableTreeNode treeChild = MenuUtils.menuNode2menuEntryNode(childMenu, menuKeyToKeyStrokeMap);
			if (treeChild != null) {
				treeNode.add(treeChild);
				MenuUtils.addChildrenRecursively(treeChild, childMenu.children(), menuKeyToKeyStrokeMap);
			}
			else {
				MenuUtils.addChildrenRecursively(treeNode, childMenu.children(), menuKeyToKeyStrokeMap);
			}
		}
	}

	// in: node for JMenu, out: node for MenuEntry
	private static DefaultMutableTreeNode menuNode2menuEntryNode(final DefaultMutableTreeNode menuNode,
	                                                             final HashMap<String, KeyStroke> menuKeyToKeyStrokeMap) {
		final IndexedTree.Node node = (Node) menuNode;
		final Object userObject = menuNode.getUserObject();
		if (userObject instanceof JMenuItem) {
			final JMenuItem jMenuItem = (JMenuItem) userObject;
			final IFreeplaneAction action = (IFreeplaneAction) jMenuItem.getAction();
			final String key = String.valueOf(node.getKey());
			final String iconKey = action == null ? null : action.getIconKey();
			return new DefaultMutableTreeNode(new MenuEntry(key, jMenuItem.getText(), iconKey, menuKeyToKeyStrokeMap
			    .get(key), jMenuItem.getToolTipText()));
		}
		// the other expected types are String and javax.swing.JPopupMenu.Separator
		// - just omit them
		return null;
	}

	/** Used as the basis for dynamic generation of hotkey list.
	 * Same as {@link #createMenuEntryTree(String, MenuBuilder)} but all MenuEntries without associated accelerator
	 * and (then) empty submenus are removed from the result.
	 */
	public static DefaultMutableTreeNode createAcceleratebleMenuEntryTree(final String menuRootKey,
	                                                                      final MenuBuilder menuBuilder) {
		final DefaultMutableTreeNode menuEntryTreeNode = MenuUtils.createMenuEntryTree(menuRootKey, menuBuilder);
		final DefaultMutableTreeNode result = new DefaultMutableTreeNode(menuEntryTreeNode.getUserObject());
		MenuUtils.addAcceleratableChildrenRecursively(result, menuEntryTreeNode.children());
		return result;
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
				MenuUtils.addAcceleratableChildrenRecursively(newNode, sourceChild.children());
				if (newNode.isLeaf()) {
					target.remove(newNode);
				}
			}
		}
	}

	private static HashMap<String, KeyStroke> invertAcceleratorMap(final Map<KeyStroke, Node> acceleratorMap) {
		final HashMap<String, KeyStroke> result = new HashMap<String, KeyStroke>();
		for (final Entry<KeyStroke, Node> entry : acceleratorMap.entrySet()) {
			result.put(String.valueOf(entry.getValue().getKey()), entry.getKey());
		}
		return result;
	}

	/** Could be (but currently isn't) used to generate a mindmap representation of the menu.
	 * @param children Enumeration of DefaultMutableTreeNode from the menu tree. */
	@SuppressWarnings("rawtypes")
	public static void insertAsNodeModelRecursively(final NodeModel nodeModel, final Enumeration children,
	                                                final MapController mapController) {
		while (children.hasMoreElements()) {
			final DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
			final NodeModel newNodeModel = MenuUtils.insertAsNodeModel(nodeModel, child, mapController);
			if (!child.isLeaf()) {
				MenuUtils.insertAsNodeModelRecursively(newNodeModel, child.children(), mapController);
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

	/** that's the key that is used to define keyboard accelerators, e.g. found in the auto.properties. */
    public static String makeAcceleratorKey(String menuItemKey) {
        return "acceleratorForMindMap/$" + menuItemKey + "$0";
    }

    /** to be used from scripts to execute menu items. 
     * Find out the menuItemKey of a menu item with the devtools add-on. It contains a tool for that. */
    public static void executeMenuItems(final List<String> menuItemKeys) {
        LogUtils.info("menu items to execute: " + menuItemKeys);
        final MenuBuilder menuBuilder = getMenuBuilder();
        for (String menuItemKey : menuItemKeys) {
            final DefaultMutableTreeNode treeNode = menuBuilder.get(menuItemKey);
            if (treeNode == null || !treeNode.isLeaf() || !(treeNode.getUserObject() instanceof JMenuItem)) {
                UITools.errorMessage(TextUtils.format("MenuUtils.invalid_menuitem", menuItemKey));
                return;
            }
            final JMenuItem menuItem = (JMenuItem) treeNode.getUserObject();
            final Action action = menuItem.getAction();
            LogUtils.info("executing " + menuItem.getText() + "(" + menuItemKey + ")");
            ActionEvent e = new ActionEvent(menuItem, 0, null);
            action.actionPerformed(e);
        }
    }

    private static MenuBuilder getMenuBuilder() {
        final ModeController modeController = Controller.getCurrentModeController();
        final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
        return menuBuilder;
    }

    /** returns the icon for a menuItemKey or null if it has none. */
    public static Icon getMenuItemIcon(String menuItemKey) {
        final DefaultMutableTreeNode treeNode = getMenuBuilder().get(menuItemKey);
        if (treeNode == null || !treeNode.isLeaf() || !(treeNode.getUserObject() instanceof JMenuItem)) {
            return null;
        }
        final JMenuItem menuItem = (JMenuItem) treeNode.getUserObject();
        return menuItem.getIcon();
    }
}
