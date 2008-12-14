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
package org.freeplane.map.tree;

import java.awt.Color;
import java.awt.Font;
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

import org.freeplane.extension.ExtensionArray;
import org.freeplane.extension.IExtension;
import org.freeplane.extension.IExtensionCollection;
import org.freeplane.main.HtmlTools;
import org.freeplane.main.Tools;
import org.freeplane.map.attribute.Attribute;
import org.freeplane.map.attribute.NodeAttributeTableModel;
import org.freeplane.map.cloud.CloudModel;
import org.freeplane.map.edge.EdgeModel;
import org.freeplane.map.icon.MindIcon;
import org.freeplane.map.link.LinkModel;
import org.freeplane.map.link.NodeLinks;
import org.freeplane.map.nodelocation.LocationModel;
import org.freeplane.map.nodestyle.NodeStyleModel;
import org.freeplane.map.note.NoteModel;
import org.freeplane.map.tree.view.INodeViewVisitor;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.EncryptionModel;
import org.freeplane.service.filter.FilterInfo;
import org.freeplane.service.filter.IFilter;

/**
 * This class represents a single Node of a Tree. It contains direct handles to
 * its parent and children and to its view.
 */
public class NodeModel implements MutableTreeNode, IExtensionCollection {
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
	private final List hooks;
	final private NodeIconSetModel icons;
	private String id;
	private final EventListenerList listenerList = new EventListenerList();
	private MapModel map = null;
	private NodeModel parent;
	private int position = NodeModel.UNKNOWN_POSITION;
	private NodeModel preferredChild;
	private String text = "no text";
	private TreeMap toolTip = null;
	private Collection views = null;
	private String xmlText = "no text";

	public NodeModel(final MapModel map) {
		this(null, map);
	}

