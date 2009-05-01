/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.extension.SmallExtensionMap;
import org.freeplane.core.filter.Filter;
import org.freeplane.core.filter.FilterInfo;
import org.freeplane.core.modecontroller.INodeViewVisitor;
import org.freeplane.core.modecontroller.NodeChangeEvent;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.XmlTool;

/**
 * This class represents a single Node of a Tree. It contains direct handles to
 * its parent and children and to its view.
 */
// TODO ARCH rladstaetter 21.03.2009 rename to node
public class NodeModel implements MutableTreeNode {
	public enum NodeChangeType {
		FOLDING, REFRESH
	}

	private static final boolean ALLOWSCHILDREN = true;
	public final static int LEFT_POSITION = -1;
	public static final String NODE_TEXT = "node_text";
	public final static int RIGHT_POSITION = 1;
	public final static int UNKNOWN_POSITION = 0;
	static public final Object UNKNOWN_PROPERTY = new Object();
	protected final List<NodeModel> children = new ArrayList<NodeModel>();
	private final ExtensionContainer extensionContainer;
	final private FilterInfo filterInfo = new FilterInfo();
	protected boolean folded;
	private HistoryInformationModel historyInformation = null;
	final private NodeIconSetModel icons;
	private String id;
	private MapModel map = null;
	private NodeModel parent;
	private int position = NodeModel.UNKNOWN_POSITION;
	private NodeModel preferredChild;
	private String text = "no text";
	private TreeMap toolTip = null;
	private Collection<INodeView> views = null;
	private String xmlText = "no text";

	public NodeModel(final MapModel map) {
		this(null, map);
	}

	public NodeModel(final Object userObject, final MapModel map) {
		extensionContainer = new ExtensionContainer(new SmallExtensionMap());
		setText((String) userObject);
		setHistoryInformation(new HistoryInformationModel());
		this.map = map;
		icons = new NodeIconSetModel();
	}

	public void acceptViewVisitor(final INodeViewVisitor visitor) {
		final Iterator<INodeView> iterator = views.iterator();
		while (iterator.hasNext()) {
			visitor.visit(iterator.next());
		}
	}

	public void addExtension(final IExtension extension) {
		extensionContainer.addExtension(extension);
	}

	public void addIcon(final MindIcon _icon, final int position) {
		icons.addIcon(_icon, position);
		getMap().getIconRegistry().addIcon(_icon);
	}

	public void addViewer(final INodeView viewer) {
		getViewers().add(viewer);
	}

	public boolean areViewsEmpty() {
		return views == null || views.isEmpty();
	}

	public Enumeration children() {
		final Iterator i = children.iterator();
		return new Enumeration() {
			public boolean hasMoreElements() {
				return i.hasNext();
			}

			public Object nextElement() {
				return i.next();
			}
		};
	}

	public boolean containsExtension(final Class<? extends IExtension> clazz) {
		return extensionContainer.containsExtension(clazz);
	}

	public String createID() {
		if (id == null) {
			id = getMap().registryNode(this);
		}
		return id;
	}

	private void createToolTip() {
		if (toolTip == null) {
			toolTip = new TreeMap();
		}
	}

	public void fireNodeChanged(final NodeChangeEvent nodeChangeEvent) {
		if (views == null) {
			return;
		}
		final Iterator<INodeView> iterator = views.iterator();
		while (iterator.hasNext()) {
			iterator.next().nodeChanged(nodeChangeEvent);
		}
	}

	private void fireNodeInserted(final NodeModel child, final int index) {
		if (views == null) {
			return;
		}
		final Iterator<INodeView> iterator = views.iterator();
		while (iterator.hasNext()) {
			iterator.next().onNodeInserted(this, child, index);
		}
	}

	private void fireNodeRemoved(final NodeModel child, final int index) {
		if (views == null) {
			return;
		}
		final Iterator<INodeView> iterator = views.iterator();
		while (iterator.hasNext()) {
			iterator.next().onNodeDeleted(this, child, index);
		}
	}

	public boolean getAllowsChildren() {
		return NodeModel.ALLOWSCHILDREN;
	};

	public TreeNode getChildAt(final int childIndex) {
		return children.get(childIndex);
	}

