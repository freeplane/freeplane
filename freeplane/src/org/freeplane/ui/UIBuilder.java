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
package org.freeplane.ui;

import java.awt.Component;
import java.awt.Container;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * @author Dimitry Polivaev
 */
public abstract class UIBuilder {
	private static class Node extends DefaultMutableTreeNode {
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
	public static final int ICON_SIZE = 16;
	protected DefaultTreeModel menuStructure;
	protected HashMap string2Element;

	/**
	 *
	 */
	public UIBuilder(final Object root) {
		super();
		string2Element = new HashMap();
		final DefaultMutableTreeNode rootNode = new Node(root);
		menuStructure = new DefaultTreeModel(rootNode);
		string2Element.put(this, rootNode);
	}

	protected void addComponent(final Container container, final Component component,
	                            final int index) {
		container.add(component, index);
	}

	/**
	 */
	protected void addComponent(final DefaultMutableTreeNode childNode, final int position) {
		int index;
		final Container parentComponent = getParentComponent(childNode, Container.class);
		if (parentComponent == null) {
			return;
		}
		if (position == UIBuilder.AS_CHILD
		        && ((DefaultMutableTreeNode) childNode.getParent()).getUserObject() instanceof Container) {
			index = getParentComponentCount(parentComponent) - 1;
		}
		else {
			final Component relative = getPrevious(childNode);
			index = -1;
			if (relative != null) {
				for (int i = 0; i < getParentComponentCount(parentComponent); i++) {
					if (getChildComponent(parentComponent, i) == relative) {
						index = i;
						break;
					}
				}
			}
		}
		if (position != UIBuilder.BEFORE) {
			index++;
		}
		final Component component = (Component) childNode.getUserObject();
		addComponent(parentComponent, component, index);
	}

	protected DefaultMutableTreeNode addElement(final Object relativeKey, final Object element,
	                                            final int position) {
		final DefaultMutableTreeNode relativeNode = getNode(relativeKey);
		final DefaultMutableTreeNode node = new Node(element);
		if (relativeNode == null) {
			return node;
		}
		addNode(relativeNode, node, position);
		return node;
	}

	protected void addElement(final Object relativeKey, final Object element, final Object key,
	                          final int position) {
		final DefaultMutableTreeNode existingNode = get(key);
		if (existingNode != null) {
			throw new RuntimeException(key.toString() + " added twice");
		}
		final DefaultMutableTreeNode relativeNode = getNode(relativeKey);
		if (relativeNode == null) {
			return;
		}
		final DefaultMutableTreeNode node = new Node(element, key);
		addNode(relativeNode, node, position);
		string2Element.put(key, node);
	}

	private void addNode(final DefaultMutableTreeNode relativeNode,
	                     final DefaultMutableTreeNode node, final int position) {
		switch (position) {
			case AS_CHILD:
				relativeNode.add(node);
				break;
			case BEFORE: {
				final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) relativeNode
				    .getParent();
				if (parent == null) {
					throw new RuntimeException("relative node has no parent element");
				}
				final int index = parent.getIndex(relativeNode);
				parent.insert(node, index);
				break;
			}
			case AFTER:
				final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) relativeNode
				    .getParent();
				if (parent == null) {
					throw new RuntimeException("relative node has no parent element");
				}
				final int index = parent.getIndex(relativeNode);
				parent.insert(node, index + 1);
				break;
			default:
				throw new RuntimeException("wrong position");
		}
		if (node.getUserObject() instanceof Component) {
			addComponent(node, position);
		}
	}

	public boolean contains(final Object key) {
		return string2Element.containsKey(key);
	}

	public DefaultMutableTreeNode get(final Object key) {
		final Object object = string2Element.get(key);
		if (object == null) {
			return null;
		}
		return (DefaultMutableTreeNode) object;
	}

	protected Component getChildComponent(final Container parentComponent, final int index) {
		return parentComponent.getComponent(index);
	}

	protected Container getContainer(final DefaultMutableTreeNode node, final Class clazz) {
		if (node == null) {
			return null;
		}
		final Object userObject = node.getUserObject();
		if (clazz.isInstance(userObject)) {
			return (Container) userObject;
		}
		return getParentComponent(node, clazz);
	}

	protected DefaultMutableTreeNode getNode(final Object key) {
		final DefaultMutableTreeNode node = ((DefaultMutableTreeNode) string2Element.get(key));
		if (node == null) {
			Logger.global.warning(key + " not found");
		}
		return node;
	}

	/**
	 * @param clazz
	 */
	private Container getParentComponent(final DefaultMutableTreeNode child, final Class clazz) {
		final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) child.getParent();
		return getContainer(parent, clazz);
	}

	protected int getParentComponentCount(final Container parentComponent) {
		return parentComponent.getComponentCount();
	}

	private Component getPrevious(final DefaultMutableTreeNode childNode) {
		final DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) childNode.getParent();
		if (parentNode == null || parentNode.getUserObject() == null) {
			return null;
		}
		final int childNodeIndex = parentNode.getIndex(childNode);
		final Component c = getPrevious(parentNode, childNodeIndex - 1);
		if (c == null) {
			return getPrevious(parentNode);
		}
		return c;
	}

	/**
	 */
	private Component getPrevious(final TreeNode parentNode, final int last) {
		for (int i = last; i >= 0; i--) {
			final DefaultMutableTreeNode child = (DefaultMutableTreeNode) parentNode.getChildAt(i);
			final Object userObject = child.getUserObject();
			if (userObject instanceof Component) {
				return (Component) userObject;
			}
			final Component childComponent = getPrevious(child, child.getChildCount() - 1);
			if (childComponent != null) {
				return childComponent;
			}
		}
		return null;
	}

	/**
	 * @param parentComponent
	 */
	private void removeChildComponents(final Container parentComponent,
	                                   final DefaultMutableTreeNode node) {
		{
			final Object userObject = node.getUserObject();
			if (userObject instanceof Component) {
				final Component component = (Component) userObject;
				parentComponent.remove(component);
				return;
			}
		}
		final Enumeration children = node.children();
		while (children.hasMoreElements()) {
			final DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
			final Object userObject = child.getUserObject();
			if (userObject instanceof Component) {
				parentComponent.remove((Component) userObject);
			}
			else {
				removeChildComponents(parentComponent, child);
			}
		}
	}

	public void removeChildElements(final Object key) {
		final DefaultMutableTreeNode node = getNode(key);
		final Container parentComponent = getContainer(node, Container.class);
		final Enumeration children = node.children();
		while (children.hasMoreElements()) {
			final Node child = (Node) children.nextElement();
			final Object childKey = child.getKey();
			if (childKey != null) {
				string2Element.remove(childKey);
			}
			removeChildComponents(parentComponent, child);
		}
		node.removeAllChildren();
	}

	/**
	 */
	private void removeChildKeys(final Node node) {
		final Enumeration children = node.children();
		while (children.hasMoreElements()) {
			final Node child = (Node) children.nextElement();
			string2Element.remove(child.getKey());
			removeChildKeys(child);
		}
	}

	public void removeElement(final Object key) {
		final DefaultMutableTreeNode node = ((DefaultMutableTreeNode) string2Element.remove(key));
		if (node != null) {
			removeChildKeys((Node) node);
			final Container parentComponent = getParentComponent(node, Container.class);
			if (parentComponent == null) {
				return;
			}
			removeChildComponents(parentComponent, node);
			((DefaultMutableTreeNode) node.getParent()).remove(node);
		}
	}
}
