/*
 * Created on 11 Sep 2022
 *
 * author dimitry
 */
package org.freeplane.view.swing.map;

import java.awt.Component;
import java.awt.Dimension;

import org.freeplane.api.VerticalNodeAlignment;
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
		return usesHorizontallayout() ? new Dimension(contentSize.height, contentSize.width) : contentSize;
	}

	int getAdditionalCloudHeigth() {
		return CloudHeightCalculator.INSTANCE.getAdditionalCloudHeigth(view);
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

	int getContentX() {
		return getX(view.getContent());
	}

	int getContentY() {
		return getY(view.getContent());
	}

	int getContentWidth() {
		return getWidth(view.getContent());
	}

	int getContentHeight() {
		return getHeight(view.getContent());
	}

	void setContentBounds(int x, int y, int width, int height) {
		Component component = view.getContent();
		if (usesHorizontallayout()) 
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
		return getHeight(view);
	}

	int getWidth() {
		return getWidth(view);
	}

	int getX() {
		return getX(view);
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

	private int getX(Component component) {
		return usesHorizontallayout() ? component.getY(): component.getX();
	}

	private int getY(Component component) {
		return usesHorizontallayout() ? component.getX(): component.getY();
	}

	private int getWidth(Component component) {
		return usesHorizontallayout() ? component.getHeight(): component.getWidth();
	}

	private int getHeight(Component component) {
		return usesHorizontallayout() ? component.getWidth(): component.getHeight();
	}

	String describeComponent(int i) {
		return view.getComponent(i).toString();
	}

	boolean usesHorizontallayout() {
		return view.usesHorizontalLayout();
	}
}
