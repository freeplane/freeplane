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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.icon.MindIcon;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.IndexedTree.Node;

public class MenuTools {
	/** The userObject type for createMenuEntryTree(). */
	public static class MenuEntry {
		private final String key;
		private final String label;
		private final Icon icon;
		private final KeyStroke keyStroke;

		public MenuEntry(String key, String label, Icon icon, KeyStroke keyStroke) {
			this.key = key;
			this.label = label;
			this.icon = icon;
			this.keyStroke = keyStroke;
		}

		public String getKey() {
			return key;
		}

		public String getLabel() {
			return label;
		}

		public Icon getIcon() {
			return icon;
		}

		public KeyStroke getKeyStroke() {
			return keyStroke;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private static class IconMindIcon extends MindIcon {
		private Icon icon;

		// FIXME: improve by conditional cast to ImageIcon? ImageIcon at least has a description
		public IconMindIcon(Icon icon) {
			super("", "");
			this.icon = icon;
		}

		public Icon getIcon() {
			return icon;
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
			final String key = String.valueOf(node.getKey());
			return new DefaultMutableTreeNode(new MenuEntry(key, jMenuItem.getText(), jMenuItem.getIcon(),
			    menuKeyToKeyStrokeMap.get(key)));
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
		if (menuEntry.getIcon() != null)
			newNodeModel.addIcon(new IconMindIcon(menuEntry.getIcon()));
		mapController.insertNodeIntoWithoutUndo(newNodeModel, nodeModel);
		return newNodeModel;
	}

	// FIXME: is there a better formatter for KeyStrokes?
	private static String formatKeyStroke(KeyStroke keyStroke) {
		final String[] components = keyStroke.toString().replaceAll("(typed|released|pressed)", "").split("\\s+");
		StringBuilder builder = new StringBuilder();
		for (String s : components) {
			if (builder.length() > 0)
				builder.append("+");
			if (s.length() > 1) {
				builder.append(s.substring(0, 1).toUpperCase());
				builder.append(s.substring(1));
			}
			else {
				builder.append(s.toUpperCase());
			}
		}
		return builder.toString();
	}
}
