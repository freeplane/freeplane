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

import org.freeplane.features.common.cloud.CloudModel;
import org.freeplane.features.common.map.FlexibleLayout.Type;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.view.swing.map.cloud.CloudView;

abstract public class NodeViewLayoutAdapter implements INodeViewLayout {
    protected static class LayoutData{
            final int[] lx;
            final int[] ly;
            int left;
            int childContentHeight;
            int top;
            int topOverlap;
            int bottomOverlap;
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
    private Dimension contentPreferredSize;
    private int contentWidth;
    private Type branchesOverlap;
    
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
        
        final int[] groupStart = new int[highestSummaryLevel];
        final int[] groupStartContentHeightSum = new int[highestSummaryLevel];
        final int[] groupStartVisibleChildCounter = new int[highestSummaryLevel];
        final int[] groupStartY = new int[highestSummaryLevel];
        final int[] groupEndY = new int[highestSummaryLevel];
        
        final int summaryBaseX[] = new int[highestSummaryLevel];
        
        level = highestSummaryLevel;
        int top = 0;
        boolean firstChild = true;
        for (int i = 0; i < getChildCount(); i++) {
            final NodeView child = (NodeView) getView().getComponent(i);
            if (child.isLeft() != isLeft) {
                continue;
            }
            
            final boolean isSummary = child.isSummary();
            final boolean isItem = ! isSummary || firstChild;
            firstChild = false;
            int oldLevel = level;
            if(isItem){
                level = 0;
            }
            else{
                level++;
            }
                
            
            final int childCloudHeigth = getAdditionalCloudHeigth(child);
            final int childContentHeight = child.getContent().getHeight();
            final int childShiftY = child.getShift();
            final int childContentShift = child.getContent().getY() - getSpaceAround();
            final int childHGap = child.getContent().isVisible() ? child.getHGap() : 0;
            final int childHeight = child.getHeight() - 2 * getSpaceAround();
            final int childSummedContentHeight = child.getSummedContentHeight();
            final int childTopOverlap; 
            final int childBottomOverlap;
            if(childSummedContentHeight > childContentHeight){
                childTopOverlap = childContentShift - (childSummedContentHeight - childContentHeight)/2;
                childBottomOverlap = childHeight - childContentShift - (childSummedContentHeight + childContentHeight)/2;
            }
            else{
                childTopOverlap = childContentShift;
                childBottomOverlap = childHeight - childContentShift - childContentHeight;
            }
            
            if(isItem){
                if(branchesOverlap != null)
                    y -= childTopOverlap;
                else
                    top -= childTopOverlap;
                if(Type.SIBLINGS.equals(branchesOverlap) || childShiftY > 0)
                    y += childShiftY;
                else
                    top += childShiftY;


                if(oldLevel > 0){
                    summaryBaseX[0] = 0;
                    for(int j = 0; j < oldLevel; j++){
                        groupStart[j] = i;
                        groupStartY[j] = y + childContentShift;
                        groupEndY[j] = y + childContentShift + childContentHeight;
                        groupStartContentHeightSum[j] = childContentHeightSum;
                        groupStartVisibleChildCounter[j] = visibleChildCounter;
                    }
                }
                else if(child.isFirstGroupNode()){
                    groupStartContentHeightSum[0] = childContentHeightSum;
                    groupStartVisibleChildCounter[0] = visibleChildCounter;
                    summaryBaseX[0] = 0;
                    groupStartY[0] = y + childContentShift;
                    groupEndY[0] = y + childContentShift + childContentHeight;
                    groupStart[0] = i;
                }
                for(int j = 0; j < highestSummaryLevel; j++){
                    groupStartY[j] = Math.min(groupStartY[j],y + childContentShift + childContentHeight);
                }
                childContentHeightSum += Math.max(childContentHeight, childSummedContentHeight) + childCloudHeigth;
                if (child.getHeight() - 2 * getSpaceAround() != 0) {
                    if (visibleChildCounter > 0)
                        childContentHeightSum +=  getVGap();
                    visibleChildCounter++;
                }

                y += childCloudHeigth/2;

                data.ly[i] = y;
                groupEndY[0] = Math.max(y + childContentShift + childContentHeight, groupEndY[0]);
                if (childHeight != 0) {
                    y += childHeight + getVGap() + childCloudHeigth/2;
                }
                
                if(branchesOverlap != null)
                    y -= childBottomOverlap;
                if(Type.SIBLINGS.equals(branchesOverlap) || childShiftY < 0)
                    y -= childShiftY;
                
                final int x;
                if(child.isLeft()){
                    x = - childHGap - child.getContent().getX() - child.getContent().getWidth();
                    summaryBaseX[0] = Math.min(summaryBaseX[0], x + getSpaceAround());
                }
                else{
                    x = contentWidth + childHGap - child.getContent().getX();
                    summaryBaseX[0] = Math.max(summaryBaseX[0], x + child.getWidth() - 2 * getSpaceAround());
                }
                data.lx[i] = x; 
                left = Math.min(left, x);
            }
            else{
                final int itemLevel = level - 1;
                if(child.isFirstGroupNode()){
                    groupStartContentHeightSum[level] = groupStartContentHeightSum[itemLevel];
                    groupStartVisibleChildCounter[level] = groupStartVisibleChildCounter[itemLevel];
                    summaryBaseX[level] = 0;
                    groupStartY[level] = groupStartY[itemLevel];
                    groupEndY[level] = groupEndY[itemLevel];
                    groupStart[level] = groupStart[itemLevel];
                }
                
                final int groupHeight = groupEndY[itemLevel] - groupStartY[itemLevel];
                int summaryY = groupStartY[itemLevel] + (groupHeight - childContentHeight)/2- childContentShift + childShiftY;
                final int deltaY;
                final int summaryContentHeight = Math.max(childContentHeight, childSummedContentHeight);
                if(Type.CHILDREN.equals(branchesOverlap)){
                    final int delta1 = (summaryContentHeight - (childContentHeightSum - groupStartContentHeightSum[itemLevel]))/2;
                    final int delta2 = groupStartY[itemLevel] - summaryY;
                    deltaY = Math.min( delta1, delta2);
                 }
                else{
                    deltaY = groupStartY[itemLevel] - summaryY;
                }
                if(deltaY > 0){
                    for(int j = groupStart[itemLevel]; j < i; j++){
                        NodeView groupItem = (NodeView) getView().getComponent(j);
                        if(groupItem.isLeft() == isLeft)
                            data.ly[j]+=deltaY;
                    }
                    top -= deltaY;
                    y += deltaY;
                    summaryY += deltaY;
                }
                   
                final int summaryEnd; 
                groupEndY[level] = Math.max(summaryY + childContentShift + childContentHeight, groupEndY[level]);
                data.ly[i] = summaryY;
                if(branchesOverlap == null)
                    summaryEnd = summaryY + childHeight;
                else{
                    summaryEnd = Math.min(summaryY + childHeight, summaryY  - childShiftY - childBottomOverlap + summaryContentHeight);
                }
                y = Math.max(y, summaryEnd);
                final int x;
                if(child.isLeft()){
                    x = summaryBaseX[itemLevel] - childHGap - child.getContent().getX() - child.getContent().getWidth();
                    summaryBaseX[level] = Math.min(summaryBaseX[level], x + getSpaceAround());
                }
                else{
                    x = summaryBaseX[itemLevel] + childHGap - child.getContent().getX() + getSpaceAround();
                    summaryBaseX[level] = Math.max(summaryBaseX[level], x + child.getWidth() - 2 * getSpaceAround());
                }
                left = Math.min(left, x);
                data.lx[i] = x; 
            }
        }
        final NodeView view = getView();
        if (view.isContentVisible()) {
            top += (contentPreferredSize.height - childContentHeightSum)/2;
        }

