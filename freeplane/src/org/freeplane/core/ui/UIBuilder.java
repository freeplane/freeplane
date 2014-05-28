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

import java.awt.Component;
import java.awt.Container;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * @author Dimitry Polivaev
 */
public abstract class UIBuilder extends IndexedTree {
	public static final int ICON_SIZE = 16;

	/**
	 *
	 */
	public UIBuilder(final Object root) {
		super(root);
	}

	protected void addComponent(final Container container, final Component component, final int index) {
		container.add(component, index);
	}

	/**
	 */
	protected void addComponent(final DefaultMutableTreeNode childNode, final int position) {
		int index;
		Container parentComponent = getParentComponent(childNode, Container.class);
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
				ParentLoop: for (Container nextParentComponent = parentComponent; nextParentComponent != null; nextParentComponent = getNextParentComponent(nextParentComponent)) {
					parentComponent = nextParentComponent;
					for (int i = 0; i < getParentComponentCount(parentComponent); i++) {
						if (getChildComponent(parentComponent, i) == relative) {
							index = i;
							break ParentLoop;
						}
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

	protected Container getNextParentComponent(Container parentComponent) {
	    return null;
    }

	@Override
	protected void addNode(final DefaultMutableTreeNode relativeNode, final DefaultMutableTreeNode node,
	                       final int position) {
		super.addNode(relativeNode, node, position);
		if (node.getUserObject() instanceof Component) {
			addComponent(node, position);
		}
	}

	protected Component getChildComponent(final Container parentComponent, final int index) {
		return parentComponent.getComponent(index);
	}

	protected Container getContainer(final DefaultMutableTreeNode node, final Class<?> clazz) {
		if (node == null) {
			return null;
		}
		final Object userObject = node.getUserObject();
		if (clazz.isInstance(userObject)) {
			return (Container) userObject;
		}
		return getParentComponent(node, clazz);
	}

	/**
	 * @param clazz
	 */
	Container getParentComponent(final DefaultMutableTreeNode child, final Class<?> clazz) {
		final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) child.getParent();
		return getContainer(parent, clazz);
	}

	protected int getParentComponentCount(final Container parentComponent) {
		return parentComponent.getComponentCount();
	}

	protected Component getPrevious(final DefaultMutableTreeNode childNode) {
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
	protected void removeChildComponents(final Container parentComponent, final DefaultMutableTreeNode node) {
		{
			final Object userObject = node.getUserObject();
			if (userObject instanceof Component) {
				final Component component = (Component) userObject;
				parentComponent.remove(component);
				return;
			}
		}
		final Enumeration<?> children = node.children();
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

	@Override
	public void removeChildElements(final Object key) {
		final DefaultMutableTreeNode node = getNode(key);
		if(node == null) {
			return;
		}
		final Container parentComponent = getContainer(node, Container.class);
		final Enumeration<?> children = node.children();
		while (children.hasMoreElements()) {
			final Node child = (Node) children.nextElement();
			removeChildComponents(parentComponent, child);
		}
		super.removeChildElements(key);
	}

	@Override
	public DefaultMutableTreeNode removeElement(final Object key) {
		final DefaultMutableTreeNode node = super.removeElement(key);
		final Container parentComponent = getParentComponent(node, Container.class);
		if (parentComponent == null || node == null) {
			return node;
		}
		removeChildComponents(parentComponent, node);
		((DefaultMutableTreeNode) node.getParent()).remove(node);
		if (parentComponent.isShowing()) {
			parentComponent.validate();
			parentComponent.repaint();
		}
		return node;
	}
}
