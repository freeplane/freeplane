/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.view.swing.map;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JComponent;

import org.freeplane.features.common.cloud.CloudController;
import org.freeplane.features.common.cloud.CloudModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.view.swing.map.cloud.CloudView;

abstract public class NodeViewLayoutAdapter implements INodeViewLayout {
	protected static class LayoutData{
    		final int[] lx;
    		final int[] ly;
    		int left;
    		int childContentHeight;
    		int childVerticalShift;
    		boolean rightDataSet;
    		boolean leftDataSet;
    		public LayoutData(int childCount) {
    	        super();
    	        this.lx = new int[childCount];
    	        this.ly = new int[childCount];
    	        this.left = 0;
    	        this.childContentHeight = 0;
    	        this.childVerticalShift = 0;
    	        rightDataSet = false;
    	        leftDataSet = false;
            }
    	}

	private static Dimension minDimension;
	private int childCount;
	private JComponent content;
	protected final int LISTENER_VIEW_WIDTH = 10;
	protected Point location = new Point();
	private NodeModel model;
	private int spaceAround;
	private int vGap;
	private NodeView view;
	private Dimension contentPreferredSize;
	private int contentWidth;

	public void addLayoutComponent(final String arg0, final Component arg1) {
	}
	/**
	 * @return Returns the childCount.
	 */
	protected int getChildCount() {
		return childCount;
	}

	/**
	 * @return Returns the content.
	 */
	protected JComponent getContent() {
		return content;
	}

	/**
	 * @return Returns the model.
	 */
	protected NodeModel getModel() {
		return model;
	}

	/**
	 * @return Returns the spaceAround.
	 */
	int getSpaceAround() {
		return spaceAround;
	}

	/**
	 * @return Returns the vGap.
	 */
	int getVGap() {
		return vGap;
	}

	/**
	 * @return Returns the view.
	 */
	protected NodeView getView() {
		return view;
	}

	abstract protected void layout();

