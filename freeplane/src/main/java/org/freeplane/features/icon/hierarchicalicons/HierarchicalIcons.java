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
package org.freeplane.features.icon.hierarchicalicons;

import java.util.Collection;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.icon.IStateIconProvider;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.NamedIcon;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.UIIconSet;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.map.NodeStream;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.n3.nanoxml.XMLElement;

public abstract class HierarchicalIcons extends PersistentNodeHook implements INodeChangeListener, IMapChangeListener, IExtension {
	public static final String ICONS = "hierarchical_icons";

    public static void install(final ModeController modeController) {
        IconController.getController(modeController).addStateIconProvider(new IStateIconProvider() {
            @Override
            public UIIcon getStateIcon(NodeModel node) {
                
                AccumulatedIcons iconSet = node.getExtension(AccumulatedIcons.class);
                if(iconSet == null) {
                    HierarchicalIcons hierarchicalIconsForSubtree = hierarchicalIconsForSubtree(node);
                    if(hierarchicalIconsForSubtree != null) {
                        NodeStream.bottomUpOf(node).forEach(n -> AccumulatedIcons.setStyleCheckForChange(n, hierarchicalIconsForSubtree.mode));
                        iconSet = node.getExtension(AccumulatedIcons.class);
                    }
                }
                if(iconSet != null) {
                    Collection<NamedIcon> accumulatedIcons = iconSet.getAccumulatedIcons();
                    if(! accumulatedIcons.isEmpty())
                        return new UIIconSet(accumulatedIcons, 0.75f);
                }
                return null;
            }

            @Override
            public boolean mustIncludeInIconRegistry() {
                return false;
            }
        });
        new IconIntersectionHierarchy().installHook(modeController);
        new IconUnionHierarchy().installHook(modeController);
	}
    
	protected HierarchicalIcons(Mode mode) {
		super();
		this.mode = mode;
	}

    protected void installHook(final ModeController modeController) {
		modeController.getMapController().addUINodeChangeListener(this);
		modeController.getMapController().addUIMapChangeListener(this);
    }

	@Override
	public void undoableToggleHook(NodeModel node, IExtension extension) {
		removeAnotherMode(node);
		super.undoableToggleHook(node, extension);
	}

	abstract protected void removeAnotherMode(NodeModel node);
	
	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		return this;
	}

	/**
	 */

	@Override
	public void mapChanged(final MapChangeEvent event) {
		final MapModel map = event.getMap();
		if(map == null){
			return;
		}
		final Object property = event.getProperty();
		if(! property.equals(MapStyle.MAP_STYLES)){
			return;
		}
		final NodeModel rootNode = map.getRootNode();
		NodeStream.of(rootNode).forEach(node -> node.removeExtension(AccumulatedIcons.class));
	}

	@Override
	public void nodeChanged(final NodeChangeEvent event) {
		final NodeModel node = event.getNode();
		setStyleUpToRoot(node);
	}

    @Override
	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
		setStyleUpToRoot(nodeDeletionEvent.parent);
	}

	@Override
	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
		setStyleUpToRoot(child);
	}

	@Override
	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		setStyleUpToRoot(nodeMoveEvent.oldParent);
		setStyleUpToRoot(nodeMoveEvent.child);
	}

	@Override
	public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
	}

	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		removeIcons(node);
		super.remove(node, extension);
	}

	/**
	 */
	private void removeIcons(final NodeModel node) {
		AccumulatedIcons icons = node.removeExtension(AccumulatedIcons.class);
		if(icons != null && ! icons.getAccumulatedIcons().isEmpty()){
			Controller.getCurrentModeController().getMapController().delayedNodeRefresh(node, HierarchicalIcons.ICONS, null, null);
			for (final NodeModel child : node.getChildren()) {
			    if(null == hierarchicalIconsExtensionOf(child))
			        removeIcons(child);
			}
		}
	}

	public static enum Mode{INTERSECTION, UNION}
	final private Mode mode;

	private static HierarchicalIcons hierarchicalIconsExtensionOf(NodeModel node) {
	    if(node.containsExtension(IconUnionHierarchy.class))
	        return node.getExtension(IconUnionHierarchy.class);
	    else if (node.containsExtension(IconIntersectionHierarchy.class))
	        return node.getExtension(IconIntersectionHierarchy.class);
	    else
	        return null;
	}
	
    private static HierarchicalIcons hierarchicalIconsForSubtree(NodeModel node) {
        for(NodeModel parent = node; parent != null; parent = parent.getParentNode()) {
            HierarchicalIcons extension = hierarchicalIconsExtensionOf(parent);
            if(extension != null)
                return extension;
        }
        return null;
    }

    private static void setStyleUpToRoot(final NodeModel node, Mode mode) {
        if (AccumulatedIcons.setStyleCheckForChange(node, mode)) {
            NodeModel parent = node.getParentNode();
            if (parent != null && parent.containsExtension(AccumulatedIcons.class)) {
                HierarchicalIcons extension = hierarchicalIconsExtensionOf(node);
                if(extension != null)
                    setStyleUpToRoot(parent);
                else
                    setStyleUpToRoot(parent, mode);
            }
        }
    }
    private static void setStyleUpToRoot(final NodeModel node) {
        if(node.containsExtension(AccumulatedIcons.class)) {
            HierarchicalIcons extension = hierarchicalIconsForSubtree(node);
            if(extension != null)
                setStyleUpToRoot(node, extension.mode);
        }
    }

    @Override
	public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
	}


}

@NodeHookDescriptor(hookName = "accessories/plugins/HierarchicalIcons.properties", onceForMap = false)
class IconUnionHierarchy extends HierarchicalIcons{
    public IconUnionHierarchy() {
        super(Mode.UNION);
    }
    @Override
    protected void removeAnotherMode(NodeModel node) {
        final HierarchicalIcons extension = node.getExtension(IconIntersectionHierarchy.class);
        if(extension != null)
            extension.undoableDeactivateHook(node);
    }
}

@NodeHookDescriptor(hookName = "accessories/plugins/HierarchicalIcons2.properties", onceForMap = false)
class IconIntersectionHierarchy extends HierarchicalIcons{
	public IconIntersectionHierarchy() {
	    super(Mode.INTERSECTION);
    }
	@Override
	protected void removeAnotherMode(NodeModel node) {
		final HierarchicalIcons extension = node.getExtension(IconUnionHierarchy.class);
		if(extension != null)
			extension.undoableDeactivateHook(node);
	}
}

