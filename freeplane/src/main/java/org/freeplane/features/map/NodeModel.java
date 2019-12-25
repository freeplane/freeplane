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
package org.freeplane.features.map;

import static org.freeplane.features.map.NodeModel.CloneType.CONTENT;
import static org.freeplane.features.map.NodeModel.CloneType.TREE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterInfo;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.NamedIcon;
import org.freeplane.features.ui.INodeViewVisitor;

/**
 * This class represents a single Node of a Tree. It contains direct handles to
 * its parent and children and to its view.
 *
 * Note that this class does not and must not know anything about its extensions,
 * otherwise this class would become too big.
 * Extension methods that add functionality to nodes are in the extension packages
 * and get NodeModel as an argument.
 */
public class NodeModel{
	public enum NodeChangeType {
		FOLDING, REFRESH
	}

	public enum CloneType{TREE, CONTENT}
	final static int TREE_CLONE_INDEX = CloneType.TREE.ordinal();
	final static int CONTENT_CLONE_INDEX = CloneType.CONTENT.ordinal();

	private static final boolean ALLOWSCHILDREN = true;
	public final static int LEFT_POSITION = -1;
	public static final String NODE_TEXT = "node_text";
	public static final String NOTE_TEXT = "note_text";
	public final static int RIGHT_POSITION = 1;
	public final static int UNKNOWN_POSITION = 0;
	static public final Object UNKNOWN_PROPERTY = new Object();
	public static final String NODE_ICON = "icon";
	public static final String NODE_ICON_SIZE = "icon_size";
	static public final Object HYPERLINK_CHANGED = "hyperlink_changed";

	private List<NodeModel> children;
	private NodeModel parent;
	final private FilterInfo filterInfo;
	private String id;
	private MapModel map = null;
	private int position = NodeModel.UNKNOWN_POSITION;
	private NodeModel preferredChild;
	private Collection<INodeView> views = null;

	private SharedNodeData sharedData;
	private Clones[] clones;

	void setClones(Clones clones) {
		this.clones[clones.getCloneType().ordinal()] = clones;
		for(NodeModel clone : clones)
			clone.fireNodeChanged(new NodeChangeEvent(this, NodeModel.UNKNOWN_PROPERTY, null, null, false, false));
	}

	public Object getUserObject() {
		return sharedData.getUserObject();
	}

	public NodeModel(final MapModel map) {
		this("", map);
	}

	public NodeModel(final Object userObject, final MapModel map) {
		this.map = map;
		children = new ArrayList<NodeModel>();
		sharedData = new SharedNodeData();
		init(userObject);
		filterInfo = new FilterInfo();
		clones = new Clones[]{new DetachedNodeList(this, TREE), new DetachedNodeList(this, CONTENT)};
	}

	private NodeModel(NodeModel toBeCloned, CloneType cloneType){
		this.map = toBeCloned.map;
		this.sharedData = toBeCloned.sharedData;
		children = new ArrayList<NodeModel>();
		filterInfo = new FilterInfo();
		clones = new Clones[]{new DetachedNodeList(this, cloneType == TREE ? toBeCloned : this, TREE), new DetachedNodeList(this, toBeCloned, CONTENT)};
	}

	protected void init(final Object userObject) {
	    setUserObject(userObject);
		setHistoryInformation(new HistoryInformationModel());
    }

	public void acceptViewVisitor(final INodeViewVisitor visitor) {
		if (views == null) {
			return;
		}
		for (final INodeView view : views) {
			visitor.visit(view);
		}
	}

	public void addExtension(final IExtension extension) {
		getExtensionContainer().addExtension(extension);
	}

	public IExtension putExtension(final IExtension extension) {
		return getExtensionContainer().putExtension(extension);
	}

