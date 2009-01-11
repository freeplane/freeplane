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
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.core.extension.ExtensionArray;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.extension.IExtensionCollection;
import org.freeplane.core.filter.FilterInfo;
import org.freeplane.core.filter.IFilter;
import org.freeplane.core.modecontroller.INodeViewVisitor;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.Tools;


/**
 * This class represents a single Node of a Tree. It contains direct handles to
 * its parent and children and to its view.
 */
public class NodeModel implements MutableTreeNode {
	private static final boolean ALLOWSCHILDREN = true;
	public final static int LEFT_POSITION = -1;
	public static final String NODE_TEXT = "node_text";
	public final static int RIGHT_POSITION = 1;
	public final static int UNKNOWN_POSITION = 0;
	static public final Object UNKNOWN_PROPERTY = new Object();
	private final List<NodeModel> children;
	/**
	 * the edge which leads to this node, only root has none In future it has to
	 * hold more than one view, maybe with a Vector in which the index specifies
	 * the MapView which contains the NodeViews
	 */
	final private ExtensionArray extensions;
	final private FilterInfo filterInfo = new FilterInfo();
	protected boolean folded;
	private HistoryInformationModel historyInformation = null;
	final private NodeIconSetModel icons;
	private String id;
	private final EventListenerList listenerList = new EventListenerList();
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
		children = new LinkedList();
		extensions = new ExtensionArray();
		setText((String) userObject);
		setHistoryInformation(new HistoryInformationModel());
		this.map = map;
		icons = new NodeIconSetModel();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.modes.MindMapNode#acceptViewVisitor(freeplane.view.mindmapview
	 * .INodeViewVisitor)
	 */
	public void acceptViewVisitor(final INodeViewVisitor visitor) {
		final Iterator<INodeView> iterator = views.iterator();
		while (iterator.hasNext()) {
			visitor.visit(iterator.next());
		}
	}

	public boolean addExtension(final Class clazz, final IExtension extension) {
		return extensions.addExtension(clazz, extension);
	}

	public boolean addExtension(final IExtension extension) {
		return extensions.addExtension(extension);
	}

	public void addIcon(final MindIcon _icon, final int position) {
		icons.addIcon(_icon, position);
		getMap().getIconRegistry().addIcon(_icon);
	}

	/** Recursive Method for getPath() */
	private void addToPathVector(final Vector pathVector) {
		pathVector.add(0, this);
		if (parent != null) {
			(parent).addToPathVector(pathVector);
		}
	}

	public void addTreeModelListener(final TreeModelListener l) {
		listenerList.add(TreeModelListener.class, l);
	}

	public void addViewer(final INodeView viewer) {
		getViewers().add(viewer);
		addTreeModelListener(viewer);
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

	public boolean containsExtension(final Class clazz) {
		return extensions.containsExtension(clazz);
	}

	public boolean containsExtension(final IExtension extension) {
		return extensions.containsExtension(extension);
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

	public Iterator extensionIterator() {
		return extensions.extensionIterator();
	};

	public Iterator extensionIterator(final Class clazz) {
		return extensions.extensionIterator(clazz);
	}

	public boolean getAllowsChildren() {
		return NodeModel.ALLOWSCHILDREN;
	}

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

	public IExtension getExtension(final Class clazz) {
		return extensions.getExtension(clazz);
	}

	public IExtensionCollection getExtensions() {
		return extensions;
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

	public List getIcons() {
		return icons.getIcons();
	}

	public String getID() {
		return id;
	}

	public int getIndex(final TreeNode node) {
		return children.indexOf(node);
	}

	public EventListenerList getListeners() {
		return listenerList;
	}

	public MapModel getMap() {
		return map;
	}

	public ModeController getModeController() {
		return map.getModeController();
	}

	public int getNodeLevel() {
		int level = 0;
		NodeModel parent;
		for (parent = this; !parent.isRoot(); parent = parent.getParentNode()) {
			if (parent.isVisible()) {
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

	/** Creates the TreePath recursively */
	public TreePath getPath() {
		final Vector pathVector = new Vector();
		TreePath treePath;
		this.addToPathVector(pathVector);
		treePath = new TreePath(pathVector.toArray());
		return treePath;
	}

	public String getPlainTextContent() {
		return HtmlTools.htmlToPlain(getText());
	}

	public String getShortText(final ModeController controller) {
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

	/**
	 * True iff one of node's <i>strict</i> descendants is folded. A node N is
	 * not its strict descendant - the fact that node itself is folded is not
	 * sufficient to return true.
	 */
	public boolean hasFoldedStrictDescendant() {
		for (final ListIterator e = getModeController().getMapController().childrenUnfolded(this); e
		    .hasNext();) {
			final NodeModel child = (NodeModel) e.next();
			if (child.getModeController().getMapController().isFolded(child)
			        || child.hasFoldedStrictDescendant()) {
				return true;
			}
		}
		return false;
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
		final IFilter filter = getMap().getFilter();
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
		node.setParent(null);
		children.remove(node);
	}

	public IExtension removeExtension(final Class clazz) {
		return extensions.removeExtension(clazz);
	}

	public boolean removeExtension(final IExtension extension) {
		return extensions.removeExtension(extension);
	}

	public void removeFromParent() {
		parent.remove(this);
	}

	public int removeIcon(final int position) {
		return icons.removeIcon(position);
	}

	public void removeTreeModelListener(final TreeModelListener l) {
		listenerList.remove(TreeModelListener.class, l);
	}

	public void removeViewer(final INodeView viewer) {
		getViewers().remove(viewer);
		removeTreeModelListener(viewer);
	}

	public void setExtension(final Class clazz, final IExtension extension) {
		extensions.setExtension(clazz, extension);
	}

	public void setExtension(final IExtension extension) {
		extensions.setExtension(extension);
	}

	public void setFolded(final boolean folded) {
		final EncryptionModel encryptionModel = EncryptionModel.getModel(this);
		if (encryptionModel != null && !encryptionModel.isAccessible()) {
			this.folded = true;
			return;
		}
		this.folded = folded;
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
		if (icon != null) {
			getMap().getIconRegistry().addIcon(MindIcon.factory(key, icon));
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
		this.text = Tools.makeValidXml(text);
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
		xmlText = Tools.makeValidXml(pXmlText);
		text = HtmlTools.getInstance().toHtml(xmlText);
	}

	@Override
	public String toString() {
		return getText();
	}
}