        int minY = Integer.MAX_VALUE;
        for (int i = 0; i < getChildCount(); i++) {
            final NodeView child = (NodeView) getView().getComponent(i);
            if (child.isLeft() != isLeft) {
                continue;
            }
            minY = Math.min(data.ly[i], minY); 
        }
        for (int i = 0; i < getChildCount(); i++) {
            final NodeView child = (NodeView) getView().getComponent(i);
            if (child.isLeft() != isLeft) {
                continue;
            }
            data.ly[i] -= minY;
        }
        
        if(minY != Integer.MAX_VALUE){
            top += minY;
        }

        setData(data, isLeft, left, childContentHeightSum, top);
    }
    
    private void setData(final LayoutData data, boolean isLeft, int left, int childContentHeight, int top) {
        if(!isLeft && data.leftDataSet || isLeft && data.rightDataSet){
            int deltaY = top - data.top;
            final boolean changeLeft;
            if(deltaY < 0){
                changeLeft = !isLeft;
                deltaY = - deltaY;
                data.top = top;
            }
            else{
                changeLeft = isLeft;
            }
            for(int i = 0; i < getChildCount(); i++){
                NodeView child = (NodeView) getView().getComponent(i);
                if(child.isLeft() == changeLeft){
                    data.ly[i] += deltaY;
                }
            }
            data.childContentHeight = Math.max(data.childContentHeight, childContentHeight);
            data.left = Math.min(data.left, left);
        }
        else{
            data.top = top;
            data.childContentHeight = childContentHeight;
            data.left = left;
        }
        if(isLeft)
            data.leftDataSet = true;
        else
            data.rightDataSet = true;
    }
    

    protected void placeChildren(final LayoutData data) {
        final int contentHeight;
        final NodeView view = getView();
        if (view.isContentVisible()) {
            contentHeight = contentPreferredSize.height;
        }
        else {
            contentHeight = data.childContentHeight;
        }
        
        if (getView().isContentVisible()) {
            getContent().setVisible(true);
        }
        else {
            getContent().setVisible(false);
        }
        final int contentX = Math.max(getSpaceAround(), -data.left);
        final int contentY = getSpaceAround() + Math.max(0, -data.top);
        getContent().setBounds(contentX, contentY, contentWidth, contentHeight);
        
        final int baseY = Math.max(0, data.top);
    
        int width =  contentX + contentWidth + getSpaceAround();
        int height = contentY + contentHeight + getSpaceAround();
        for (int i = 0; i < getChildCount(); i++) {
            NodeView child = (NodeView) getView().getComponent(i);
            child.setLocation(contentX + data.lx[i], baseY + data.ly[i]);
            width = Math.max(width, child.getX() + child.getWidth());
            height = Math.max(height, child.getY() + child.getHeight());
        }
        
        view.setSize(width, height);
        view.setSummedContentHeight(data.childContentHeight);
    }
    public void setBranchesOverlap(Type type) {
        branchesOverlap = type;
    }
}
