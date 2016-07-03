/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008-2014 Dimitry Polivaev
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
import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.JComponent;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.SummaryLevels;
import org.freeplane.features.nodelocation.LocationModel;

class VerticalNodeViewLayoutStrategy {
	
	static private boolean wrongChildComponentsReported = false;
	
	private int childViewCount;
	private final int spaceAround;
	private final NodeView view;

	private final int[] xCoordinates;
	private final int[] yCoordinates;
	private final boolean[] isChildFreeNode;
	private SummaryLevels viewLevels;
	private int left;
	private int childContentHeight;
	private int top;
	private boolean rightSideCoordinatesAreSet;
	private boolean leftSideCoordinaresAreSet;

	public VerticalNodeViewLayoutStrategy(NodeView view) {
		this.view = view;
		childViewCount = view.getComponentCount() - 1;
		layoutChildViews(view);
		this.left = 0;
		this.childContentHeight = 0;
		this.top = 0;
		rightSideCoordinatesAreSet = false;
		leftSideCoordinaresAreSet = false;
		this.xCoordinates = new int[childViewCount];
		this.yCoordinates = new int[childViewCount];
		this.isChildFreeNode = new boolean[childViewCount];
		spaceAround = view.getSpaceAround();
	}

	private void layoutChildViews(NodeView view) {
		for (int i = 0; i < childViewCount; i++) {
			final Component component = view.getComponent(i);
			if(component instanceof NodeView)
				((NodeView) component).validateTree();
			else {
				childViewCount = i;
				if(! wrongChildComponentsReported) {
					wrongChildComponentsReported = true;
					final String wrongChildComponents = Arrays.toString(view.getComponents());
					LogUtils.severe("Unexpected child components:" + wrongChildComponents, new Exception());
				}
			}
		}
	}

	private void setFreeChildNodes(final boolean isLeft) {
		for (int i = 0; i < childViewCount; i++) {
			final NodeView child = (NodeView) view.getComponent(i);
			if (child.isLeft() == isLeft)
				this.isChildFreeNode[i] = child.isFree();
		}
	}
	public void calculateLayoutData() {
		viewLevels = new SummaryLevels(view.getModel());
		for(boolean isLeft : viewLevels.sides)
			calculateLayoutData(isLeft);
		applyLayoutToChildComponents();
	}
	
	private void calculateLayoutData(final boolean isLeft) {
		setFreeChildNodes(isLeft);
		calculateLayoutY(isLeft);
		calculateLayoutX(isLeft);

	}

