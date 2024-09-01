/*
 * Created on 11 Sep 2022
 *
 * author dimitry
 */
package org.freeplane.view.swing.map;

import java.awt.Component;
import java.awt.Dimension;

import org.freeplane.api.ChildrenSides;
import org.freeplane.api.ChildNodesAlignment;
import org.freeplane.features.map.NodeModel;

class NodeViewLayoutHelper {

	private NodeView view;
	private int topOverlap;
	private int bottomOverlap;

	NodeViewLayoutHelper(NodeView view) {
		this.view = view;
	}

	Dimension calculateContentSize() {
		Dimension contentSize = ContentSizeCalculator.INSTANCE.calculateContentSize(view);
		return usesHorizontallayout(view.getContent()) ? new Dimension(contentSize.height, contentSize.width) : contentSize;
	}

	int getAdditionalCloudHeigth() {
		return CloudHeightCalculator.INSTANCE.getAdditionalCloudHeigth(view);
	}

	int getComponentCount() {
		return view.getComponentCount();
	}

	NodeViewLayoutHelper getComponent(int n) {
		Component component = view.getComponent(n);
		return component instanceof NodeView ? ((NodeView) component).getLayoutHelper() : null;
	}

	MapView getMap() {
		return view.getMap();
	}

	NodeModel getNode() {
		return view.getNode();
	}

    int getMinimalDistanceBetweenChildren() {
        return view.getMinimalDistanceBetweenChildren();
    }

    int getBaseDistanceToChildren() {
        return view.getBaseDistanceToChildren();
    }

	int getSpaceAround() {
		return view.getSpaceAround();
	}

	ChildNodesAlignment getChildNodesAlignment() {
		return view.getChildNodesAlignment();
	}

	int getContentX() {
        Component component = view.getContent();
        return usesHorizontallayout(view) ? component.getY(): component.getX();
	}

	int getContentY() {
        Component component = view.getContent();
        return usesHorizontallayout(view) ? component.getX(): component.getY();
	}


    int getContentYForSummary() {
        Component component = view.getContent();
        return usesHorizontallayout(component) ? component.getX(): component.getY();
    }


	int getContentWidth() {
		Component component = view.getContent();
        return usesHorizontallayout(view) ? component.getHeight(): component.getWidth();
	}

	int getContentHeight() {
        Component component = view.getContent();
        return usesHorizontallayout(view) ? component.getWidth() : component.getHeight();
	}

	void setContentBounds(int x, int y, int width, int height) {
		Component component = view.getContent();
		if (usesHorizontallayout(component))
			component.setBounds(y, x, height, width);
		else
			component.setBounds(x, y, width, height);
	}

	void setContentVisible(boolean aFlag) {
		view.getContent().setVisible(aFlag);
	}

	boolean isContentVisible() {
		return view.isContentVisible();

	}

	boolean isSummary() {
		return view.isSummary();
	}

    boolean isFirstGroupNode() {
        return view.isFirstGroupNode();
    }

    boolean usesHorizontalLayout() {
        return view.usesHorizontalLayout();
    }

	boolean isLeft() {
		return view.isTopOrLeft();
	}

	int getHGap() {
		return view.getHGap();
	}

	int getShift() {
		return view.getShift();
	}

	boolean isFree() {
		return view.isFree();
	}


 	int getTopOverlap() {
		return topOverlap;
	}

	void setTopOverlap(int topOverlap) {
		this.topOverlap = topOverlap;
	}

	int getBottomOverlap() {
		return bottomOverlap;
	}

	void setBottomOverlap(int bottomOverlap) {
		this.bottomOverlap = bottomOverlap;
	}
	NodeViewLayoutHelper getParentView() {
		NodeView parentView = view.getParentView();
		return parentView != null ? parentView.getLayoutHelper() : null;
	}

	int getZoomed(int i) {
		return view.getZoomed(i);
	}

	int getHeight() {
		return getHeight(view);
	}

	int getWidth() {
		return getWidth(view);
	}

	int getX() {
		return getX(view);
	}

	int getY() {
		return getY(view);
	}

	void setSize(int width, int height) {
		if (usesHorizontallayout(view.getContent()))
			view.setSize(height, width);
		else
			view.setSize(width, height);
	}

	void setLocation(int x, int y) {
		if (usesHorizontallayout(view))
			view.setLocation(y, x);
		else
			view.setLocation(x, y);
	}

	private int getX(Component component) {
		return usesHorizontallayout(component) ? component.getY(): component.getX();
	}

	private int getY(Component component) {
		return usesHorizontallayout(component) ? component.getX(): component.getY();
	}

	private int getWidth(Component component) {
		return usesHorizontallayout(component) ? component.getHeight(): component.getWidth();
	}

	private int getHeight(Component component) {
		return usesHorizontallayout(component) ? component.getWidth(): component.getHeight();
	}

	String describeComponent(int i) {
	    return view.getComponent(i).toString();
	}

	String getText() {
	    return view.getNode().getText();
	}

	boolean usesHorizontallayout(Component component) {
	    NodeView parent;
        if (component == view && view.isRoot()) {
            parent = view;
        } else {
            parent = (NodeView)component.getParent();
        }
	    return parent.usesHorizontalLayout();
	}

    int getMinimumDistanceConsideringHandles() {
        return view.getMinimumDistanceConsideringHandles();
    }

    boolean paintsChildrenOnTheLeft() {
        return view.paintsChildrenOnTheLeft();
    }

    @Override
    public String toString() {
        return "NodeViewLayoutHelper [view=" + view + "]";
    }

    ChildrenSides childrenSides() {
        return view.childrenSides();
    }

    boolean isSubtreeVisible() {
       return view.isSubtreeVisible();
    }
}
