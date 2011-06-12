/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Christian Foltin
 *  It is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.icon;

import java.util.HashMap;
import java.util.TreeSet;


import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IReadCompletionListener;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Foltin
 */
@NodeHookDescriptor(hookName = "accessories/plugins/HierarchicalIcons.properties")
public class HierarchicalIcons extends PersistentNodeHook implements INodeChangeListener, IMapChangeListener,
        IReadCompletionListener, IExtension {
	public static final String ICONS = "hierarchical_icons";

	public HierarchicalIcons() {
		this(Mode.OR);
		new HierarchicalIcons2();
	}
	protected HierarchicalIcons(Mode mode) {
		super();
		this.mode = mode;
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.getMapController().getReadManager().addReadCompletionListener(this);
		modeController.getMapController().addNodeChangeListener(this);
		modeController.getMapController().addMapChangeListener(this);
	}

	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		gatherLeavesAndSetStyle(node);
		gatherLeavesAndSetParentsStyle(node);
		super.add(node, extension);
	}

	/**
	 */
	private void addAccumulatedIconsToTreeSet(final NodeModel child, final TreeSet<UIIcon> iconSet) {
		iconSet.addAll(IconController.getController().getIcons(child));
		final UIIconSet uiIcon = (UIIconSet) child.getStateIcons().get(getHookName());
		if (uiIcon == null) {
			return;
		}
		iconSet.addAll(uiIcon.getIcons());
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		return this;
	}

	/**
	 */
	private void gatherLeavesAndSetParentsStyle(final NodeModel node) {
		if (node.getChildCount() == 0) {
			if (node.getParentNode() != null) {
				setStyleRecursive(node.getParentNode());
			}
			return;
		}
		for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
			gatherLeavesAndSetParentsStyle(child);
		}
	}

	/**
	 */
	private void gatherLeavesAndSetStyle(final NodeModel node) {
		if (node.getChildCount() == 0) {
			setStyle(node);
			return;
		}
		for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
			gatherLeavesAndSetStyle(child);
		}
	}

	public void mapChanged(final MapChangeEvent event) {
		final MapModel map = event.getMap();
		if(map == null){
			return;
		}
		final NodeModel rootNode = map.getRootNode();
		if (!isActive(rootNode)) {
			return;
		}
		final Object property = event.getProperty();
		if(! property.equals(MapStyle.MAP_STYLES)){
			return;
		}
		gatherLeavesAndSetStyle(rootNode);
		gatherLeavesAndSetParentsStyle(rootNode);
	}

	public void nodeChanged(final NodeChangeEvent event) {
		final NodeModel node = event.getNode();
		if (!isActive(node)) {
			return;
		}
		final Object property = event.getProperty();
		if (!(property.equals("icon") || property.equals(LogicalStyleModel.class))) {
			return;
		}
		setStyle(node);
		onUpdateChildren(node);
	}

	public void onNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
		if (!isActive(parent)) {
			return;
		}
		setStyleRecursive(parent);
	}

	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
		if (!isActive(parent)) {
			return;
		}
		setStyleRecursive(child);
	}

	public void onNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                        final NodeModel child, final int newIndex) {
		if (!isActive(newParent)) {
			return;
		}
		setStyleRecursive(oldParent);
		setStyleRecursive(child);
	}

	public void onPreNodeDelete(final NodeModel parent, final NodeModel child, final int index) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.extensions.PermanentNodeHook#onUpdateChildrenHook(freeplane.modes
	 * .MindMapNode)
	 */
	private void onUpdateChildren(final NodeModel updatedNode) {
		setStyleRecursive(updatedNode);
	}

	public void readingCompleted(final NodeModel topNode, final HashMap<String, String> newIds) {
		if (!topNode.containsExtension(getClass()) && !topNode.getMap().getRootNode().containsExtension(getClass())) {
			return;
		}
		gatherLeavesAndSetStyle(topNode);
		gatherLeavesAndSetParentsStyle(topNode);
	}

	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		removeIcons(node);
		super.remove(node, extension);
	}

	/**
	 */
	private void removeIcons(final NodeModel node) {
		node.removeStateIcons(getHookName());
		Controller.getCurrentModeController().getMapController().nodeRefresh(node);
		for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
			removeIcons(child);
		}
	}

	public static enum Mode{AND, OR};
	private Mode mode = Mode.OR;
	private void setStyle(final NodeModel node) {
		final TreeSet<UIIcon> iconSet = new TreeSet<UIIcon>();
		boolean first = true;
		for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
			if(first || mode.equals(Mode.OR)){
				addAccumulatedIconsToTreeSet(child, iconSet);
			}
			else{
				final TreeSet<UIIcon> iconSet2 = new TreeSet<UIIcon>();
				addAccumulatedIconsToTreeSet(child, iconSet2);
				iconSet.retainAll(iconSet2);
				if(iconSet.isEmpty())
					break;
			}
			first = false;
		}
		iconSet.removeAll(IconController.getController().getIcons(node));
		
		if (iconSet.size() > 0) {
			node.setStateIcon(getHookName(), new UIIconSet(iconSet, 0.75f), false);
		}
		else {
			node.removeStateIcons(getHookName());
		}
		Controller.getCurrentModeController().getMapController().delayedNodeRefresh(node, HierarchicalIcons.ICONS, null, null);
	}

	/**
	 */
	private void setStyleRecursive(final NodeModel node) {
		setStyle(node);
		if (node.getParentNode() != null) {
			setStyleRecursive(node.getParentNode());
		}
	}

	public void onPreNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                           final NodeModel child, final int newIndex) {
	}
	
	
}

@NodeHookDescriptor(hookName = "accessories/plugins/HierarchicalIcons2.properties")
class HierarchicalIcons2 extends HierarchicalIcons{
	public HierarchicalIcons2() {
	    super(Mode.AND);
    }
}