	private void calculateLayoutY(final boolean isLeft) {
		final int minimalDistanceBetweenChildren = view.getChildDistanceContainer().getMinimalDistanceBetweenChildren();
		final Dimension contentSize = ContentSizeCalculator.INSTANCE.calculateContentSize(view);
		int childContentHeightSum = 0;
		int top = 0;
		int level = viewLevels.highestSummaryLevel + 1;
		int y = 0;
		int vGap = 0;
		int visibleChildCounter = 0;
		final int[] groupStartIndex = new int[level];
		final int[] contentHeightSumAtGroupStart = new int[level];
		final int[] groupUpperYCoordinate = new int[level];
		final int[] groupLowerYCoordinate = new int[level];

		for (int childViewIndex = 0; childViewIndex < childViewCount; childViewIndex++) {
			final NodeView child = (NodeView) view.getComponent(childViewIndex);
			if (child.isLeft() == isLeft) {
				final int childHeight = child.getHeight() - 2 * spaceAround;
				final int oldLevel = level;
				if(childViewIndex >= viewLevels.summaryLevels.length){
					final String errorMessage = "Bad node view child components: missing node for component " + childViewIndex;
					UITools.errorMessage(errorMessage);
					System.err.println(errorMessage);
					for (int i = 0; i < view.getComponentCount(); i++){
						final Component component = view.getComponent(i);
						System.err.println(component);
					}
				}
				level = viewLevels.summaryLevels[childViewIndex];
				boolean isFreeNode = child.isFree();
				boolean isItem = level == 0;

				final int childCloudHeigth = CloudHeightCalculator.INSTANCE.getAdditionalCloudHeigth(child);
				final int childContentHeight = child.getContent().getHeight() + childCloudHeigth;
				final int childShiftY = child.isContentVisible() ? child.getShift() : 0;
				final int childContentShift = child.getContent().getY() - childCloudHeigth / 2 - spaceAround;

				if (isItem) {
					if (isFreeNode)
						this.yCoordinates[childViewIndex] = childShiftY - childContentShift - childCloudHeigth / 2 - spaceAround;
					else {
						if (childHeight != 0) {
							if (visibleChildCounter > 0)
								childContentHeightSum += vGap;
						}
						if (childShiftY < 0 || visibleChildCounter == 0)
							top += childShiftY;

						top += - childContentShift + child.getTopOverlap();
						y -= child.getTopOverlap();
						if (childShiftY < 0) {
							this.yCoordinates[childViewIndex] = y;
							y -= childShiftY;
						} else {
							if (visibleChildCounter > 0)
								y += childShiftY;
							this.yCoordinates[childViewIndex] = y;
						}
						final int summaryNodeIndex = viewLevels.findSummaryNodeIndex(childViewIndex);
						if(summaryNodeIndex == SummaryLevels.NODE_NOT_FOUND || summaryNodeIndex - 1 == childViewIndex)
							vGap = minimalDistanceBetweenChildren;
						else if (childHeight != 0)
							vGap = summarizedNodeDistance(minimalDistanceBetweenChildren);
						if (childHeight != 0)
							y += childHeight + vGap - child.getBottomOverlap();

						childContentHeightSum += childContentHeight;
						if (oldLevel > 0) {
							for (int j = 0; j < oldLevel; j++) {
								groupStartIndex[j] = childViewIndex;
								groupUpperYCoordinate[j] = Integer.MAX_VALUE;
								groupLowerYCoordinate[j] = Integer.MIN_VALUE;
								contentHeightSumAtGroupStart[j] = childContentHeightSum;
							}
						} else if (child.isFirstGroupNode()) {
							contentHeightSumAtGroupStart[0] = childContentHeightSum;
							groupStartIndex[0] = childViewIndex;
						}
					}
					if (childHeight != 0)
						visibleChildCounter++;
				} else {
					final int itemLevel = level - 1;
					if (child.isFirstGroupNode()) {
						contentHeightSumAtGroupStart[level] = contentHeightSumAtGroupStart[itemLevel];
						groupStartIndex[level] = groupStartIndex[itemLevel];
					}
					int summaryY = (groupUpperYCoordinate[itemLevel] + groupLowerYCoordinate[itemLevel]) / 2 
							- childContentHeight / 2 + childShiftY
							- (child.getContent().getY() - childCloudHeigth / 2 - spaceAround);
					this.yCoordinates[childViewIndex] = summaryY;
					if (!isFreeNode) {
						final int deltaY = summaryY - groupUpperYCoordinate[itemLevel]
								+ child.getTopOverlap();
						if (deltaY < 0) {
							top += deltaY;
							y -= deltaY;
							summaryY -= deltaY;
							for (int j = groupStartIndex[itemLevel]; j <= childViewIndex; j++) {
								NodeView groupItem = (NodeView) view.getComponent(j);
								if (groupItem.isLeft() == isLeft
										&& (this.viewLevels.summaryLevels[j] > 0 || !this.isChildFreeNode[j]))
									this.yCoordinates[j] -= deltaY;
							}
						}
						if (childHeight != 0) {
							summaryY += childHeight + minimalDistanceBetweenChildren
									- child.getBottomOverlap();
						}
						y = Math.max(y, summaryY);
					}
				}
				if (! (isItem && isFreeNode)) {
					int childUpperCoordinate = this.yCoordinates[childViewIndex] + child.getTopOverlap();
					int childBottomCoordinate = this.yCoordinates[childViewIndex] + childHeight - child.getBottomOverlap();
					if (child.isFirstGroupNode()) {
						if(isItem){
							groupUpperYCoordinate[level] = Integer.MAX_VALUE;
							groupLowerYCoordinate[level] = Integer.MIN_VALUE;
						}
						else{
							groupUpperYCoordinate[level] = childUpperCoordinate;
							groupLowerYCoordinate[level] = childBottomCoordinate;
						}
					} else if (childHeight != 0 || isNextNodeSummaryNode(childViewIndex)){
						groupUpperYCoordinate[level] = Math.min(groupUpperYCoordinate[level], childUpperCoordinate);
						groupLowerYCoordinate[level] = Math.max(childBottomCoordinate, groupLowerYCoordinate[level]);
					}
				}
			}
		}
		top += (contentSize.height - childContentHeightSum) / 2;
		calculateRelativeCoordinatesForContentAndBothSides(isLeft, childContentHeightSum, top);
	}

	private boolean isNextNodeSummaryNode(int childViewIndex) {
		return childViewIndex + 1 < viewLevels.summaryLevels.length && viewLevels.summaryLevels[childViewIndex + 1] > 0;
	}

	private int summarizedNodeDistance(final int distance) {
		final int defaultVGap = view.getMap().getZoomed(LocationModel.DEFAULT_VGAP.toBaseUnits());
		if(defaultVGap >= distance)
			return distance;
		else
			return defaultVGap + (distance - defaultVGap) / 6;
	}