	public IExtension putExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
		return getExtensionContainer().putExtension(clazz, extension);
	}

	public void addIcon(final NamedIcon icon) {
		getIconModel().addIcon(icon);
		if (map != null) {
			map.getIconRegistry().addIcon(icon);
		}
	}

	public void addIcon(final NamedIcon icon, final int position) {
		getIconModel().addIcon(icon, position);
		getMap().getIconRegistry().addIcon(icon);
	}

	public void addViewer(final INodeView viewer) {
		getViewers().add(viewer);
	}

	public boolean areViewsEmpty() {
		return views == null || views.isEmpty();
	}

	protected List<NodeModel> getChildrenInternal() {
	    return children;
    }

	protected void setChildrenInternal(List<NodeModel> chidren) {
	    this.children = chidren;
    }

	public Enumeration<NodeModel> children() {
		final Iterator<NodeModel> i = getChildrenInternal().iterator();
		return new Enumeration<NodeModel>() {
			@Override
			public boolean hasMoreElements() {
				return i.hasNext();
			}

			@Override
			public NodeModel nextElement() {
				return i.next();
			}
		};
	}

	public boolean containsExtension(final Class<? extends IExtension> clazz) {
		return getExtensionContainer().containsExtension(clazz);
	}

	public String createID() {
		if (id == null) {
			id = getMap().registryNode(this);
		}
		return id;
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
		final NodeDeletionEvent nodeDeletionEvent = new NodeDeletionEvent(this, child, index);
		while (iterator.hasNext()) {
			iterator.next().onNodeDeleted(nodeDeletionEvent);
		}
	}

	public boolean getAllowsChildren() {
		return NodeModel.ALLOWSCHILDREN;
	};

	public NodeModel getChildAt(final int childIndex) {
		return childIndex >= 0 ? getChildrenInternal().get(childIndex) : null;
	}

	public int getChildCount() {
		if (getChildrenInternal() == null) {
			return 0;
		}
		return getChildrenInternal().size();
	}

	public List<NodeModel> getChildren() {
		List<NodeModel> childrenList;
		if (getChildrenInternal() != null) {
			childrenList = getChildrenInternal();
		}
		else {
			childrenList = Collections.emptyList();
		}
		return Collections.unmodifiableList(childrenList);
	}

    public <T extends IExtension> T getExtension(final Class<T> clazz) {
		return getExtensionContainer().getExtension(clazz);
	}

	public Map<Class<? extends IExtension>, IExtension> getSharedExtensions() {
		return getExtensionContainer().getExtensions();
	};

	public FilterInfo getFilterInfo() {
		return filterInfo;
	}

	public HistoryInformationModel getHistoryInformation() {
		return sharedData.getHistoryInformation();
	}

	public NamedIcon getIcon(final int position) {
		return getIconModel().getIcon(position);
	}

	public List<NamedIcon> getIcons() {
		return getIconModel().getIcons();
	}

	public String getID() {
		return id;
	}

	public int getIndex(final NodeModel node) {
		return children.indexOf(node);
	}

	public MapModel getMap() {
		return map;
	}

	public int getNodeLevel(final boolean countHidden) {
		int level = 0;
		NodeModel parent;
		for (parent = getParentNode(); parent != null; parent = parent.getParentNode()) {
			if (countHidden || parent.isVisible()) {
				level++;
			}
		}
		return level;
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

	public String getText() {
		String string = "";
		if (getUserObject() != null) {
			string = getUserObject().toString();
		}
		return string;
	}

	public Collection<INodeView> getViewers() {
		if (views == null) {
			views = new LinkedList<INodeView>();
		}
		return views;
	}

	public final String getXmlText() {
		return sharedData.getXmlText();
	}

	public boolean hasChildren() {
		return getChildCount() != 0;
	}

	public boolean hasID() {
		return id != null;
	}

	public void insert(final NodeModel child, int index) {
		final NodeModel childNode = child;
		if (index < 0) {
			index = getChildCount();
			children.add(index, child);
		}
		else {
			children.add(index, child);
			preferredChild = childNode;
		}
		child.setParent(this);
		fireNodeInserted(childNode, getIndex(child));
	}

	private boolean isAccessible() {
		final EncryptionModel encryptionModel = EncryptionModel.getModel(this);
		return encryptionModel == null || encryptionModel.isAccessible();
	}

	/**
	 * Returns whether the argument is parent or parent of one of the grandpa's
	 * of this node. (transitive)
	 */
	public boolean isDescendantOf(final NodeModel node) {
		if (parent == null) {
			return false;
		}
		else if (node == parent) {
			return true;
		}
		else {
			return parent.isDescendantOf(node);
		}
	}

	public boolean isFolded() {
		return sharedData.isFolded() && isAccessible();
	}

	/*
	 * Notes
	 */
	public boolean isLeaf() {
		return getChildCount() == 0;
	}

	public boolean isLeft() {
		if (position == NodeModel.UNKNOWN_POSITION && getParentNode() != null) {
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
			if (!getChildAt(i).isLeft()) {
				rightChildrenCount++;
			}
			if (rightChildrenCount > getChildCount() / 2) {
				return true;
			}
		}
		return false;
	}

	public boolean isRoot() {
		return getMap().getRootNode() == this;
	}

	public boolean hasVisibleContent() {
		return ! isHiddenSummary() && satisfiesFilter();
	}

	private boolean satisfiesFilter() {
		final Filter filter = getMap().getFilter();
		return (filter == null || filter.isVisible(this));
	}

	public boolean isHiddenSummary() {
		return SummaryNode.isHidden(this);
	}

	public boolean isVisible() {
		return isHiddenSummary() || satisfiesFilter();
	}

	public void remove(final int index) {
	    final NodeModel child = children.get(index);
		if (child == preferredChild) {
			if (getChildrenInternal().size() > index + 1) {
				preferredChild = (getChildrenInternal().get(index + 1));
			}
			else {
				preferredChild = (index > 0) ? (NodeModel) (getChildrenInternal().get(index - 1)) : null;
			}
		}
		child.setParent(null);
		children.remove(index);
		fireNodeRemoved(child, index);
    }

	public <T extends IExtension> T removeExtension(final Class<T> clazz){
		return getExtensionContainer().removeExtension(clazz);
	}

	public boolean removeExtension(final IExtension extension) {
		return getExtensionContainer().removeExtension(extension);
	}

	/**
	 * remove last icon
	 *
	 * @return the number of remaining icons.
	 */
	public int removeIcon() {
		return getIconModel().removeIcon();
	}

	/**
	 * @param remove icons with given position
	 *
	 * @return the number of remaining icons
	 */
	public int removeIcon(final int position) {
		return getIconModel().removeIcon(position);
	}

	public void removeViewer(final INodeView viewer) {
		getViewers().remove(viewer);
	}

	public void setFolded(boolean folded) {
		boolean wasFolded = isFolded();
		if (wasFolded != folded && isAccessible()) {
			sharedData.setFolded(folded && ! AlwaysUnfoldedNode.isAlwaysUnfolded(this));
		}
		fireNodeChanged(new NodeChangeEvent(this, NodeChangeType.FOLDING, Boolean.valueOf(wasFolded), Boolean.valueOf(folded), false, false));
	}

	public void setHistoryInformation(final HistoryInformationModel historyInformation) {
		this.sharedData.setHistoryInformation(historyInformation);
	}

	public void setID(final String value) {
		id = value;
		getMap().registryID(value, this);
	}

	public void setLeft(final boolean isLeft) {
		position = isLeft ? NodeModel.LEFT_POSITION : NodeModel.RIGHT_POSITION;
		if (!isRoot()) {
			for (final NodeModel child : children) {
				if (child.position != position) {
					child.setLeft(isLeft);
				}
			}
		}
	}

	/**
	 */
	public void setMap(final MapModel map) {
		this.map = map;
		for (final NodeModel child : children) {
			child.setMap(map);
		}
	}

	public void setParent(final NodeModel newParent) {
		if(parent == null && newParent != null && newParent.isAttached())
	        attach();
		else if(parent != null && parent.isAttached() &&  (newParent == null || !newParent.isAttached())
				|| newParent == null && isAttached())
	        detach();
		parent = newParent;
	}

	void attach() {
		attachClones();
	    for(NodeModel child : children)
	    	child.attach();
    }

	private void attachClones() {
		for(Clones clonesGroup : clones)
			clonesGroup.attach();
	}

	private void detach() {
		detachClones();
	    for(NodeModel child : children)
	    	child.detach();
    }

	private void detachClones() {
		for(Clones clonesGroup : clones)
			clonesGroup.detach(this);
	}


	boolean isAttached() {
	    return clones[0].size() != 0;
    }

	public final void setText(final String text) {
		sharedData.setText(text);
	}

	public final void setUserObject(final Object data) {
		sharedData.setUserObject(data);
	}

	public final void setXmlText(final String pXmlText) {
		sharedData.setXmlText(pXmlText);
	}

	@Override
	public String toString() {
		return HtmlUtils.htmlToPlain(getText());
	}

	public int depth() {
		final NodeModel parentNode = getParentNode();
		if (parentNode == null) {
			return 0;
		}
		return parentNode.depth() + 1;
	}

	public void insert(final NodeModel newNodeModel) {
		insert(newNodeModel, getChildCount());
	}

	public NodeModel getVisibleAncestorOrSelf() {
		NodeModel node = this;
		while (!node.hasVisibleContent()) {
			node = node.getParentNode();
		}
		return node;
	}

	private ExtensionContainer getExtensionContainer() {
	    return sharedData.getExtensionContainer();
    }

	private NodeIconSetModel getIconModel() {
	    return sharedData.getIcons();
    }

	void fireNodeChanged(INodeChangeListener[] nodeChangeListeners, final NodeChangeEvent nodeChangeEvent) {
		for(NodeModel node : clones[CONTENT.ordinal()]){
			final NodeChangeEvent cloneEvent = nodeChangeEvent.forNode(node);
			node.fireSingleNodeChanged(nodeChangeListeners, cloneEvent);
		}
	}

	private void fireSingleNodeChanged(INodeChangeListener[] nodeChangeListeners, final NodeChangeEvent nodeChangeEvent) {
	    for (final INodeChangeListener listener : nodeChangeListeners) {
			listener.nodeChanged(nodeChangeEvent);
		}
		fireNodeChanged(nodeChangeEvent);
    }

    public NodeModel cloneTree(){
		final NodeModel clone = new Cloner(this).cloneTree();
		return clone;
	}

    public NodeModel cloneContent() {
		if(containsExtension(EncryptionModel.class))
			throw new CloneEncryptedNodeException();
		return cloneNode(CloneType.CONTENT);
	}

	protected NodeModel cloneNode(CloneType cloneType) {
	    final NodeModel clone = new NodeModel(this, cloneType);
		return clone;
    }

	public SharedNodeData getSharedData() {
	    return sharedData;
    }

	public Collection<IExtension> getIndividualExtensionValues() {
		return Collections.emptyList();
    }

	public void convertToClone(NodeModel node, CloneType cloneType) {
		sharedData = node.sharedData;
		if(cloneType == TREE)
			this.clones[TREE.ordinal()] = new DetachedNodeList(this, node, TREE);
		this.clones[CONTENT.ordinal()] = new DetachedNodeList(this, node, CONTENT);
    }

	public  Clones subtreeClones() {
	    return clones(CloneType.TREE);
    }

	public  Clones allClones() {
	    return clones(CloneType.CONTENT);
    }

	Clones clones(final CloneType cloneType) {
		return clones[cloneType.ordinal()];
	}

	public boolean subtreeContainsCloneOf(NodeModel node) {
		for(NodeModel clone : node.subtreeClones())
			if(equals(clone))
				return true;
		for(NodeModel child : children)
			if(child.subtreeContainsCloneOf(node))
				return true;
		return false;
    }

	public boolean isSubtreeCloneOf(NodeModel ancestorClone) {
	    return subtreeClones().contains(ancestorClone);
    }

	public NodeModel getSubtreeRoot() {
		if(isSubtreeRoot())
			return this;
		else
			return getParentNode().getSubtreeRoot();

    }

	private boolean isSubtreeRoot() {
	    return parent == null || isCloneTreeRoot();
    }

	public boolean isCloneTreeRoot(){
		return parent != null && parent.clones[TREE_CLONE_INDEX].size() < clones[TREE_CLONE_INDEX].size()
				|| clones[TREE_CLONE_INDEX].size() == 1 && clones[CONTENT_CLONE_INDEX].size() > 1;
	}

	public boolean isCloneTreeNode(){
		return parent != null && clones[TREE_CLONE_INDEX].size() > 1 && parent.clones[TREE_CLONE_INDEX].size() == clones[TREE_CLONE_INDEX].size();
	}

	public int nextNodeIndex(int index, final boolean leftSide) {
		return nextNodeIndex(index, leftSide, +1);
	}

	public int previousNodeIndex(int index, final boolean leftSide) {
		return nextNodeIndex(index, leftSide, -1);
	}

	private int nextNodeIndex(int index, final boolean leftSide, final int step) {
		for(int i = index  + step; i >= 0 && i < getChildCount(); i+=step){
			final NodeModel followingNode = getChildAt(i);
			if(followingNode.isLeft() == leftSide) {
				return i;
			}
		}
		return -1;
	}

	public NodeModel previousNode(int start, boolean isLeft) {
		final int previousNodeIndex = previousNodeIndex(start, isLeft);
		return parent.getChildAt(previousNodeIndex);
	}

	public int getIndex() {
		final NodeModel parentNode = getParentNode();
		return parentNode != null ? parentNode.getIndex(this) : -1;
	}

	public void swapData(NodeModel duplicate) {
		this.detachClones();
		SharedNodeData sharedDataSwap = sharedData;
		this.sharedData = duplicate.sharedData;
		duplicate.sharedData = sharedDataSwap;
		Clones[] clonesSwap = clones;
		this.clones = duplicate.clones;
		duplicate.clones = clonesSwap;
		for(CloneType cloneType : CloneType.values()) {
			final DetachedNodeList detachedClone = (DetachedNodeList) clones[cloneType.ordinal()];
			clones[cloneType.ordinal()] = detachedClone.forClone(this);
		}

		this.attachClones();
	}
}
