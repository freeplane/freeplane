/*
 * Created on 11 Sep 2022
 *
 * author dimitry
 */
package org.freeplane.view.swing.map;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;

import org.freeplane.api.VerticalNodeAlignment;
import org.freeplane.features.map.NodeModel;

class NodeViewLayoutHelper {
	class ContentAccessor {

		int getX() {
			return usesHorizontallayout() ? getContent().getY(): getContent().getX();
		}

		int getY() {
			return usesHorizontallayout() ? getContent().getX(): getContent().getY();
		}

		int getWidth() {
			return usesHorizontallayout() ? getContent().getHeight(): getContent().getWidth();
		}

		int getHeight() {
			return usesHorizontallayout() ? getContent().getWidth(): getContent().getHeight();
		}

		public void setBounds(int x, int y, int width, int height) {
			if (usesHorizontallayout()) 
				getContent().setBounds(y, x, height, width);
			else
				getContent().setBounds(x, y, width, height);
		}

		public void setVisible(boolean aFlag) {
			getContent().setVisible(aFlag);
		}

		private JComponent getContent() {
			return view.getContent();
		}
		
		

	}
	
	private NodeView view;
	private int topOverlap;
	private int bottomOverlap;
	private final ContentAccessor contentAccessor;

	NodeViewLayoutHelper(NodeView view) {
		this.view = view;
		this.contentAccessor = new ContentAccessor();
	}

	Dimension calculateContentSize() {
		Dimension contentSize = ContentSizeCalculator.INSTANCE.calculateContentSize(view);
		return usesHorizontallayout() ? new Dimension(contentSize.height, contentSize.width) : contentSize;
	}

	int getAdditionalCloudHeigth() {
		return CloudHeightCalculator.INSTANCE.getAdditionalCloudHeigth(view);
	}

	ContentAccessor getContent() {
		return contentAccessor;
	}

	int getComponentCount() {
		return view.getComponentCount();
	}

	NodeViewLayoutHelper getComponent(int n) {
		Component component = view.getComponent(n);
		return component instanceof NodeView ? new NodeViewLayoutHelper((NodeView) component) : null;
	}

	MapView getMap() {
		return view.getMap();
	}

	NodeModel getModel() {
		return view.getModel();
	}

	int getMinimalDistanceBetweenChildren() {
		return view.getMinimalDistanceBetweenChildren();
	}

	int getSpaceAround() {
		return view.getSpaceAround();
	}

	VerticalNodeAlignment getVerticalAlignment() {
		return view.getVerticalAlignment();
	}

	boolean isContentVisible() {
		return view.isContentVisible();
		
	}

	boolean isFolded() {
		return view.isFolded();
		
	}

	boolean isSummary() {
		return view.isSummary();
	}

	boolean isFirstGroupNode() {
		return view.isFirstGroupNode();
	}

	boolean isLeft() {
		return view.isLeft();
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
		return parentView != null ? new NodeViewLayoutHelper(parentView) : null;
	}

	int getZoomed(int i) {
		return view.getZoomed(i);
	}

	int getHeight() {
		return usesHorizontallayout() ? view.getWidth() : view.getHeight();
	}

	int getWidth() {
		return usesHorizontallayout() ? view.getHeight() : view.getWidth();
	}

	int getX() {
		return usesHorizontallayout() ? view.getY() : view.getX();
	}

	void setSize(int width, int height) {
		if (usesHorizontallayout())
			view.setSize(height, width);
		else
			view.setSize(width, height);
	}

	void setLocation(int x, int y) {
		if (usesHorizontallayout()) 
			view.setLocation(y, x);
		else 
			view.setLocation(x, y);
	}

	String describeComponent(int i) {
		return view.getComponent(i).toString();
	}

	boolean usesHorizontallayout() {
		return view.getMap().usesHorizontalLayout();
	}
}
