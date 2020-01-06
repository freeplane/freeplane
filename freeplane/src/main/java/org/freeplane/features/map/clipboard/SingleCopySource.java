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
package org.freeplane.features.map.clipboard;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.swing.tree.MutableTreeNode;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.filter.FilterInfo;
import org.freeplane.features.icon.NamedIcon;
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

	@Override
    protected List<NodeModel> getChildrenInternal() {
	    return Collections.emptyList();
    }

	@Override
    protected void init(final Object userObject) {
    }

	public SingleCopySource(NodeModel delegate) {
	    super(null);
	    this.delegate =delegate;
    }

	@Override
    public int hashCode() {
	    return super.hashCode();
    }

	@Override
    public Object getUserObject() {
	    return delegate.getUserObject();
    }

	@Override
    public boolean equals(Object obj) {
	    return super.equals(obj);
    }

	@Override
    public void acceptViewVisitor(INodeViewVisitor visitor) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public void addExtension(IExtension extension) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public IExtension putExtension(IExtension extension) {
	   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public void addIcon(NamedIcon icon) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public void addIcon(NamedIcon icon, int position) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public void addViewer(INodeView viewer) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public boolean areViewsEmpty() {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public Enumeration<NodeModel> children() {
	    return super.children();
    }

	@Override
    public boolean containsExtension(Class<? extends IExtension> clazz) {
	    return delegate.containsExtension(clazz);
    }

	@Override
    public String createID() {
	    return delegate.createID();
    }

	@Override
    public void fireNodeChanged(NodeChangeEvent nodeChangeEvent) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public boolean getAllowsChildren() {
	    return delegate.getAllowsChildren();
    }

	@Override
    public NodeModel getChildAt(int childIndex) {
	    return super.getChildAt(childIndex);
    }

	@Override
    public int getChildCount() {
	    return super.getChildCount();
    }

	@Override
    public List<NodeModel> getChildren() {
	    return super.getChildren();
    }

	@Override
    public <T extends IExtension> T getExtension(Class<T> clazz) {
	    return delegate.getExtension(clazz);
    }

	@Override
    public Map<Class<? extends IExtension>, IExtension> getSharedExtensions() {
	    return delegate.getSharedExtensions();
    }

	@Override
    public FilterInfo getFilterInfo() {
	    return delegate.getFilterInfo();
    }

	@Override
    public HistoryInformationModel getHistoryInformation() {
	    return delegate.getHistoryInformation();
    }

	@Override
    public NamedIcon getIcon(int position) {
	    return delegate.getIcon(position);
    }

	@Override
    public List<NamedIcon> getIcons() {
	    return delegate.getIcons();
    }

	@Override
    public String getID() {
	    return delegate.getID();
    }

	@Override
    public int getIndex(NodeModel node) {
	    return super.getIndex(node);
    }

	@Override
    public MapModel getMap() {
	    return delegate.getMap();
    }

	@Override
    public int getNodeLevel(boolean countHidden) {
	    return delegate.getNodeLevel(countHidden);
    }

	@Override
    public NodeModel getParentNode() {
	    return delegate.getParentNode();
    }

	@Override
    public NodeModel[] getPathToRoot() {
	    return delegate.getPathToRoot();
    }

	@Override
    public String getText() {
	    return delegate.getText();
    }

	@Override
    public Collection<INodeView> getViewers() {
	    return delegate.getViewers();
    }

	@Override
    public boolean hasChildren() {
	    return false;
    }

	@Override
    public boolean hasID() {
	    return delegate.hasID();
    }

	public void insert(MutableTreeNode child, int index) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public boolean isDescendantOf(NodeModel node) {
	    return delegate.isDescendantOf(node);
    }

	@Override
    public boolean isFolded() {
	    return false;
    }

	@Override
    public boolean isLeaf() {
	    return delegate.isLeaf();
    }

	@Override
    public boolean isLeft() {
	    return delegate.isLeft();
    }

	@Override
    public boolean isNewChildLeft() {
	    return delegate.isNewChildLeft();
    }

	@Override
    public boolean isRoot() {
	    return delegate.isRoot();
    }

	@Override
    public boolean isVisible() {
	    return delegate.isVisible();
	}
	
	@Override
	public boolean hasVisibleContent() {
	    return delegate.hasVisibleContent();
    }
	
	@Override
	public boolean isHiddenSummary() {
		return delegate.isHiddenSummary();
	}

	public void remove(MutableTreeNode node) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public <T extends IExtension> T removeExtension(Class<T> clazz) {
		throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public boolean removeExtension(IExtension extension) {
		throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public int removeIcon() {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public int removeIcon(int position) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public void removeViewer(INodeView viewer) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public void setFolded(boolean folded) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public void setHistoryInformation(HistoryInformationModel historyInformation) {
		throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public void setID(String value) {
		throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public void setLeft(boolean isLeft) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public void setMap(MapModel map) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	public void setParent(MutableTreeNode newParent) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public void setParent(NodeModel newParent) {
		   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public String toString() {
	    return delegate.toString();
    }

	@Override
    public int depth() {
	    return delegate.depth();
    }

	@Override
    public void insert(NodeModel newNodeModel) {
	   throw new RuntimeException(METHOD_NOT_SUPPORTED);
    }

	@Override
    public NodeModel getVisibleAncestorOrSelf() {
	    return delegate.getVisibleAncestorOrSelf();
    }


}
