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

import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.view.swing.map.cloud.CloudView;

abstract public class NodeViewLayoutAdapter implements INodeViewLayout {
    protected static class LayoutData{
            final int[] lx;
            final int[] ly;
            int left;
            int childContentHeight;
            int top;
            boolean rightDataSet;
            boolean leftDataSet;
            public LayoutData(int childCount) {
                super();
                this.lx = new int[childCount];
                this.ly = new int[childCount];
                this.left = 0;
                this.childContentHeight = 0;
                this.top = 0;
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
    private int contentWidth;
    private int contentHeight;

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
        spaceAround = view.getSpaceAround();
		if (view.isContentVisible()) {
	        Dimension contentPreferredSize;
            contentPreferredSize = getContent().getPreferredSize();
            contentWidth = contentPreferredSize.width;
            contentHeight = contentPreferredSize.height;
        }
        else {
        	contentHeight = 0;
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
        final CloudModel cloud = node.getCloudModel();
        if (cloud != null) {
            return CloudView.getAdditionalHeigth(cloud, node);
        }
        else {
            return 0;
        }
    }

    protected void calcLayout(final boolean isLeft, final LayoutData data) {
        int highestSummaryLevel = 1;
        int level = 1;
        for (int i = 0; i < getChildCount(); i++) {
            final NodeView child = (NodeView) getView().getComponent(i);
            if (child.isLeft() != isLeft) {
                continue;
            }
            if(child.isSummary()){
                level++;
                highestSummaryLevel = Math.max(highestSummaryLevel, level);
            }
            else{
                level = 1;
            }
        }
        int left = 0;
        int y = 0;
        
        int childContentHeightSum = 0;
        int visibleChildCounter = 0;
        int top = 0;
        
        final int[] groupStart = new int[highestSummaryLevel];
        final int[] groupStartContentHeightSum = new int[highestSummaryLevel];
        final int[] groupStartVisibleChildCounter = new int[highestSummaryLevel];
        final int[] groupStartY = new int[highestSummaryLevel];
        final int[] groupEndY = new int[highestSummaryLevel];
        
        final int summaryBaseX[] = new int[highestSummaryLevel];
        
        level = highestSummaryLevel;
        for (int i = 0; i < getChildCount(); i++) {
            final NodeView child = (NodeView) getView().getComponent(i);
            if (child.isLeft() != isLeft) {
                continue;
            }
            
            final boolean isSummary = child.isSummary();
            final boolean isItem = ! (isSummary && visibleChildCounter > 0);
            final int oldLevel = level;
            if(isItem){
                level = 0;
            }
            else{
                level++;
            }
                
            
            final int childCloudHeigth = getAdditionalCloudHeigth(child);
            final int childContentHeight = child.getContent().getHeight();
            final int childShiftY = child.isContentVisible() ? child.getShift() : 0;
            final int childContentShift = child.getContent().getY() - getSpaceAround();
            final int childHGap = child.isContentVisible() ? child.getHGap() : 0;
            final int childHeight = child.getHeight() - 2 * getSpaceAround();
            
            if(isItem){
                if (childShiftY < 0 || visibleChildCounter == 0) {
                    top += childShiftY;
                }
                top -= childContentShift;

                y += childCloudHeigth/2;
                if (childShiftY < 0) {
                    data.ly[i] = y;
                    y -= childShiftY;
                }
                else {
                    if(visibleChildCounter > 0)
                        y += childShiftY;
                    data.ly[i] = y;
                }
                if (childHeight != 0) {
                    y += childHeight + getVGap() + childCloudHeigth/2;
                }
                
                childContentHeightSum += childContentHeight + childCloudHeigth;
                if (child.getHeight() - 2 * getSpaceAround() != 0) {
                    if (visibleChildCounter > 0)
                        childContentHeightSum +=  getVGap();
                    visibleChildCounter++;
                }
                if(oldLevel > 0){
                    summaryBaseX[0] = 0;
                    for(int j = 0; j < oldLevel; j++){
                        groupStart[j] = i;
                        groupStartY[j] = Integer.MAX_VALUE;
                        groupEndY[j] = Integer.MIN_VALUE;
                        groupStartContentHeightSum[j] = childContentHeightSum;
                        groupStartVisibleChildCounter[j] = visibleChildCounter;
                    }
                }
                else if(child.isFirstGroupNode()){
                    groupStartContentHeightSum[0] = childContentHeightSum;
                    groupStartVisibleChildCounter[0] = visibleChildCounter;
                    summaryBaseX[0] = 0;
                    groupStart[0] = i;
                }
           }
            else{
                final int itemLevel = level - 1;
                if(child.isFirstGroupNode()){
                    groupStartContentHeightSum[level] = groupStartContentHeightSum[itemLevel];
                    groupStartVisibleChildCounter[level] = groupStartVisibleChildCounter[itemLevel];
                    summaryBaseX[level] = 0;
                    groupStart[level] = groupStart[itemLevel];
                }
                int summaryY = (groupStartY[itemLevel] + groupEndY[itemLevel] ) / 2 - childContentHeight / 2 + childShiftY - (child.getContent().getY() - getSpaceAround());
                data.ly[i] = summaryY;
                final int deltaY = summaryY -childCloudHeigth/2 - groupStartY[itemLevel];
                if(deltaY < 0){
                    top += deltaY;
                    y -= deltaY;
                    summaryY -= deltaY;
                    for(int j = groupStart[itemLevel]; j <= i; j++){
                        NodeView groupItem = (NodeView) getView().getComponent(j);
                        if(groupItem.isLeft() == isLeft)
                            data.ly[j]-=deltaY;
                    }
                }
                if (childHeight != 0) {
                    summaryY += childHeight + getVGap() + childCloudHeigth/2;
                }
                y = Math.max(y, summaryY);
                final int summaryContentHeight = groupStartContentHeightSum[itemLevel] + childContentHeight;
                if(childContentHeightSum  < summaryContentHeight){
                    childContentHeightSum = summaryContentHeight;
                }
            }
            if(child.isFirstGroupNode()){
                groupStartY[level] = data.ly[i]-childCloudHeigth/2;
                groupEndY[level] = data.ly[i] + childHeight+childCloudHeigth/2;
            }
            else{
                groupStartY[level] = Math.min(groupStartY[level],data.ly[i]-childCloudHeigth/2);
                groupEndY[level] = Math.max(data.ly[i] + childHeight+childCloudHeigth/2, groupEndY[level]);
            }
            final int x;
            final int baseX;
            if(level > 0)
                baseX = summaryBaseX[level - 1];
            else{
                if(child.isLeft()){
                    baseX = 0;
                }
                else{
                    baseX = contentWidth;
                }
            }
            if(child.isLeft()){
                x = baseX - childHGap - child.getContent().getX() - child.getContent().getWidth();
                summaryBaseX[level] = Math.min(summaryBaseX[level], x + getSpaceAround());
            }
            else{
                x = baseX + childHGap - child.getContent().getX();
                summaryBaseX[level] = Math.max(summaryBaseX[level], x + child.getWidth() - getSpaceAround());
            }
            left = Math.min(left, x);
            data.lx[i] = x; 
        }
        
        top += (contentHeight - childContentHeightSum) / 2;
        setData(data, isLeft, left, childContentHeightSum, top);
    }
    
    private void setData(final LayoutData data, boolean isLeft, int left, int childContentHeight, int top) {
        if(!isLeft && data.leftDataSet || isLeft && data.rightDataSet){
            data.left = Math.min(data.left, left);
            data.childContentHeight = Math.max(data.childContentHeight, childContentHeight);
            int deltaTop = top - data.top;
            final boolean changeLeft;
            if(deltaTop < 0){
                data.top = top;
                changeLeft = !isLeft;
                deltaTop = - deltaTop;
            }
            else{
                changeLeft = isLeft;
            }
            for(int i = 0; i < getChildCount(); i++){
                NodeView child = (NodeView) getView().getComponent(i);
                if(child.isLeft() == changeLeft){
                    data.ly[i] += deltaTop;
                }
            }
        }
        else{
            data.left = left;
            data.childContentHeight = childContentHeight;
            data.top = top;
        }
        if(isLeft)
            data.leftDataSet = true;
        else
            data.rightDataSet = true;
    }
    

    protected void placeChildren(final LayoutData data) {
        final int contentX = Math.max(getSpaceAround(), -data.left);
        final int contentY= getSpaceAround() + Math.max(0, -data.top);
        
        if (getView().isContentVisible()) {
            getContent().setVisible(true);
        }
        else {
            getContent().setVisible(false);
        }
        getContent().setBounds(contentX, contentY, contentWidth, contentHeight);
        
        final int baseY = contentY - getSpaceAround() + data.top;
    
        int width =  contentX + contentWidth + getSpaceAround();
        int height = contentY + contentHeight + getSpaceAround();
        for (int i = 0; i < getChildCount(); i++) {
            NodeView child = (NodeView) getView().getComponent(i);
            child.setLocation(contentX + data.lx[i], baseY + data.ly[i]);
            width = Math.max(width, child.getX() + child.getWidth());
            height = Math.max(height, child.getY() + child.getHeight()+ getAdditionalCloudHeigth(child) / 2);
        }
        
        getView().setSize(width, height);
    }
}