	public int getChildCount() {
		if (children == null) {
			return 0;
		}
		final EncryptionModel encryptionModel = EncryptionModel.getModel(this);
		return encryptionModel == null || encryptionModel.isAccessible() ? children.size() : 0;
	}

	public int getChildPosition(final NodeModel childNode) {
		int position = 0;
		for (final ListIterator i = children.listIterator(); i.hasNext(); ++position) {
			if (((NodeModel) i.next()) == childNode) {
				return position;
			}
		}
		return -1;
	}

	public List<NodeModel> getChildren() {
		return Collections.unmodifiableList((children != null) ? children : Collections.EMPTY_LIST);
	}

	public IExtension getExtension(final Class<? extends IExtension> clazz) {
		return extensionContainer.getExtension(clazz);
	}

	public Map<Class<? extends IExtension>, IExtension> getExtensions() {
		return extensionContainer.getExtensions();
	};

	public FilterInfo getFilterInfo() {
		return filterInfo;
	}

	public HistoryInformationModel getHistoryInformation() {
		return historyInformation;
	}

	public MindIcon getIcon(final int position) {
		return icons.getIcon(position);
	}

	public List<MindIcon> getIcons() {
		return icons.getIcons();
	}

	public String getID() {
		return id;
	}

	public int getIndex(final TreeNode node) {
		return children.indexOf(node);
	}

	public MapModel getMap() {
		return map;
	}

	public int getNodeLevel(final boolean countHidden) {
		int level = 0;
		NodeModel parent;
		for (parent = this; !parent.isRoot(); parent = parent.getParentNode()) {
			if (countHidden || parent.isVisible()) {
				level++;
			}
		}
		return level;
	}

	public TreeNode getParent() {
		return parent;
	}

	public NodeModel getParentNode() {
		return parent;
	}

	public NodeModel[] getPathToRoot() {
		int i = getNodeLevel(true);
		final NodeModel[] path = new NodeModel[i + 1];
		NodeModel node = this;
		while (i >= 0) {
			path[i--] = node;
			node = node.getParentNode();
		}
		return path;
	}

	public String getPlainTextContent() {
		return HtmlTools.htmlToPlain(getText());
	}

	public String getShortText() {
		String adaptedText = getPlainTextContent();
		if (adaptedText.length() > 40) {
			adaptedText = adaptedText.substring(0, 40) + " ...";
		}
		return adaptedText;
	}

	public Map getStateIcons() {
		return icons.getStateIcons();
	}

	public String getText() {
		String string = "";
		if (text != null) {
			string = text.toString();
		}
		return string;
	}

	/**
	 */
	public SortedMap getToolTip() {
		if (toolTip == null) {
			return new TreeMap();
		};
		return Collections.unmodifiableSortedMap(toolTip);
	}

	public Collection<INodeView> getViewers() {
		if (views == null) {
			views = new LinkedList();
		}
		return views;
	}

	public final String getXmlText() {
		return xmlText;
	}

	public boolean hasChildren() {
		return getChildCount() != 0;
	}

	public boolean hasID() {
		return id != null;
	}

	public void insert(final MutableTreeNode child, int index) {
		final EncryptionModel encryptionModel = EncryptionModel.getModel(this);
		if (encryptionModel != null && !encryptionModel.isAccessible()) {
			throw new IllegalArgumentException("Trying to insert nodes into a ciphered node.");
		}
		final NodeModel childNode = (NodeModel) child;
		if (index < 0) {
			index = getChildCount();
			children.add(index, (NodeModel) child);
		}
		else {
			children.add(index, (NodeModel) child);
			preferredChild = childNode;
		}
		child.setParent(this);
		fireNodeInserted(childNode, getIndex(child));
	}

	/**
	 * Returns whether the argument is parent or parent of one of the grandpa's
	 * of this node. (transitive)
	 */
	public boolean isDescendantOf(final NodeModel node) {
		if (this.isRoot()) {
			return false;
		}
		else if (node == getParentNode()) {
			return true;
		}
		else {
			return getParentNode().isDescendantOf(node);
		}
	}

	public boolean isFolded() {
		return folded;
	}

	/*
	 * Notes
	 */
	public boolean isLeaf() {
		return getChildCount() == 0;
	}

	public boolean isLeft() {
		if (position == NodeModel.UNKNOWN_POSITION && !isRoot()) {
			setLeft(getParentNode().isLeft());
		}
		return position == NodeModel.LEFT_POSITION;
	}

