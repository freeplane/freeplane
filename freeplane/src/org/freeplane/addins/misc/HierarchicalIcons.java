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
package org.freeplane.addins.misc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.freeplane.addins.NodeHookDescriptor;
import org.freeplane.addins.PersistentNodeHook;
import org.freeplane.core.controller.ActionDescriptor;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IReadCompletionListener;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.mode.INodeChangeListener;
import org.freeplane.core.mode.ModeController;
import org.freeplane.core.mode.NodeChangeEvent;
import org.freeplane.map.icon.MindIcon;
import org.freeplane.modes.mindmapmode.IMapChangeListener;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.view.map.MultipleImage;

/**
 * @author Foltin
 */
@NodeHookDescriptor(hookName = "accessories/plugins/HierarchicalIcons.properties")
@ActionDescriptor(locations = { "/menu_bar/format/nodes/automaticLayout2" }, //
name = "accessories/plugins/HierarchicalIcons.properties_name", //
tooltip = "accessories/plugins/HierarchicalIcons.properties_documentation")
public class HierarchicalIcons extends PersistentNodeHook implements INodeChangeListener,
        IMapChangeListener, IReadCompletionListener {
	final private HashMap /* of MindMapNode to a TreeSet */nodeIconSets = new HashMap();

	public HierarchicalIcons(final MModeController modeController) {
		super(modeController);
		modeController.getMapController().getReadManager().addReadCompletionListener(this);
	}

	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		gatherLeavesAndSetStyle(node);
		gatherLeavesAndSetParentsStyle(node);
		final ModeController modeController = getModeController();
		modeController.addNodeChangeListener(this);
		modeController.getMapController().addMapChangeListener(this);
		super.add(node, extension);
	}

	/**
	 */
	private void addAccumulatedIconsToTreeSet(final NodeModel child, final TreeSet iconSet,
	                                          final TreeSet childsTreeSet) {
		for (final Iterator i = child.getIcons().iterator(); i.hasNext();) {
			final MindIcon icon = (MindIcon) i.next();
			iconSet.add(icon.getName());
		}
		if (childsTreeSet == null) {
			return;
		}
		for (final Iterator i = childsTreeSet.iterator(); i.hasNext();) {
			final String iconName = (String) i.next();
			iconSet.add(iconName);
		}
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
		for (final Iterator i = node.getModeController().getMapController().childrenUnfolded(node); i
		    .hasNext();) {
			final NodeModel child = (NodeModel) i.next();
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
		for (final Iterator i = node.getModeController().getMapController().childrenUnfolded(node); i
		    .hasNext();) {
			final NodeModel child = (NodeModel) i.next();
			gatherLeavesAndSetStyle(child);
		}
	}

	public void nodeChanged(final NodeChangeEvent event) {
		if (!event.getProperty().equals("icon")) {
			return;
		}
		onUpdateChildren(event.getNode());
	}

	public void onNodeDeleted(final NodeModel parent, final NodeModel child) {
		setStyleRecursive(parent);
	}

	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
		setStyleRecursive(child);
	}

	public void onNodeMoved(final NodeModel oldParent, final NodeModel newParent,
	                        final NodeModel child, final int newIndex) {
		setStyleRecursive(oldParent);
		setStyleRecursive(child);
	}

	public void onPreNodeDelete(final NodeModel model) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.extensions.PermanentNodeHook#onUpdateChildrenHook(freemind.modes
	 * .MindMapNode)
	 */
	private void onUpdateChildren(final NodeModel updatedNode) {
		setStyleRecursive(updatedNode);
	}

	public void readingCompleted(final NodeModel topNode, final HashMap<String, String> newIds) {
		if (!topNode.containsExtension(getClass())
		        && !topNode.getMap().getRootNode().containsExtension(getClass())) {
			return;
		}
		gatherLeavesAndSetStyle(topNode);
		gatherLeavesAndSetParentsStyle(topNode);
	}

	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		final ModeController modeController = getModeController();
		modeController.removeNodeChangeListener(this);
		modeController.getMapController().removeMapChangeListener(this);
		nodeIconSets.clear();
		removeIcons(node);
		super.remove(node, extension);
	}

	/**
	 */
	private void removeIcons(final NodeModel node) {
		node.setStateIcon(getHookName(), null);
		getModeController().getMapController().nodeRefresh(node);
		for (final Iterator i = node.getModeController().getMapController().childrenUnfolded(node); i
		    .hasNext();) {
			final NodeModel child = (NodeModel) i.next();
			removeIcons(child);
		}
	}

	private void setStyle(final NodeModel node) {
		final TreeSet iconSet = new TreeSet();
		for (final Iterator i = node.getModeController().getMapController().childrenUnfolded(node); i
		    .hasNext();) {
			final NodeModel child = (NodeModel) i.next();
			addAccumulatedIconsToTreeSet(child, iconSet, (TreeSet) nodeIconSets.get(child));
		}
		for (final Iterator i = node.getIcons().iterator(); i.hasNext();) {
			final MindIcon icon = (MindIcon) i.next();
			iconSet.remove(icon.getName());
		}
		boolean dirty = true;
		if (nodeIconSets.containsKey(node)) {
			final TreeSet storedIconSet = (TreeSet) nodeIconSets.get(node);
			if (storedIconSet.equals(iconSet)) {
				dirty = false;
			}
		}
		nodeIconSets.put(node, iconSet);
		if (dirty) {
			if (iconSet.size() > 0) {
				final MultipleImage image = new MultipleImage(0.75f);
				for (final Iterator i = iconSet.iterator(); i.hasNext();) {
					final String iconName = (String) i.next();
					final MindIcon icon = MindIcon.factory(iconName);
					image.addImage(icon.getIcon());
				}
				node.setStateIcon(getHookName(), image);
			}
			else {
				node.setStateIcon(getHookName(), null);
			}
			getModeController().getMapController().nodeRefresh(node);
		}
	}

	/**
	 */
	private void setStyleRecursive(final NodeModel node) {
		setStyle(node);
		if (node.getParentNode() != null) {
			setStyleRecursive(node.getParentNode());
		}
	}
}
