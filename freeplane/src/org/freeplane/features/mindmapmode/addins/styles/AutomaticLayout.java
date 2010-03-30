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
package org.freeplane.features.mindmapmode.addins.styles;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Iterator;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IReadCompletionListener;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.common.addins.styles.LogicalStyleController;
import org.freeplane.features.common.addins.styles.MapStyleModel;
import org.freeplane.features.common.map.IMapChangeListener;
import org.freeplane.features.common.map.MapChangeEvent;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.n3.nanoxml.XMLElement;

@NodeHookDescriptor(hookName = "accessories/plugins/AutomaticLayout.properties")
@ActionLocationDescriptor(locations = "/menu_bar/format/nodes")
public class AutomaticLayout extends PersistentNodeHook implements IMapChangeListener,
        IReadCompletionListener, IExtension {

	
	private boolean setStyleActive = false;

	/**
	 *
	 */
	public AutomaticLayout(final ModeController modeController) {
		super(modeController);
		modeController.getMapController().getReadManager().addReadCompletionListener(this);
		getModeController().getMapController().addMapChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.NodeHook#invoke(freeplane.modes.MindMapNode)
	 */
	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		super.add(node, extension);
		setStyleRecursive(node);
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		return this;
	}

	public void mapChanged(final MapChangeEvent event) {
	}

	public void onNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
	}

	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
		if (setStyleActive || !isActive(parent)) {
			return;
		}
		setStyleRecursive(child);
	}

	public void onNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                        final NodeModel child, final int newIndex) {
		if (setStyleActive || !isActive(newParent)) {
			return;
		}
		setStyleRecursive(child);
	}

	public void onPreNodeDelete(final NodeModel parent, final NodeModel child, final int index) {
	}

	public void readingCompleted(final NodeModel topNode, final HashMap<String, String> newIds) {
		if (!topNode.containsExtension(getClass())) {
			return;
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				setStyleRecursive(topNode);
			}
		});
	}
	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.NodeHook#invoke(freeplane.modes.MindMapNode)
	 */
	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		super.remove(node, extension);
	}


	private void setStyleImpl(final NodeModel node) {
		MLogicalStyleController styleController = (MLogicalStyleController) getModeController().getExtension(LogicalStyleController.class);
		Object style = getStyle(node);
		styleController.setStyle(node, style);
	}

	private Object getStyle(NodeModel node) {
		final int depth = node.depth();
		final MapModel map = node.getMap();
		final MapStyleModel extension = MapStyleModel.getExtension(map);
		String name = depth == 0 ? "AutomaticLayout.level.root" : "AutomaticLayout.level," + depth;
		NamedObject obj = NamedObject.formatText(name);
		if(extension.getStyleNode(obj) != null){
			return obj;
		}
		return MapStyleModel.DEFAULT_STYLE;
    }

	/**
	 */
	private void setStyleRecursive(final NodeModel node) {
		setStyleActive = true;
		setStyleRecursiveImpl(node);
		setStyleActive = false;
	}

	private void setStyleRecursiveImpl(final NodeModel node) {
		if (((MModeController) getModeController()).isUndoAction()) {
			return;
		}
		setStyleImpl(node);
		for (final Iterator i = getModeController().getMapController().childrenUnfolded(node); i.hasNext();) {
			final NodeModel child = (NodeModel) i.next();
			setStyleRecursiveImpl(child);
		}
	}

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
	    // TODO Auto-generated method stub
	    
    }
}