	public boolean isNewChildLeft() {
		if (!isRoot()) {
			return isLeft();
		}
		int rightChildrenCount = 0;
		for (int i = 0; i < getChildCount(); i++) {
			if (!((NodeModel) getChildAt(i)).isLeft()) {
				rightChildrenCount++;
			}
			if (rightChildrenCount > getChildCount() / 2) {
				return true;
			}
		}
		return false;
	}

	public boolean isRoot() {
		return (parent == null);
	}

	public boolean isVisible() {
		final Filter filter = getMap().getFilter();
		return filter == null || filter.isVisible(this);
	}

	public void remove(final int index) {
		final MutableTreeNode node = children.get(index);
		remove(node);
	}

	public void remove(final MutableTreeNode node) {
		if (node == preferredChild) {
			final int index = children.indexOf(node);
			if (children.size() > index + 1) {
				preferredChild = (children.get(index + 1));
			}
			else {
				preferredChild = (index > 0) ? (NodeModel) (children.get(index - 1)) : null;
			}
		}
		final int index = getIndex(node);
		node.setParent(null);
		children.remove(node);
		fireNodeRemoved((NodeModel) node, index);
	}

	public IExtension removeExtension(final Class<? extends IExtension> clazz) {
		return extensionContainer.removeExtension(clazz);
	}

	public boolean removeExtension(final IExtension extension) {
		return extensionContainer.removeExtension(extension);
	}

	public void removeFromParent() {
		parent.remove(this);
	}

	public int removeIcon(final int position) {
		return icons.removeIcon(position);
	}

	public void removeViewer(final INodeView viewer) {
		getViewers().remove(viewer);
	}

	public void setFolded(final boolean folded) {
		if (this.folded == folded) {
			return;
		}
		final EncryptionModel encryptionModel = EncryptionModel.getModel(this);
		if (encryptionModel != null && !encryptionModel.isAccessible()) {
			this.folded = true;
			return;
		}
		this.folded = folded;
		fireNodeChanged(new NodeChangeEvent(this, NodeChangeType.FOLDING, Boolean.valueOf(!folded), Boolean
		    .valueOf(folded)));
	}

	public void setHistoryInformation(final HistoryInformationModel historyInformation) {
		this.historyInformation = historyInformation;
	}

	public void setID(final String value) {
		id = value;
		getMap().registryID(value, this);
	}

	public void setLeft(final boolean isLeft) {
		position = isLeft ? NodeModel.LEFT_POSITION : NodeModel.RIGHT_POSITION;
		if (!isRoot()) {
			for (int i = 0; i < getChildCount(); i++) {
				final NodeModel child = (NodeModel) getChildAt(i);
				child.position = position;
			}
		}
	}

	/**
	 */
	public void setMap(final MapModel map) {
		this.map = map;
	}

	public void setParent(final MutableTreeNode newParent) {
		parent = (NodeModel) newParent;
	}

	public void setParent(final NodeModel newParent) {
		parent = newParent;
	}

	public void setStateIcon(final String key, final ImageIcon icon) {
		icons.setStateIcon(key, icon);
		if (icon != null && map != null) {
			map.getIconRegistry().addIcon(MindIcon.factory(key, icon));
		}
	}

	public void setText(final Object object) {
		setText((String) object);
	}

	public final void setText(final String text) {
		if (text == null) {
			this.text = null;
			xmlText = null;
			return;
		}
		this.text = XmlTool.makeValidXml(text);
		xmlText = HtmlTools.getInstance().toXhtml(text);
	}

	/**
	 */
	public void setToolTip(final String key, final String string) {
		createToolTip();
		if (string == null) {
			if (toolTip.containsKey(key)) {
				toolTip.remove(key);
			}
			if (toolTip.size() == 0) {
				toolTip = null;
			}
		}
		else {
			toolTip.put(key, string);
		}
	}

	public void setUserObject(final Object object) {
		setText(object.toString());
	}

	public final void setXmlText(final String pXmlText) {
		xmlText = XmlTool.makeValidXml(pXmlText);
		text = HtmlTools.getInstance().toHtml(xmlText);
	}

	@Override
	public String toString() {
		return getText();
	}
}