	public void layoutContainer(final Container c) {
		setUp(c);
		layout();
		shutDown();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	public Dimension minimumLayoutSize(final Container arg0) {
		if (NodeViewLayoutAdapter.minDimension == null) {
			NodeViewLayoutAdapter.minDimension = new Dimension(0, 0);
		}
		return NodeViewLayoutAdapter.minDimension;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 */
	public Dimension preferredLayoutSize(final Container c) {
		if (!c.isValid()) {
			c.validate();
		}
		return c.getSize();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
	 */
	public void removeLayoutComponent(final Component arg0) {
	}

	protected void setUp(final Container c) {
		final NodeView localView = (NodeView) c;
		final int localChildCount = localView.getComponentCount() - 1;
		for (int i = 0; i < localChildCount; i++) {
			final Component component = localView.getComponent(i);
			if (component instanceof NodeView) {
				((NodeView) component).validateTree();
			}
			else {
				component.validate();
			}
		}
		view = localView;
		model = localView.getModel();
		childCount = localChildCount;
		content = localView.getContent();
		if (getModel().isVisible()) {
			setVGap(getView().getVGap());
		}
		else {
			setVGap(getView().getVisibleParentView().getVGap());
		}
		spaceAround = view.getMap().getZoomed(NodeView.SPACE_AROUND);
		if (view.isContentVisible()) {
    		contentPreferredSize = getContent().getPreferredSize();
    		contentWidth = contentPreferredSize.width;
    	}
    	else {
    		contentWidth = 0;
    	}
	}

	protected void shutDown() {
		view = null;
		model = null;
		content = null;
		childCount = 0;
		setVGap(0);
		spaceAround = 0;
	}

	public void setVGap(final int vGap) {
		this.vGap = vGap;
	}

	/**
	 * Calculates the tree height increment because of the clouds.
	 */
	public int getAdditionalCloudHeigth(final NodeView node) {
		if (!node.isContentVisible()) {
			return 0;
		}
		final ModeController modeController = node.getMap().getModeController();
		final CloudController cloudController = CloudController.getController(modeController);
		final CloudModel cloud = cloudController.getCloud(node.getModel());
		if (cloud != null) {
			return CloudView.getAdditionalHeigth(cloud, node);
		}
		else {
			return 0;
		}
	}

	protected void calcLayout(final boolean isLeft, final LayoutData data) {
        int y = 0;
    	int childContentHeight = 0;
    	boolean visibleChildFound = false;
    	int childVerticalShift = 0;
    	int left = 0;
    	for (int i = 0; i < getChildCount(); i++) {
    		final NodeView child = (NodeView) getView().getComponent(i);
    		if (child.isLeft() != isLeft) {
    			continue;
    		}
    		final int additionalCloudHeigth = getAdditionalCloudHeigth(child);
    		final int h = child.getContent().getHeight();
    		childContentHeight += h + additionalCloudHeigth;
    		if (child.getHeight() - 2 * getSpaceAround() != 0) {
    			if (visibleChildFound)
    				childContentHeight +=  getVGap();
    			else
    				visibleChildFound = true;
    		}
    		
    		final int childShift = child.getShift();
    		if (childShift < 0 || i == 0) {
    			childVerticalShift += childShift;
    		}
    		final int contentShift = child.getContent().getY() - getSpaceAround();
    		childVerticalShift -= contentShift;
    		
    		y += additionalCloudHeigth/2;
    		final int childHGap = child.getContent().isVisible() ? child.getHGap() : 0;
			final int x;
			if(child.isLeft())
				x = - childHGap - child.getContent().getX() - child.getContent().getWidth();
			else
				x = contentWidth + childHGap - child.getContent().getX();
    		left = Math.min(left, x);
    		if (childShift < 0) {
    			data.lx[i] = x; data.ly[i] = y;
    				y -= childShift;
    		}
    		else {
    			if(i > 0)
    				y += childShift;
    			data.lx[i] = x; data.ly[i] = y;
    		}
    		final int childHeight = child.getHeight() - 2 * getSpaceAround();
    		if (childHeight != 0) {
    			y += childHeight + getVGap() + additionalCloudHeigth/2;
    		}
    	}
    	if (getView().isContentVisible()) {
    		final Dimension contentPreferredSize = getContent().getPreferredSize();
    		final int contentVerticalShift = (contentPreferredSize.height - childContentHeight) / 2;
    		childVerticalShift += contentVerticalShift;
    	}
    	setData(data, isLeft, left, childContentHeight, childVerticalShift);
    }
	
	private void setData(final LayoutData data, boolean isLeft, int left, int childContentHeight, int childVerticalShift) {
		if(!isLeft && data.leftDataSet || isLeft && data.rightDataSet){
			data.left = Math.min(data.left, left);
			data.childContentHeight = Math.max(data.childContentHeight, childContentHeight);
			int deltaVerticalShift = childVerticalShift - data.childVerticalShift;
			final boolean changeLeft;
			if(deltaVerticalShift < 0){
				data.childVerticalShift = childVerticalShift;
				changeLeft = !isLeft;
				deltaVerticalShift = - deltaVerticalShift;
			}
			else{
				changeLeft = isLeft;
			}
			for(int i = 0; i < getChildCount(); i++){
				NodeView child = (NodeView) getView().getComponent(i);
				if(child.isLeft() == changeLeft){
					data.ly[i] += deltaVerticalShift;
				}
			}
		}
		else{
			data.left = left;
			data.childContentHeight = childContentHeight;
			data.childVerticalShift = childVerticalShift;
		}
		if(isLeft)
			data.leftDataSet = true;
		else
			data.rightDataSet = true;
    }
	

	protected void placeChildren(final LayoutData data) {
    	int childContentHeight = data.childContentHeight;
    	int left = data.left;
    	int childVerticalShift = data.childVerticalShift;
    	final int contentHeight;
    	final NodeView view = getView();
    	final int contentX = Math.max(getSpaceAround(), -left);
    	final int contentY = getSpaceAround() + Math.max(0, -childVerticalShift);
		if (view.isContentVisible()) {
     		contentHeight = contentPreferredSize.height;
    	}
    	else {
    		contentHeight = childContentHeight;
    	}
    	
    	if (getView().isContentVisible()) {
    		getContent().setVisible(true);
    	}
    	else {
    		getContent().setVisible(false);
    	}
    	getContent().setBounds(contentX, contentY, contentWidth, contentHeight);
    	
    	final int baseY = contentY - getSpaceAround() + data.childVerticalShift;
    
    	final int bottomContent = contentY + contentHeight + getSpaceAround();
    	int width =  contentX + contentWidth + getSpaceAround();
    	int bottomLeft = bottomContent;
    	int bottomRight = bottomContent;
    	for (int i = 0; i < getChildCount(); i++) {
    		NodeView child = (NodeView) getView().getComponent(i);
			child.setLocation(contentX + data.lx[i], baseY + data.ly[i]);
    		if(child.isLeft()){
    			bottomLeft = child.getY() + child.getHeight();
    		}
    		else{
    			bottomRight = child.getY() + child.getHeight();
    		}
    		width = Math.max(width, child.getX() + child.getWidth());
    		
    	}
    	
    	getView().setSize(width,
    		Math.max(bottomContent, Math.max(bottomLeft, bottomRight)));
    }
}
