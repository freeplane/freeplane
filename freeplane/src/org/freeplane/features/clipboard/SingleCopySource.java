/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.clipboard;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.filter.FilterInfo;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.map.HistoryInformationModel;
import org.freeplane.features.map.INodeView;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.ui.INodeViewVisitor;

/**
 * @author Dimitry Polivaev
 * 03.02.2013
 */
class SingleCopySource extends NodeModel {

	private static final String METHOD_NOT_SUPPORTED = "method not supported";
	private final NodeModel delegate;
	
	protected List<NodeModel> getChildrenInternal() {
	    return Collections.emptyList();
    }
	
	protected void init(final Object userObject) {
    }

	public SingleCopySource(NodeModel delegate) {
	    super(null);
	    this.delegate =delegate;
    }
	
	public int hashCode() {
	    return super.hashCode();
    }

	public Object getUserObject() {
	    return delegate.getUserObject();
    }

	public boolean equals(Object obj) {
	    return super.equals(obj);
    }

	public void acceptViewVisitor(INodeViewVisitor visitor) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void addExtension(IExtension extension) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public IExtension putExtension(IExtension extension) {
	   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void addIcon(MindIcon icon) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void addIcon(MindIcon icon, int position) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void addViewer(INodeView viewer) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public boolean areViewsEmpty() {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public Enumeration<NodeModel> children() {
	    return super.children();
    }

	public boolean containsExtension(Class<? extends IExtension> clazz) {
	    return delegate.containsExtension(clazz);
    }

	public String createID() {
	    return delegate.createID();
    }

	public void fireNodeChanged(NodeChangeEvent nodeChangeEvent) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public boolean getAllowsChildren() {
	    return delegate.getAllowsChildren();
    }

	public TreeNode getChildAt(int childIndex) {
	    return  super.getChildAt(childIndex);
    }

	public int getChildCount() {
	    return super.getChildCount();
    }

	public int getChildPosition(NodeModel childNode) {
	    return super.getChildPosition(childNode);
    }

	public List<NodeModel> getChildren() {
	    return super.getChildren();
    }

	public <T extends IExtension> T getExtension(Class<T> clazz) {
	    return delegate.getExtension(clazz);
    }

	public Map<Class<? extends IExtension>, IExtension> getExtensions() {
	    return delegate.getExtensions();
    }

	public FilterInfo getFilterInfo() {
	    return delegate.getFilterInfo();
    }

	public HistoryInformationModel getHistoryInformation() {
	    return delegate.getHistoryInformation();
    }

	public MindIcon getIcon(int position) {
	    return delegate.getIcon(position);
    }

	public List<MindIcon> getIcons() {
	    return delegate.getIcons();
    }

	public String getID() {
	    return delegate.getID();
    }

	public int getIndex(TreeNode node) {
	    return super.getIndex(node);
    }

	public MapModel getMap() {
	    return delegate.getMap();
    }

	public int getNodeLevel(boolean countHidden) {
	    return delegate.getNodeLevel(countHidden);
    }

	public TreeNode getParent() {
	    return delegate.getParent();
    }

	public NodeModel getParentNode() {
	    return delegate.getParentNode();
    }

	public NodeModel[] getPathToRoot() {
	    return delegate.getPathToRoot();
    }

	public String getText() {
	    return delegate.getText();
    }

	public Collection<INodeView> getViewers() {
	    return delegate.getViewers();
    }

	public boolean hasChildren() {
	    return false;
    }

	public boolean hasID() {
	    return delegate.hasID();
    }

	public void insert(MutableTreeNode child, int index) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public boolean isDescendantOf(NodeModel node) {
	    return delegate.isDescendantOf(node);
    }

	public boolean isFolded() {
	    return false;
    }

	public boolean isLeaf() {
	    return delegate.isLeaf();
    }

	public boolean isLeft() {
	    return delegate.isLeft();
    }

	public boolean isNewChildLeft() {
	    return delegate.isNewChildLeft();
    }

	public boolean isRoot() {
	    return delegate.isRoot();
    }

	public boolean hasVisibleContent() {
	    return delegate.hasVisibleContent();
    }

	public void remove(int index) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void remove(MutableTreeNode node) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public <T extends IExtension> T removeExtension(Class<T> clazz) {
		throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public boolean removeExtension(IExtension extension) {
		throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void removeFromParent() {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public int removeIcon() {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public int removeIcon(int position) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void removeViewer(INodeView viewer) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void setFolded(boolean folded) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void setHistoryInformation(HistoryInformationModel historyInformation) {
		throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void setID(String value) {
		throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void setLeft(boolean isLeft) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void setMap(MapModel map) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void setParent(MutableTreeNode newParent) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void setParent(NodeModel newParent) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public String toString() {
	    return delegate.toString();
    }

	public int depth() {
	    return delegate.depth();
    }

	public void insert(NodeModel newNodeModel) {
	   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public NodeModel getVisibleAncestorOrSelf() {
	    return delegate.getVisibleAncestorOrSelf();
    }
	
	
}
