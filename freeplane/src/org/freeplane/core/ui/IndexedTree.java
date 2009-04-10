/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.core.ui;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Dimitry Polivaev
 * 25.12.2008
 */
public class IndexedTree {
	protected static class Node extends DefaultMutableTreeNode {
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
		private Object key;

		Node(final Object userObject) {
			super(userObject);
		}

		Node(final Object userObject, final Object key) {
			this(userObject);
			this.key = key;
		}

		Object getKey() {
			return key;
		}
	}

	public static final int AFTER = 1;
	public static final int AS_CHILD = 0;
	public static final int BEFORE = -1;
	private final HashMap<Object, Node> string2Element;

	public IndexedTree(final Object root) {
		super();
		final Node rootNode = new Node(root);
		string2Element = new HashMap<Object, Node>();
		string2Element.put(this, rootNode);
	}

	public DefaultMutableTreeNode addElement(final Object relativeKey, final Object element, final int position) {
		final DefaultMutableTreeNode relativeNode = getNode(relativeKey);
		final DefaultMutableTreeNode node = new Node(element);
		if (relativeNode == null) {
			return node;
		}
		addNode(relativeNode, node, position);
		return node;
	}

	public void addElement(final Object relativeKey, final Object element, final Object key, final int position) {
		final DefaultMutableTreeNode existingNode = get(key);
		if (existingNode != null) {
			throw new RuntimeException(key.toString() + " added twice");
		}
		final DefaultMutableTreeNode relativeNode = getNode(relativeKey);
		if (relativeNode == null) {
			return;
		}
		final Node node = new Node(element, key);
		addNode(relativeNode, node, position);
		string2Element.put(key, node);
	}

	protected void addNode(final DefaultMutableTreeNode relativeNode, final DefaultMutableTreeNode node,
	                       final int position) {
		switch (position) {
			case AS_CHILD:
				relativeNode.add(node);
				break;
			case BEFORE: {
				final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) relativeNode.getParent();
				if (parent == null) {
					throw new RuntimeException("relative node has no parent element");
				}
				final int index = parent.getIndex(relativeNode);
				parent.insert(node, index);
				break;
			}
			case AFTER:
				final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) relativeNode.getParent();
				if (parent == null) {
					throw new RuntimeException("relative node has no parent element");
				}
				final int index = parent.getIndex(relativeNode);
				parent.insert(node, index + 1);
				break;
			default:
				throw new RuntimeException("wrong position");
		}
	}

	public boolean contains(final Object key) {
		return string2Element.containsKey(key);
	}

	public String dump() {
		return string2Element.toString();
	}

	public DefaultMutableTreeNode get(final Object key) {
		final Object object = string2Element.get(key);
		if (object == null) {
			return null;
		}
		return (DefaultMutableTreeNode) object;
	}

	protected DefaultMutableTreeNode getNode(final Object key) {
		final DefaultMutableTreeNode node = (string2Element.get(key));
		if (node == null) {
			Logger.global.warning(key + " not found");
		}
		return node;
	}

	public DefaultMutableTreeNode getRoot() {
		return string2Element.get(this);
	}

	public void removeChildElements(final Object key) {
		final DefaultMutableTreeNode node = getNode(key);
		final Enumeration children = node.children();
		while (children.hasMoreElements()) {
			final Node child = (Node) children.nextElement();
			final Object childKey = child.getKey();
			if (childKey != null) {
				string2Element.remove(childKey);
			}
		}
		node.removeAllChildren();
	}

	/**
	 */
	protected void removeChildKeys(final Node node) {
		final Enumeration children = node.children();
		while (children.hasMoreElements()) {
			final Node child = (Node) children.nextElement();
			string2Element.remove(child.getKey());
			removeChildKeys(child);
		}
	}

	public DefaultMutableTreeNode removeElement(final Object key) {
		final DefaultMutableTreeNode node = (string2Element.remove(key));
		if (node != null) {
			removeChildKeys((Node) node);
		}
		return node;
	}
}