	public NodeModel(final Object userObject, final MapModel map) {
		children = new LinkedList();
		extensions = new ExtensionArray();
		setText((String) userObject);
		hooks = null;
		setHistoryInformation(new HistoryInformationModel());
		this.map = map;
		icons = new NodeIconSetModel();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.modes.MindMapNode#acceptViewVisitor(freemind.view.mindmapview
	 * .NodeViewVisitor)
	 */
	public void acceptViewVisitor(final INodeViewVisitor visitor) {
		final Iterator iterator = views.iterator();
		while (iterator.hasNext()) {
			visitor.visit((NodeView) iterator.next());
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
		getMap().getRegistry().addIcon(_icon);
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

	public void addViewer(final NodeView viewer) {
		getViewers().add(viewer);
		addTreeModelListener(viewer);
	}

	/**
	 * Correct iterative level values of children
	 */
	private void changeChildCloudIterativeLevels(final int deltaLevel) {
		for (final ListIterator e = getModeController().getMapController()
		    .childrenUnfolded(this); e.hasNext();) {
			final NodeModel childNode = (NodeModel) e.next();
			final CloudModel childCloud = childNode.getCloud();
			if (childCloud != null) {
				childCloud.changeIterativeLevel(deltaLevel);
			}
			childNode.changeChildCloudIterativeLevels(deltaLevel);
		}
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

	public void createAttributeTableModel() {
		if (getExtension(NodeAttributeTableModel.class) == null) {
			addExtension(new NodeAttributeTableModel(this));
			if (views == null) {
				return;
			}
			final Iterator iterator = views.iterator();
			while (iterator.hasNext()) {
				final NodeView view = (NodeView) iterator.next();
				view.createAttributeView();
			}
		}
	}

	public EdgeModel createEdge() {
		EdgeModel edge = (EdgeModel) getExtension(EdgeModel.class);
		if (edge == null) {
			edge = new EdgeModel();
			addExtension(edge);
		}
		return edge;
	}

	public String createID() {
		if (id == null) {
			id = getMap().registryNode(this);
		}
		return id;
	}

	public LocationModel createLocationModel() {
		LocationModel location = (LocationModel) getExtension(LocationModel.class);
		if (location == null) {
			location = new LocationModel();
			addExtension(location);
		}
		return location;
	}

	private NodeStyleModel createNodeStyleModel(final NodeModel node) {
		NodeStyleModel styleModel = (NodeStyleModel) getExtension(NodeStyleModel.class);
		if (styleModel == null) {
			styleModel = new NodeStyleModel();
			addExtension(styleModel);
		}
		return styleModel;
	}

	private void createToolTip() {
		if (toolTip == null) {
			toolTip = new TreeMap();
		}
	};

	public Iterator extensionIterator() {
		return extensions.extensionIterator();
	}

	public Iterator extensionIterator(final Class clazz) {
		return extensions.extensionIterator(clazz);
	}

	public boolean getAllowsChildren() {
		return NodeModel.ALLOWSCHILDREN;
	}

	public Attribute getAttribute(final int row) {
		return getAttributes().getAttribute(row);
	}

	public NodeAttributeTableModel getAttributes() {
		final NodeAttributeTableModel attributes = (NodeAttributeTableModel) getExtension(NodeAttributeTableModel.class);
		return attributes != null ? attributes
		        : NodeAttributeTableModel.EMTPY_ATTRIBUTES;
	}

	public int getAttributeTableLength() {
		return getAttributes().getAttributeTableLength();
	}

	public Color getBackgroundColor() {
		final NodeStyleModel styleModel = (NodeStyleModel) getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getBackgroundColor();
	}

	public TreeNode getChildAt(final int childIndex) {
		return children.get(childIndex);
	}

	public int getChildCount() {
		if (children == null) {
			return 0;
		}
		final EncryptionModel encryptionModel = getEncryptionModel();
		return encryptionModel == null || encryptionModel.isAccessible() ? children
		    .size()
		        : 0;
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
		return Collections.unmodifiableList((children != null) ? children
		        : Collections.EMPTY_LIST);
	}

	public CloudModel getCloud() {
		return (CloudModel) getExtension(CloudModel.class);
	}

	public Color getColor() {
		final NodeStyleModel styleModel = (NodeStyleModel) getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getColor();
	}

	public EdgeModel getEdge() {
		return (EdgeModel) getExtension(EdgeModel.class);
	}

	public EncryptionModel getEncryptionModel() {
		return (EncryptionModel) getExtension(EncryptionModel.class);
	}

	public IExtension getExtension(final Class clazz) {
		return extensions.getExtension(clazz);
	};

	public FilterInfo getFilterInfo() {
		return filterInfo;
	}

	public Font getFont() {
		final NodeStyleModel styleModel = (NodeStyleModel) getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getFont();
	}

	public HistoryInformationModel getHistoryInformation() {
		return historyInformation;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.modes.MindMapNode#getHooks()
	 */
	public List getHooks() {
		if (hooks == null) {
			return Collections.EMPTY_LIST;
		}
		return Collections.unmodifiableList(hooks);
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

	public String getLink() {
		return getModeController().getLinkController().getLink(this);
	}

	/**
	 * @return
	 */
	public Collection<LinkModel> getLinks() {
		return NodeLinks.getLinks(this);
	}

	public EventListenerList getListeners() {
		return listenerList;
	}

	public LocationModel getLocationModel() {
		final LocationModel location = (LocationModel) getExtension(LocationModel.class);
		return location != null ? location : LocationModel.NULL_LOCATION;
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

	public final String getNoteText() {
		final NoteModel extension = (NoteModel) getExtension(NoteModel.class);
		return extension != null ? extension.getNoteText() : null;
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

	public String getShape() {
		final NodeStyleModel styleModel = (NodeStyleModel) getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getShape();
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

	public Collection getViewers() {
		if (views == null) {
			views = new LinkedList();
		}
		return views;
	}

	public final String getXmlNoteText() {
		final NoteModel extension = (NoteModel) getExtension(NoteModel.class);
		return extension != null ? extension.getXmlNoteText() : null;
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
		for (final ListIterator e = getModeController().getMapController()
		    .childrenUnfolded(this); e.hasNext();) {
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
		final EncryptionModel encryptionModel = getEncryptionModel();
		if (encryptionModel != null && !encryptionModel.isAccessible()) {
			throw new IllegalArgumentException(
			    "Trying to insert nodes into a ciphered node.");
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
				preferredChild = (index > 0) ? (NodeModel) (children
				    .get(index - 1)) : null;
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

	public void removeViewer(final NodeView viewer) {
		getViewers().remove(viewer);
		removeTreeModelListener(viewer);
	}

	public void setBackgroundColor(final Color color) {
		final NodeStyleModel styleModel = createNodeStyleModel(this);
		styleModel.setBackgroundColor(color);
	}

	public void setCloud(final CloudModel cloud) {
		final CloudModel oldCloud = getCloud();
		if (cloud != null && oldCloud == null) {
			changeChildCloudIterativeLevels(1);
			addExtension(cloud);
		}
		else if (cloud == null && oldCloud != null) {
			changeChildCloudIterativeLevels(-1);
			removeExtension(CloudModel.class);
		}
	}

	public void setColor(final Color color) {
		final NodeStyleModel styleModel = createNodeStyleModel(this);
		styleModel.setColor(color);
	}

	public void setEdge(final EdgeModel edge) {
		setExtension(edge);
	}

	public void setExtension(final Class clazz, final IExtension extension) {
		extensions.setExtension(clazz, extension);
	}

	public void setExtension(final IExtension extension) {
		extensions.setExtension(extension);
	}

	public void setFolded(final boolean folded) {
		final EncryptionModel encryptionModel = getEncryptionModel();
		if (encryptionModel != null && !encryptionModel.isAccessible()) {
			this.folded = true;
			return;
		}
		this.folded = folded;
	}

	public void setFont(final Font font) {
		final NodeStyleModel styleModel = createNodeStyleModel(this);
		styleModel.setFont(font);
	}

	public void setHistoryInformation(
	                                  final HistoryInformationModel historyInformation) {
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
		map.getRegistry().registrySubtree(this);
	}

	public void setParent(final MutableTreeNode newParent) {
		parent = (NodeModel) newParent;
	}

	public void setParent(final NodeModel newParent) {
		parent = newParent;
	}

	public void setShape(final String shape) {
		final NodeStyleModel styleModel = createNodeStyleModel(this);
		styleModel.setShape(shape);
	}

	public void setStateIcon(final String key, final ImageIcon icon) {
		icons.setStateIcon(key, icon);
		if (icon != null) {
			getMap().getRegistry().addIcon(MindIcon.factory(key, icon));
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
