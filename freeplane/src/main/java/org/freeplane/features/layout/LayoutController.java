/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.layout;

import java.util.Collection;
import java.util.function.Function;

import org.freeplane.api.ChildNodesAlignment;
import org.freeplane.api.ChildNodesLayout;
import org.freeplane.api.ChildrenSides;
import org.freeplane.api.LayoutOrientation;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeModel.Side;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ExclusivePropertyChain;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class LayoutController implements IExtension {
    final private ExclusivePropertyChain<ChildNodesLayout, NodeModel> childrenLayoutHandlers;

    private final ModeController modeController;

	public static LayoutController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static LayoutController getController(ModeController modeController) {
		return modeController.getExtension(LayoutController.class);
	}

	public static void install( final LayoutController layoutController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(LayoutController.class, layoutController);
	}

	public LayoutController() {
		super();
		modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final LayoutBuilder layoutBuilder = new LayoutBuilder();
		layoutBuilder.registerBy(readManager, writeManager);

        childrenLayoutHandlers = new ExclusivePropertyChain<ChildNodesLayout, NodeModel>();
        childrenLayoutHandlers.addGetter(IPropertyHandler.STYLE, new IPropertyHandler<ChildNodesLayout, NodeModel>() {
            @Override
            public ChildNodesLayout getProperty(final NodeModel node, LogicalStyleController.StyleOption option, ChildNodesLayout currentValue) {
                final ChildNodesLayout returnedAlignment = getLayoutProperty(node, LayoutModel::getChildNodesLayout, ChildNodesLayout.NOT_SET);
                return returnedAlignment;
            }
        });

        childrenLayoutHandlers.addGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<ChildNodesLayout, NodeModel>() {
            @Override
            public ChildNodesLayout getProperty(final NodeModel node, LogicalStyleController.StyleOption option, ChildNodesLayout currentValue) {
                return ChildNodesLayout.AUTO;
            }
        });
	}
	private <V> V getLayoutProperty(final NodeModel node, Function<LayoutModel, V> getter, V defaultValue) {
        final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
        final Collection<IStyle> styleKeys = styleController.getStyles(node, StyleOption.FOR_UNSELECTED_NODE);
        final MapModel map = node.getMap();
        final MapStyleModel model = MapStyleModel.getExtension(map);
        for(IStyle styleKey : styleKeys){
            final NodeModel styleNode = model.getStyleNode(styleKey);
            if (styleNode == null) {
                continue;
            }
            final LayoutModel styleModel = styleNode.getExtension(LayoutModel.class);
            if (styleModel == null) {
                continue;
            }
            V value = getter.apply(styleModel);
            if (value == defaultValue) {
                continue;
            }
            return value;
        }
        return null;
    }

	public ChildNodesAlignment getChildNodesAlignment(NodeModel node) {
	    return getEffectiveChildNodesLayout(node).childNodesAlignment();
	}

	public LayoutOrientation getLayoutOrientation(NodeModel node) {
	    return getEffectiveChildNodesLayout(node).layoutOrientation();
	}

	public ChildNodesLayout getEffectiveChildNodesLayout(NodeModel node) {
	    ChildNodesLayout layout = getChildNodesLayout(node);
	    if(node.isRoot() && layout.childNodesAlignment() == ChildNodesAlignment.STACKED_AUTO) {
	        return ChildNodesLayout.LEFTTORIGHT_BOTHSIDES_CENTERED;
        }
	    else if(! node.isRoot() && layout == ChildNodesLayout.AUTO && node.getParentNode().isRoot())
	        return childrenLayoutHandlers.getProperty(node.getParentNode(), StyleOption.FOR_UNSELECTED_NODE);
        return layout;
	}

    public ChildNodesLayout getChildNodesLayout(NodeModel node) {
        ChildNodesLayout layout = childrenLayoutHandlers.getProperty(node, StyleOption.FOR_UNSELECTED_NODE);
        return layout;
    }

    public void withNodeChangeEventOnLayoutChange(NodeModel node, Runnable runnable) {
        ChildNodesLayout oldLayout = getChildNodesLayout(node);
        runnable.run();
        ChildNodesLayout newLayout = getChildNodesLayout(node);
        if(oldLayout != newLayout)
            Controller.getCurrentModeController().getMapController().refreshNodeLaterUndoable(node, ChildNodesLayout.class, oldLayout, newLayout);
    }

	public LayoutOrientation getEffectiveLayoutOrientation(NodeModel node) {
	    LayoutOrientation layoutOrientation = getLayoutOrientation(node);
	    switch(layoutOrientation) {
	    case TOP_TO_BOTTOM:
	    case LEFT_TO_RIGHT:
	        return layoutOrientation;
	    default: break;
	    }
	    NodeModel parentNode = node.getParentNode();
	    if(parentNode != null)
	        return getEffectiveLayoutOrientation(parentNode);
	    else
            return LayoutOrientation.TOP_TO_BOTTOM;
    }

	   private boolean isTopOrLeft(NodeModel node, NodeModel root) {
	        NodeModel parentNode = node.getParentNode();
	        if (parentNode == null)
	            return false;
	        ChildrenSides childrenSides = getEffectiveChildNodesLayout(parentNode).childrenSides();
	        switch(childrenSides) {
	        case NOT_SET:
	        case ASC:
	        case DESC:
            case AUTO:
                break;
            case BOTH_SIDES: {
                Side side = node.getSide();
                if(side == Side.TOP_OR_LEFT)
                    return true;
                if(side == Side.BOTTOM_OR_RIGHT)
                    return false;
                break;
            }
            case BOTTOM_OR_RIGHT:
                return false;
            case TOP_OR_LEFT:
                return true;
	        }
            if (parentNode == root) {
                Side side = node.getSide();
                if (side != Side.DEFAULT)
                    return side == Side.TOP_OR_LEFT;
                else
                    return parentNode.isTopOrLeft(parentNode.getMap().getRootNode());
            } else
                return childrenSides == ChildrenSides.ASC != isTopOrLeft(parentNode, root);
	    }

	    public Side suggestNewChildSide(NodeModel parent, NodeModel root) {
	        if(parent != root)
	            return Side.DEFAULT;
	        int rightChildrenCount = 0;
	        int childCount = parent.getChildCount();
	        int childCountInTree = childCount;
	        for (int i = 0; i < childCount; i++) {
	            NodeModel child = parent.getChildAt(i);
	            if(child.isHiddenSummary() || FreeNode.isFreeNode(child))
	                childCountInTree--;
	            else if (!isTopOrLeft(child, parent)) {
	                rightChildrenCount++;
	            }
	            if (rightChildrenCount > childCountInTree / 2) {
	                return Side.TOP_OR_LEFT;
	            }
	        }
	        return Side.BOTTOM_OR_RIGHT;
	    }
	    public static final boolean[] BOTH_SIDES = {true, false};
	    public static final boolean[] LEFT_SIDE = {true};
	    public static final boolean[] RIGHT_SIDE = {false};

	    public boolean[] sidesOf(NodeModel parentNode, NodeModel root) {
	        if (parentNode == root)
                return BOTH_SIDES;
	        ChildrenSides childrenSides = getEffectiveChildNodesLayout(parentNode).childrenSides();
	        switch(childrenSides) {
            case BOTTOM_OR_RIGHT:
                return RIGHT_SIDE;
            case TOP_OR_LEFT:
                return LEFT_SIDE;
            case BOTH_SIDES:
            case ASC:
            case DESC:
                return BOTH_SIDES;
            default:
                break;
	        }
	        return parentNode.isTopOrLeft(root) ? LEFT_SIDE : RIGHT_SIDE;
	    }

}
