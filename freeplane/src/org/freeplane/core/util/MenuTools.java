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

import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IFreeplaneAction;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.IndexedTree.Node;
import org.freeplane.features.common.icon.MindIcon;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.NodeModel;

public class MenuTools {
	/** The userObject type for createMenuEntryTree(). */
	public static class MenuEntry {
		private final String key;
		private final String label;
		private final String iconKey;
		private final KeyStroke keyStroke;
		private final String toolTipText;

		public MenuEntry(String key, String label, String iconKey, KeyStroke keyStroke, String toolTipText) {
			this.key = key;
			this.label = label;
			this.iconKey = iconKey;
			this.keyStroke = keyStroke;
			this.toolTipText = toolTipText;
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
	public static DefaultMutableTreeNode createMenuEntryTree(String menuRootKey, final MenuBuilder menuBuilder) {
		final HashMap<String, KeyStroke> menuKeyToKeyStrokeMap = invertAcceleratorMap(menuBuilder.getAcceleratorMap());
		final DefaultMutableTreeNode menuRoot = menuBuilder.get(menuRootKey);
		final DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode(menuNode2menuEntryNode(menuRoot,
		    menuKeyToKeyStrokeMap));
		addChildrenRecursively(treeRoot, menuRoot.children(), menuKeyToKeyStrokeMap);
		return treeRoot;
	}

	@SuppressWarnings("unchecked")
	private static void addChildrenRecursively(DefaultMutableTreeNode treeNode, Enumeration menuChildren,
	                                           HashMap<String, KeyStroke> menuKeyToKeyStrokeMap) {
		while (menuChildren.hasMoreElements()) {
			DefaultMutableTreeNode childMenu = (DefaultMutableTreeNode) menuChildren.nextElement();
			final DefaultMutableTreeNode treeChild = menuNode2menuEntryNode(childMenu, menuKeyToKeyStrokeMap);
			if (treeChild != null) {
				treeNode.add(treeChild);
				addChildrenRecursively(treeChild, childMenu.children(), menuKeyToKeyStrokeMap);
			}
			else {
				addChildrenRecursively(treeNode, childMenu.children(), menuKeyToKeyStrokeMap);
			}
		}
	}

	// in: node for JMenu, out: node for MenuEntry
	private static DefaultMutableTreeNode menuNode2menuEntryNode(DefaultMutableTreeNode menuNode,
	                                                             HashMap<String, KeyStroke> menuKeyToKeyStrokeMap) {
		IndexedTree.Node node = (Node) menuNode;
		final Object userObject = menuNode.getUserObject();
		if (userObject instanceof JMenuItem) {
			JMenuItem jMenuItem = (JMenuItem) userObject;
			IFreeplaneAction action = (IFreeplaneAction) jMenuItem.getAction();
			final String key = String.valueOf(node.getKey());
			final String iconKey = action == null ? null : action.getIconKey();
			return new DefaultMutableTreeNode(new MenuEntry(key, jMenuItem.getText(), iconKey, menuKeyToKeyStrokeMap
			    .get(key), jMenuItem.getToolTipText()));
		}
		// the other expected types are String and javax.swing.JPopupMenu.Separator
		// - just omit them
		return null;
	}

	/**
	 * Same as {@link #createMenuEntryTree(String, Controller)} but all MenuEntries without associated accelerator
	 * and (then) empty submenus are removed from the result.
	 */
	public static DefaultMutableTreeNode createAcceleratebleMenuEntryTree(String menuRootKey, MenuBuilder menuBuilder) {
		final DefaultMutableTreeNode menuEntryTreeNode = createMenuEntryTree(menuRootKey, menuBuilder);
		DefaultMutableTreeNode result = new DefaultMutableTreeNode(menuEntryTreeNode.getUserObject());
		addAcceleratableChildrenRecursively(result, menuEntryTreeNode.children());
		return result;
	}

	// filters out non-acceleratable menu entries
	@SuppressWarnings("unchecked")
	private static void addAcceleratableChildrenRecursively(DefaultMutableTreeNode target, Enumeration sourceChildren) {
		while (sourceChildren.hasMoreElements()) {
			DefaultMutableTreeNode sourceChild = (DefaultMutableTreeNode) sourceChildren.nextElement();
			MenuEntry menuEntry = (MenuEntry) sourceChild.getUserObject();
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

	private static HashMap<String, KeyStroke> invertAcceleratorMap(Map<KeyStroke, Node> acceleratorMap) {
		HashMap<String, KeyStroke> result = new HashMap<String, KeyStroke>();
		for (Entry<KeyStroke, Node> entry : acceleratorMap.entrySet()) {
			result.put(String.valueOf(entry.getValue().getKey()), entry.getKey());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static void insertAsNodeModelRecursively(NodeModel nodeModel, Enumeration children,
	                                                MapController mapController) {
		while (children.hasMoreElements()) {
			final DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
			final NodeModel newNodeModel = insertAsNodeModel(nodeModel, child, mapController);
			if (!child.isLeaf())
				insertAsNodeModelRecursively(newNodeModel, child.children(), mapController);
		}
	}

	private static NodeModel insertAsNodeModel(NodeModel nodeModel, DefaultMutableTreeNode treeNode,
	                                           MapController mapController) {
		final MenuEntry menuEntry = (MenuEntry) treeNode.getUserObject();
		final String text = menuEntry.getKeyStroke() == null ? menuEntry.getLabel() : menuEntry.getLabel() + ": "
		        + formatKeyStroke(menuEntry.getKeyStroke());
		final NodeModel newNodeModel = mapController.newNode(text, nodeModel.getMap());
		if (!treeNode.isLeaf())
			newNodeModel.setFolded(true);
		if (menuEntry.getIconKey() != null) {
			addIcon(menuEntry, newNodeModel);
		}
		nodeModel.insert(newNodeModel);
		return newNodeModel;
	}

	private static void addIcon(final MenuEntry menuEntry, final NodeModel newNodeModel) {
		final String iconKey = menuEntry.getIconKey();
		String resource = ResourceController.getResourceController().getProperty(iconKey, null);
		if (resource == null) {
			// LogTool.info("no icon for key '" + iconKey + "'");
			return;
		}
		// icons are expected to live in the directory /images/icons/
		// but the menu icons live in /images - set a relative path to navigate one level up
		String name = resource.replaceAll("/images/(.*).png", "../$1");
		final MindIcon mindIcon = new MindIcon(name, name + ".png", "");
		newNodeModel.addIcon(mindIcon);
	}

	public static String formatKeyStroke(KeyStroke keyStroke) {
		final String keyModifiersText = KeyEvent.getKeyModifiersText(keyStroke.getModifiers());
		final String keyText = KeyEvent.getKeyText(keyStroke.getKeyCode());
		return keyModifiersText.length() == 0 ? keyText : keyModifiersText + "+" + keyText;
	}
}