	private void calculateLayoutX(final boolean isLeft) {
		final Dimension contentSize = ContentSizeCalculator.INSTANCE.calculateContentSize(view);
		int level = viewLevels.highestSummaryLevel + 1;
		final int summaryBaseX[] = new int[level];
		for (int i = 0; i < childViewCount; i++) {
			final NodeView child = (NodeView) view.getComponent(i);
			if (child.isLeft() == isLeft) {
				final int oldLevel = level;
				level = viewLevels.summaryLevels[i];
				boolean isFreeNode = child.isFree();
				boolean isItem = level == 0;
				int childHGap;
				if (child.isContentVisible())
					childHGap = child.getHGap();
				else if (child.isSummary())
					childHGap = child.getZoomed(LocationModel.DEFAULT_HGAP_PX*7/12);
				else
					childHGap = 0;
				if(view.getModel().isHiddenSummary() && ! child.getModel().isHiddenSummary())
					childHGap -= child.getZoomed(LocationModel.DEFAULT_HGAP_PX*7/12);

				if (isItem) {
					if (!isFreeNode && (oldLevel > 0 || child.isFirstGroupNode()))
						summaryBaseX[0] = 0;
				} 
				else if (child.isFirstGroupNode())
					summaryBaseX[level] = 0;


				final int x;
				final int baseX;
				if (level > 0)
					baseX = summaryBaseX[level - 1];
				else {
					if (child.isLeft() != (isItem && isFreeNode)) {
						baseX = 0;
					} else {
						baseX = contentSize.width;
					}
				}
				if (child.isLeft()) {
					x = baseX - childHGap - child.getContent().getX() - child.getContent().getWidth();
					summaryBaseX[level] = Math.min(summaryBaseX[level], x + spaceAround);
				} else {
					x = baseX + childHGap - child.getContent().getX();
					summaryBaseX[level] = Math.max(summaryBaseX[level], x + child.getWidth() - spaceAround);
				}
				left = Math.min(left, x);
				this.xCoordinates[i] = x;
			}
		}
	}

	private void calculateRelativeCoordinatesForContentAndBothSides(boolean isLeft, int childContentHeightOnSide,  int topOnSide) {
		if (! (leftSideCoordinaresAreSet || rightSideCoordinatesAreSet)) {
			childContentHeight = childContentHeightOnSide;
			top = topOnSide;
		} else {
			childContentHeight = Math.max(this.childContentHeight, childContentHeightOnSide);
			int deltaTop = topOnSide - this.top;
			final boolean changeLeft;
			if (deltaTop < 0) {
				top = topOnSide;
				changeLeft = !isLeft;
				deltaTop = -deltaTop;
			} else {
				changeLeft = isLeft;
			}
			for (int i = 0; i < childViewCount; i++) {
				NodeView child = (NodeView) view.getComponent(i);
				if (child.isLeft() == changeLeft
						&& (viewLevels.summaryLevels[i] > 0 || !isChildFreeNode[i])) {
					yCoordinates[i] += deltaTop;
				}
			}
		}
		if (isLeft)
			leftSideCoordinaresAreSet = true;
		else
			rightSideCoordinatesAreSet = true;
	}

	private void applyLayoutToChildComponents() {
		JComponent content = view.getContent();
		int spaceAround = view.getSpaceAround();
		final int contentX = Math.max(spaceAround, -this.left);
		int cloudHeight = CloudHeightCalculator.INSTANCE
				.getAdditionalCloudHeigth(view);
		int contentY = spaceAround + cloudHeight / 2 - Math.min(0, this.top);

		content.setVisible(view.isContentVisible());

		int baseY = contentY - spaceAround + this.top;
		int minY = 0;
		for (int i = 0; i < childViewCount; i++) {
			if (this.viewLevels.summaryLevels[i] == 0 && this.isChildFreeNode[i]) {
				minY = Math.min(minY, contentY + this.yCoordinates[i]);
			} else
				minY = Math.min(minY, baseY + this.yCoordinates[i]);
		}
		if (minY < 0) {
			contentY -= minY;
			baseY -= minY;
		}
		final Dimension contentSize = ContentSizeCalculator.INSTANCE
				.calculateContentSize(view);
		int width = contentX + contentSize.width + spaceAround;
		int height = contentY + contentSize.height + cloudHeight / 2
				+ spaceAround;
		content.setBounds(contentX, contentY, contentSize.width,
				contentSize.height);
		int topOverlap = -minY;
		int heigthWithoutOverlap = height;
		for (int i = 0; i < childViewCount; i++) {
			NodeView child = (NodeView) view.getComponent(i);
			final int y;
			if (this.viewLevels.summaryLevels[i] == 0 && this.isChildFreeNode[i]) {
				y = contentY + this.yCoordinates[i];
			} else {
				y = baseY + this.yCoordinates[i];
				if (!this.isChildFreeNode[i])
					heigthWithoutOverlap = Math.max(
							heigthWithoutOverlap,
							y + child.getHeight() + cloudHeight / 2
									- child.getBottomOverlap());
			}
			final int x = contentX + this.xCoordinates[i];
			child.setLocation(x, y);
			width = Math.max(width, child.getX() + child.getWidth());
			height = Math.max(height, y + child.getHeight() + cloudHeight / 2);
		}

		view.setSize(width, height);
		view.setTopOverlap(topOverlap);
		view.setBottomOverlap(height - heigthWithoutOverlap);
	}

}