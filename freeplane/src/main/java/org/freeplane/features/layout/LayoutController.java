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
import org.freeplane.api.ChildrenSides;
import org.freeplane.api.LayoutOrientation;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.filter.Filter;
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
    final private ExclusivePropertyChain<ChildNodesAlignment, NodeModel> childNodesAlignmentHandlers;
    final private ExclusivePropertyChain<LayoutOrientation, NodeModel> layoutOrientationHandlers;
    final private ExclusivePropertyChain<ChildrenSides, NodeModel> childrenSidesHandlers;
	
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

// 	final private ModeController modeController;

	public LayoutController() {
		super();
		modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final LayoutBuilder layoutBuilder = new LayoutBuilder();
		layoutBuilder.registerBy(readManager, writeManager);
		
		childNodesAlignmentHandlers = new ExclusivePropertyChain<ChildNodesAlignment, NodeModel>();
		childNodesAlignmentHandlers.addGetter(IPropertyHandler.STYLE, new IPropertyHandler<ChildNodesAlignment, NodeModel>() {
        	@Override
            public ChildNodesAlignment getProperty(final NodeModel node, LogicalStyleController.StyleOption option, ChildNodesAlignment currentValue) {
        		final ChildNodesAlignment returnedAlignment = getLayoutProperty(node, LayoutModel::getChildNodesAlignment, LayoutModel.DEFAULT_CHILD_NODES_ALIGNMENT);
        		return returnedAlignment;
        	}
        });
		
		childNodesAlignmentHandlers.addGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<ChildNodesAlignment, NodeModel>() {
        	@Override
            public ChildNodesAlignment getProperty(final NodeModel node, LogicalStyleController.StyleOption option, ChildNodesAlignment currentValue) {
        		return LayoutModel.DEFAULT_CHILD_NODES_ALIGNMENT;
        	}
        });

        
        layoutOrientationHandlers = new ExclusivePropertyChain<LayoutOrientation, NodeModel>();
        layoutOrientationHandlers.addGetter(IPropertyHandler.STYLE, new IPropertyHandler<LayoutOrientation, NodeModel>() {
            @Override
            public LayoutOrientation getProperty(final NodeModel node, LogicalStyleController.StyleOption option, LayoutOrientation currentValue) {
                final LayoutOrientation returnedAlignment = getLayoutProperty(node, LayoutModel::getLayoutOrientation, LayoutOrientation.NOT_SET);
                return returnedAlignment;
            }
        });
        
        layoutOrientationHandlers.addGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<LayoutOrientation, NodeModel>() {
            @Override
            public LayoutOrientation getProperty(final NodeModel node, LogicalStyleController.StyleOption option, LayoutOrientation currentValue) {
                return LayoutOrientation.AS_PARENT;
            }
        });
        
        childrenSidesHandlers = new ExclusivePropertyChain<ChildrenSides, NodeModel>();
        childrenSidesHandlers.addGetter(IPropertyHandler.STYLE, new IPropertyHandler<ChildrenSides, NodeModel>() {
            @Override
            public ChildrenSides getProperty(final NodeModel node, LogicalStyleController.StyleOption option, ChildrenSides currentValue) {
                final ChildrenSides returnedAlignment = getLayoutProperty(node, LayoutModel::getChildrenSides, ChildrenSides.NOT_SET);
                return returnedAlignment;
            }
        });
        
        childrenSidesHandlers.addGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<ChildrenSides, NodeModel>() {
            @Override
            public ChildrenSides getProperty(final NodeModel node, LogicalStyleController.StyleOption option, ChildrenSides currentValue) {
                return ChildrenSides.AUTO;
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
	    return childNodesAlignmentHandlers.getProperty(node, StyleOption.FOR_UNSELECTED_NODE);
	}

	public LayoutOrientation getLayoutOrientation(NodeModel node) {
	    return layoutOrientationHandlers.getProperty(node, StyleOption.FOR_UNSELECTED_NODE);
	}

	public ChildrenSides getChildrenSides(NodeModel node) {
	    return childrenSidesHandlers.getProperty(node, StyleOption.FOR_UNSELECTED_NODE);
	}

	public LayoutOrientation getEffectiveLayoutOrientation(NodeModel node, Filter filter) {
	    if(node.hasVisibleContent(filter)) {
	        LayoutOrientation layoutOrientation = getLayoutOrientation(node);
	        switch(layoutOrientation) {
	        case TOP_TO_BOTTOM:
	        case LEFT_TO_RIGHT:
	            return layoutOrientation;
	        default: break;
	        }
	    }
	    NodeModel parentNode = node.getParentNode();
	    if(parentNode != null)
            return getEffectiveLayoutOrientation(parentNode, filter);
        else
            return LayoutOrientation.TOP_TO_BOTTOM;
    }
	
	   public boolean isTopOrLeft(NodeModel node, NodeModel root) {
	        NodeModel parentNode = node.getParentNode();
	        if (parentNode == null)
	            return false;
	        ChildrenSides childrenSides = getChildrenSides(parentNode);
	        switch(childrenSides) {
	        case NOT_SET:
                break;
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
                return isTopOrLeft(parentNode, root);
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
	    private static final boolean[] BOTH_SIDES = {true, false};
	    private static final boolean[] LEFT_SIDE = {true};
	    private static final boolean[] RIGHT_SIDE = {false};

	    public boolean[] sidesOf(NodeModel parentNode, NodeModel root) {
	        if (parentNode == root)
                return BOTH_SIDES;
	        ChildrenSides childrenSides = getChildrenSides(parentNode);
	        switch(childrenSides) {
            case BOTTOM_OR_RIGHT:
                return RIGHT_SIDE;
            case TOP_OR_LEFT:
                return LEFT_SIDE;
            case BOTH_SIDES:
                return BOTH_SIDES;
            default:
                break;
	        }
	        return parentNode.isTopOrLeft(root) ? LEFT_SIDE : RIGHT_SIDE;
	    }
	    
}
