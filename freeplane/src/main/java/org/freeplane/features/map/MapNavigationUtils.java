package org.freeplane.features.map;

import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.mode.Controller;

public class MapNavigationUtils {

    public static NodeModel findNext(final Direction direction, NodeModel current, final NodeModel end) {
    	if (hasChildren(current, direction)) {
    		final NodeModel next = current.getChildAt(0);
    		if (atEnd(next, end)) {
    			return null;
    		}
    		return next;
    	}
    	for (;;) {
    		final NodeModel parentNode = current.getParentNode();
    		if (parentNode == null) {
    			return current;
    		}
    		final int index = parentNode.getIndex(current) + 1;
    		final int childCount = parentNode.getChildCount();
    		if (direction == Direction.FORWARD_N_FOLD) {
    			Controller.getCurrentModeController().getMapController().fold(current);
    		}
    		if (index < childCount) {
    			final NodeModel next = parentNode.getChildAt(index);
    			if (atEnd(next, end)) {
    				return null;
    			}
    			return next;
    		}
    		current = parentNode;
    		if (atEnd(current, end)) {
    			return null;
    		}
    	}
    }

    private static boolean atEnd(NodeModel current, final NodeModel end) {
        return end != null && current.equals(end);
    }

    public static NodeModel findPrevious(final Direction direction, NodeModel current, final NodeModel end) {
    	for (;;) {
    		if(current == end)
    			break;
    		final NodeModel parentNode = current.getParentNode();
    		if (parentNode == null) {
    			break;
    		}
    		if (direction == Direction.BACK_N_FOLD) {
    			Controller.getCurrentModeController().getMapController().fold(current);
    		}
    		final int index = parentNode.getIndex(current) - 1;
    		if (index < 0) {
    			if (direction == Direction.BACK_N_FOLD) {
    				Controller.getCurrentModeController().getMapController().fold(parentNode);
    			}
    			if (atEnd(parentNode, end)) {
    				return null;
    			}
    			return parentNode;
    		}
    		current = parentNode.getChildAt(index);
    		if (atEnd(current, end)) {
    			return null;
    		}
    		break;
    	}
    	for (;;) {
    		if (! hasChildren(current, direction)) {
    			if (atEnd(current, end)) {
    				return null;
    			}
    			return current;
    		}
    		current = current.getChildAt(current.getChildCount() - 1);
    	}
    }

    private static boolean hasChildren(NodeModel node, Direction direction) {
        if (node.getChildCount() == 0)
            return false;
        if(direction.canUnfold())
            return true;
        IMapSelection selection = Controller.getCurrentController().getSelection();
        if(selection == null)
            return ! node.isFolded();
        else
            return ! selection.isFolded(node);
    }
}
