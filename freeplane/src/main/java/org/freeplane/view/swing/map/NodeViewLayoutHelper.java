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
	static class ContentAccessor {

		private JComponent content;

		ContentAccessor(JComponent content) {
			this.content = content;
		}

		int getX() {
			return useHorizontalLayout ? content.getY(): content.getX();
		}

		int getY() {
			return useHorizontalLayout ? content.getX(): content.getY();
		}

		int getWidth() {
			return useHorizontalLayout ? content.getHeight(): content.getWidth();
		}

		int getHeight() {
			return useHorizontalLayout ? content.getWidth(): content.getHeight();
		}

		public void setBounds(int x, int y, int width, int height) {
			if (useHorizontalLayout) 
				content.setBounds(y, x, height, width);
			else
				content.setBounds(x, y, width, height);
		}

		public void setVisible(boolean aFlag) {
			content.setVisible(aFlag);
		}
		
		

	}
	
	final static boolean useHorizontalLayout = true;

	private NodeView view;
	private int topOverlap;
	private int bottomOverlap;

	NodeViewLayoutHelper(NodeView view) {
		this.view = view;
	}

	Dimension calculateContentSize() {
		Dimension contentSize = ContentSizeCalculator.INSTANCE.calculateContentSize(view);
		return useHorizontalLayout ? new Dimension(contentSize.height, contentSize.width) : contentSize;
	}

	int getAdditionalCloudHeigth() {
		return CloudHeightCalculator.INSTANCE.getAdditionalCloudHeigth(view);
	}

	ContentAccessor getContent() {
		return new ContentAccessor(view.getContent());
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
		return useHorizontalLayout ? view.getWidth() : view.getHeight();
	}

	int getWidth() {
		return useHorizontalLayout ? view.getHeight() : view.getWidth();
	}

	int getX() {
		return useHorizontalLayout ? view.getY() : view.getX();
	}

	void setSize(int width, int height) {
		if (useHorizontalLayout)
			view.setSize(height, width);
		else
			view.setSize(width, height);
	}

	void setLocation(int x, int y) {
		if (useHorizontalLayout) 
			view.setLocation(y, x);
		else 
			view.setLocation(x, y);
	}

	String describeComponent(int i) {
		return view.getComponent(i).toString();
	}
}
