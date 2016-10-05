package org.freeplane.features.map;

import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.mode.Controller;

public class MapNavigationUtils {

    public static NodeModel findNext(final Direction direction, NodeModel current, final NodeModel end) {
    	if (current.getChildCount() != 0) {
    		final NodeModel next = (NodeModel) current.getChildAt(0);
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
    			Controller.getCurrentModeController().getMapController().setFolded(current, true);
    		}
    		if (index < childCount) {
    			final NodeModel next = (NodeModel) parentNode.getChildAt(index);
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
    		final NodeModel parentNode = current.getParentNode();
    		if (parentNode == null) {
    			break;
    		}
    		if (direction == Direction.BACK_N_FOLD) {
    			Controller.getCurrentModeController().getMapController().setFolded(current, true);
    		}
    		final int index = parentNode.getIndex(current) - 1;
    		if (index < 0) {
    			if (direction == Direction.BACK_N_FOLD) {
    				Controller.getCurrentModeController().getMapController().setFolded(parentNode, true);
    			}
    			if (atEnd(parentNode, end)) {
    				return null;
    			}
    			return parentNode;
    		}
    		current = (NodeModel) parentNode.getChildAt(index);
    		if (atEnd(current, end)) {
    			return null;
    		}
    		break;
    	}
    	for (;;) {
    		if (current.getChildCount() == 0) {
    			if (atEnd(current, end)) {
    				return null;
    			}
    			return current;
    		}
    		current = (NodeModel) current.getChildAt(current.getChildCount() - 1);
    	}
    }
}
