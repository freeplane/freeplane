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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.util.LogUtils;

/**
 * @author Dimitry Polivaev
 * 25.12.2008
 */
public class IndexedTree {
	public static class Node extends DefaultMutableTreeNode {
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

		public Object getKey() {
			return key;
		}
	}

	private final class UserObjects extends AbstractCollection<Object> {
		@Override
		public void clear() {
			string2Element.clear();
		}

		@Override
		public boolean contains(final Object o) {
			final Iterator<Object> iterator = iterator();
			while (iterator.hasNext()) {
				final Object next = iterator.next();
				if (o != null) {
					if (o.equals(next)) {
						return true;
					}
				}
				if (next == null) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Iterator<Object> iterator() {
			return newObjectIterator();
		}

		@Override
		public int size() {
			return string2Element.size();
		}
	}
	public static final int APPEND = 2;
	public static final int AFTER = 1;
	public static final int AS_CHILD = 0;
	public static final int BEFORE = -1;
	public static final int PREPEND = -2;
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

	public DefaultMutableTreeNode addElement(final Object relativeKey, final Object element, final Object key,
	                                         final int position) {
		final DefaultMutableTreeNode existingNode = get(key);
		if (existingNode != null) {
			throw new KeyAlreadyUsedException(key.toString() + " added twice");
		}
		final DefaultMutableTreeNode relativeNode = getNode(relativeKey);
		if (relativeNode == null) {
			return null;
		}
		final Node node = new Node(element, key);
		addNode(relativeNode, node, position);
		string2Element.put(key, node);
		return node;
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
			case AFTER: {
				final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) relativeNode.getParent();
				if (parent == null) {
					throw new RuntimeException("relative node has no parent element");
				}
				final int index = parent.getIndex(relativeNode);
				parent.insert(node, index + 1);
				break;
			}
			case PREPEND: { //DOCEAR - 
//				final int index = relativeNode.getChildCount()-1;
				relativeNode.insert(node, 0);
				break;
			}
			case APPEND: {							
				final int idx = relativeNode.getChildCount()-1;
				relativeNode.insert(node, idx + 1);
				break;
			}
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

	public Object getKeyByUserObject(final Object object) {
		final Collection<Node> values = string2Element.values();
		for (final Node node : values) {
			if (object != null && object.equals(node.getUserObject())) {
				return node.getKey();
			}
		}
		return null;
	}

	protected DefaultMutableTreeNode getNode(final Object key) {
		final DefaultMutableTreeNode node = (string2Element.get(key));
		if (node == null) {
			LogUtils.warn(key + " not found");
		}
		return node;
	}

	public DefaultMutableTreeNode getRoot() {
		return string2Element.get(this);
	}

	public Collection<Object> getUserObjects() {
		return Collections.unmodifiableCollection(new UserObjects());
	}

	public Iterator<Object> newObjectIterator() {
		return new Iterator<Object>() {
			private final Iterator<Node> nodeIterator = string2Element.values().iterator();

			public boolean hasNext() {
				return nodeIterator.hasNext();
			}

			public Object next() {
				return nodeIterator.next().getUserObject();
			}

			public void remove() {
				nodeIterator.remove();
			}
		};
	}

	public void removeChildElements(final Object key) {
		final DefaultMutableTreeNode node = getNode(key);
		final Enumeration<?> children = node.children();
		while (children.hasMoreElements()) {
			final Node child = (Node) children.nextElement();
			final Object childKey = child.getKey();
			if (childKey != null) {
				removeChildElements(childKey);
				string2Element.remove(childKey);
			}
		}
		node.removeAllChildren();
	}

	/**
	 */
	protected void removeChildKeys(final Node node) {
		final Enumeration<?> children = node.children();
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
