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

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodelocation.LocationModel;

public class NodeViewLayoutAdapter{
    final static Dimension minDimension = new Dimension(0, 0);
    private int childCount;
    protected Point location = new Point();
    private NodeModel model;
    private int spaceAround;
    private int vGap;
    private NodeView view;
	public static final ImmediatelyValidatingPreferredSizeCalculator immediatelyValidatingPreferredSizeCalculator  = new ImmediatelyValidatingPreferredSizeCalculator();
    /**
     * @return Returns the model.
     */
    protected NodeModel getModel() {
        return model;
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

     protected boolean setUp(final Container c) {
        final NodeView localView = (NodeView) c;
        JComponent content = localView.getContent();
        if(content == null)
        	return false;
        final int localChildCount = localView.getComponentCount() - 1;
        for (int i = 0; i < localChildCount; i++) {
            final Component component = localView.getComponent(i);
            ((NodeView) component).validateTree();
        }
        view = localView;
        model = localView.getModel();
        childCount = localChildCount;
         if (getModel().isVisible()) {
            setVGap(getView().getVGap());
        }
        else {
            setVGap(getView().getVisibleParentView().getVGap());
        }
        spaceAround = view.getSpaceAround();
		return true;
    }

	protected void shutDown() {
        view = null;
        model = null;
        childCount = 0;
        setVGap(0);
        spaceAround = 0;
    }

    public void setVGap(final int vGap) {
        this.vGap = vGap;
    }

    protected void calcLayout(final boolean isLeft, final LayoutData data) {
        int highestSummaryLevel = 1;
        int level = 1;
        for (int i = 0; i < childCount; i++) {
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
        
        final Dimension contentSize = ContentSizeCalculator.INSTANCE.calculateContentSize(view);
        int childContentHeightSum = 0;
        int visibleChildCounter = 0;
        boolean useSummaryAsItem = true;
        int top = 0;
        
        final int[] groupStart = new int[highestSummaryLevel];
        final int[] groupStartContentHeightSum = new int[highestSummaryLevel];
        final int[] groupStartY = new int[highestSummaryLevel];
        final int[] groupEndY = new int[highestSummaryLevel];
        
        final int summaryBaseX[] = new int[highestSummaryLevel];
        
        level = highestSummaryLevel;
        for (int i = 0; i < childCount; i++) {
            final NodeView child = (NodeView) getView().getComponent(i);
            if (child.isLeft() != isLeft) {
                continue;
            }
            
            final boolean isSummary = child.isSummary();
            final boolean isItem = !isSummary || useSummaryAsItem;
            final int oldLevel = level;
            if(isItem){
            	if(level > 0)
            		useSummaryAsItem = true;
                level = 0;
            }
            else{
                level++;
            }
                
            
            final int childCloudHeigth = CloudHeightCalculator.INSTANCE.getAdditionalCloudHeigth(child);
            final int childContentHeight = child.getContent().getHeight() + childCloudHeigth;
            final int childShiftY = child.isContentVisible() ? child.getShift() : 0;
            final int childContentShift = child.getContent().getY() -childCloudHeigth/2 - spaceAround;
            int childHGap;
            if(child.isContentVisible())
            	childHGap =  child.getHGap(); 
            else if(child.isSummary())
            	childHGap = child.getZoomed(LocationModel.HGAP);
            else
            	childHGap = 0;
            final int childHeight = child.getHeight() - 2 * spaceAround;
            
            boolean isFreeNode = child.isFree();
			data.free[i] = isFreeNode;
			data.summary[i] = ! isItem;
			if(isItem) {
				if (isFreeNode){
				data.ly[i] = childShiftY - childContentShift-childCloudHeigth/2 - spaceAround;
				}
				else{
					if (childShiftY < 0 || visibleChildCounter == 0) {
						top += childShiftY;
					}
                top -= childContentShift;

                top += child.getTopOverlap();
                y -= child.getTopOverlap();
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
                    y += childHeight + getVGap();
                    y -= child.getBottomOverlap();
                }
                
                childContentHeightSum += childContentHeight;
                if(oldLevel > 0){
                    summaryBaseX[0] = 0;
                    for(int j = 0; j < oldLevel; j++){
                        groupStart[j] = i;
                        groupStartY[j] = Integer.MAX_VALUE;
                        groupEndY[j] = Integer.MIN_VALUE;
                        groupStartContentHeightSum[j] = childContentHeightSum;
                    }
                }
                else if(child.isFirstGroupNode()){
                    groupStartContentHeightSum[0] = childContentHeightSum;
                    summaryBaseX[0] = 0;
                    groupStart[0] = i;
                }
                if (childHeight != 0) {
                    if (visibleChildCounter > 0)
                        childContentHeightSum +=  getVGap();
                }
				}
	            if (childHeight != 0) {
	                visibleChildCounter++;
	                useSummaryAsItem = false;
				}
			}
            else{
                final int itemLevel = level - 1;
                if(child.isFirstGroupNode()){
                    groupStartContentHeightSum[level] = groupStartContentHeightSum[itemLevel];
                    summaryBaseX[level] = 0;
                    groupStart[level] = groupStart[itemLevel];
                }
                int summaryY = (groupStartY[itemLevel] + groupEndY[itemLevel] ) / 2 - childContentHeight / 2 + childShiftY - (child.getContent().getY() - childCloudHeigth/2 - spaceAround);
                data.ly[i] = summaryY;
                if(!isFreeNode ){
                	final int deltaY = summaryY - groupStartY[itemLevel] + child.getTopOverlap();
                	if(deltaY < 0){
                		top += deltaY;
                		y -= deltaY;
                		summaryY -= deltaY;
                		for(int j = groupStart[itemLevel]; j <= i; j++){
                			NodeView groupItem = (NodeView) getView().getComponent(j);
                			if(groupItem.isLeft() == isLeft && (data.summary[j] || !data.free[j]))
                				data.ly[j]-=deltaY;
                		}
                	}
                	if (childHeight != 0) {
                		summaryY += childHeight + getVGap() - child.getBottomOverlap();
                	}
                	y = Math.max(y, summaryY);
                	final int summaryContentHeight = groupStartContentHeightSum[itemLevel] + childContentHeight;
                	if(childContentHeightSum  < summaryContentHeight){
                		childContentHeightSum = summaryContentHeight;
                	}
                }
            }
			if(! isItem || ! isFreeNode){
				if(child.isFirstGroupNode()){
					groupStartY[level] = data.ly[i] + child.getTopOverlap();
					groupEndY[level] = data.ly[i] + childHeight - child.getBottomOverlap();
				}
				else{
					groupStartY[level] = Math.min(groupStartY[level],data.ly[i] + child.getTopOverlap());
					groupEndY[level] = Math.max(data.ly[i] + childHeight - child.getBottomOverlap(), groupEndY[level]);
				}
			}
            final int x;
            final int baseX;
            if(level > 0)
                baseX = summaryBaseX[level - 1];
            else{
                if(child.isLeft() != (isItem && isFreeNode)){
                    baseX = 0;
                }
                else{
                    baseX = contentSize.width;
                }
            }
            if(child.isLeft()){
                x = baseX - childHGap - child.getContent().getX() - child.getContent().getWidth();
                summaryBaseX[level] = Math.min(summaryBaseX[level], x + spaceAround);
            }
            else{
                x = baseX + childHGap - child.getContent().getX();
                summaryBaseX[level] = Math.max(summaryBaseX[level], x + child.getWidth() - spaceAround);
            }
            left = Math.min(left, x);
            data.lx[i] = x; 
        }
        
        top += (contentSize.height - childContentHeightSum) / 2;
        data.finalizeDataSet(view, isLeft, left, childContentHeightSum, top);
    }
}
