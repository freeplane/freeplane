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

import javax.swing.JComponent;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodelocation.LocationModel;

class VerticalNodeViewLayoutStrategy {
	private final int childViewCount;
	private final NodeModel model;
	private final int spaceAround;
	private final int vGap;
	private final NodeView view;

	private final int[] lx;
	private final int[] ly;
	private final boolean[] free;
	private final int[] levels;
	private int left;
	private int childContentHeight;
	private int top;
	private boolean rightDataSet;
	private boolean leftDataSet;

	public VerticalNodeViewLayoutStrategy(NodeView view) {
		this.view = view;
		childViewCount = view.getComponentCount() - 1;
		layoutChildViews(view);
		this.left = 0;
		this.childContentHeight = 0;
		this.top = 0;
		rightDataSet = false;
		leftDataSet = false;
		this.lx = new int[childViewCount];
		this.ly = new int[childViewCount];
		this.free = new boolean[childViewCount];
		this.levels = new int[childViewCount];
		model = view.getModel();
		if (model.isVisible()) {
			this.vGap = view.getVGap();
		} else {
			this.vGap = view.getVisibleParentView().getVGap();
		}
		spaceAround = view.getSpaceAround();
	}

	private void layoutChildViews(NodeView view) {
		for (int i = 0; i < childViewCount; i++) {
			final Component component = view.getComponent(i);
			((NodeView) component).validateTree();
		}
	}

	public void calcLeftLayout() {
		calcLayout(true);
	}

	public void calcRightLayout() {
		calcLayout(false);
	}

	private int calcHighestSummaryLevel(final boolean isLeft) {
		int highestSummaryLevel = 1;
		int level = 1;
		for (int i = 0; i < childViewCount; i++) {
			final NodeView child = (NodeView) view.getComponent(i);
			if (child.isLeft() != isLeft) {
				continue;
			}
			if (child.isSummary()) {
				level++;
				highestSummaryLevel = Math.max(highestSummaryLevel, level);
			} else {
				level = 1;
			}
		}
		level = highestSummaryLevel;
		return level;
	}
	
	private void calcFree(final boolean isLeft) {
		for (int i = 0; i < childViewCount; i++) {
			final NodeView child = (NodeView) view.getComponent(i);
			if (child.isLeft() == isLeft)
				this.free[i] = child.isFree();
		}
	}
	private void calcLevels(final boolean isLeft, int highestSummaryLevel) {
		int level = highestSummaryLevel;
		boolean useSummaryAsItem = true;
		for (int i = 0; i < childViewCount; i++) {
			final NodeView child = (NodeView) view.getComponent(i);
			if (child.isLeft() == isLeft) {
				final int childHeight = child.getHeight() - 2 * spaceAround;
				final boolean isItem = !child.isSummary() || useSummaryAsItem;
				if (isItem) {
					if (level > 0)
						useSummaryAsItem = true;
					level = 0;
					if (childHeight != 0) {
						useSummaryAsItem = false;
					}
				} else {
					level++;
				}
				levels[i] = level;
			}
		}
	}

	private void calcLayout(final boolean isLeft) {
		calcFree(isLeft);
		final int highestSummaryLevel = calcHighestSummaryLevel(isLeft);
		calcLevels(isLeft, highestSummaryLevel);
		
		int left = 0;
		int y = 0;
		final Dimension contentSize = ContentSizeCalculator.INSTANCE.calculateContentSize(view);
		int childContentHeightSum = 0;
		int visibleChildCounter = 0;
		int top = 0;
		
		int level = highestSummaryLevel;
		final int[] groupStart = new int[highestSummaryLevel];
		final int[] groupStartContentHeightSum = new int[highestSummaryLevel];
		final int[] groupStartY = new int[highestSummaryLevel];
		final int[] groupEndY = new int[highestSummaryLevel];

		final int summaryBaseX[] = new int[highestSummaryLevel];
		for (int i = 0; i < childViewCount; i++) {
			final NodeView child = (NodeView) view.getComponent(i);
			if (child.isLeft() != isLeft) {
				continue;
			}

			final int childHeight = child.getHeight() - 2 * spaceAround;
			final int oldLevel = level;
			level = levels[i];
			boolean isItem = level == 0;

			final int childCloudHeigth = CloudHeightCalculator.INSTANCE
					.getAdditionalCloudHeigth(child);
			final int childContentHeight = child.getContent().getHeight()
					+ childCloudHeigth;
			final int childShiftY = child.isContentVisible() ? child.getShift()
					: 0;
			final int childContentShift = child.getContent().getY()
					- childCloudHeigth / 2 - spaceAround;
			int childHGap;
			if (child.isContentVisible())
				childHGap = child.getHGap();
			else if (child.isSummary())
				childHGap = child.getZoomed(LocationModel.HGAP);
			else
				childHGap = 0;

			boolean isFreeNode = child.isFree();

			if (isItem) {
				if (isFreeNode) {
					this.ly[i] = childShiftY - childContentShift
							- childCloudHeigth / 2 - spaceAround;
				} else {
					if (childShiftY < 0 || visibleChildCounter == 0) {
						top += childShiftY;
					}
					top -= childContentShift;

					top += child.getTopOverlap();
					y -= child.getTopOverlap();
					if (childShiftY < 0) {
						this.ly[i] = y;
						y -= childShiftY;
					} else {
						if (visibleChildCounter > 0)
							y += childShiftY;
						this.ly[i] = y;
					}
					if (childHeight != 0) {
						y += childHeight + vGap;
						y -= child.getBottomOverlap();
					}

					childContentHeightSum += childContentHeight;
					if (oldLevel > 0) {
						summaryBaseX[0] = 0;
						for (int j = 0; j < oldLevel; j++) {
							groupStart[j] = i;
							groupStartY[j] = Integer.MAX_VALUE;
							groupEndY[j] = Integer.MIN_VALUE;
							groupStartContentHeightSum[j] = childContentHeightSum;
						}
					} else if (child.isFirstGroupNode()) {
						groupStartContentHeightSum[0] = childContentHeightSum;
						summaryBaseX[0] = 0;
						groupStart[0] = i;
					}
					if (childHeight != 0) {
						if (visibleChildCounter > 0)
							childContentHeightSum += vGap;
					}
				}
				if (childHeight != 0)
					visibleChildCounter++;
			} else {
				final int itemLevel = level - 1;
				if (child.isFirstGroupNode()) {
					groupStartContentHeightSum[level] = groupStartContentHeightSum[itemLevel];
					summaryBaseX[level] = 0;
					groupStart[level] = groupStart[itemLevel];
				}
				int summaryY = (groupStartY[itemLevel] + groupEndY[itemLevel])
						/ 2
						- childContentHeight
						/ 2
						+ childShiftY
						- (child.getContent().getY() - childCloudHeigth / 2 - spaceAround);
				this.ly[i] = summaryY;
				if (!isFreeNode) {
					final int deltaY = summaryY - groupStartY[itemLevel]
							+ child.getTopOverlap();
					if (deltaY < 0) {
						top += deltaY;
						y -= deltaY;
						summaryY -= deltaY;
						for (int j = groupStart[itemLevel]; j <= i; j++) {
							NodeView groupItem = (NodeView) view
									.getComponent(j);
							if (groupItem.isLeft() == isLeft
									&& (this.levels[j] > 0 || !this.free[j]))
								this.ly[j] -= deltaY;
						}
					}
					if (childHeight != 0) {
						summaryY += childHeight + vGap
								- child.getBottomOverlap();
					}
					y = Math.max(y, summaryY);
					final int summaryContentHeight = groupStartContentHeightSum[itemLevel]
							+ childContentHeight;
					if (childContentHeightSum < summaryContentHeight) {
						childContentHeightSum = summaryContentHeight;
					}
				}
			}
			if (!isItem || !isFreeNode) {
				if (child.isFirstGroupNode()) {
					groupStartY[level] = this.ly[i] + child.getTopOverlap();
					groupEndY[level] = this.ly[i] + childHeight
							- child.getBottomOverlap();
				} else {
					groupStartY[level] = Math.min(groupStartY[level],
							this.ly[i] + child.getTopOverlap());
					groupEndY[level] = Math.max(this.ly[i] + childHeight
							- child.getBottomOverlap(), groupEndY[level]);
				}
			}
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
				x = baseX - childHGap - child.getContent().getX()
						- child.getContent().getWidth();
				summaryBaseX[level] = Math.min(summaryBaseX[level], x
						+ spaceAround);
			} else {
				x = baseX + childHGap - child.getContent().getX();
				summaryBaseX[level] = Math.max(summaryBaseX[level],
						x + child.getWidth() - spaceAround);
			}
			left = Math.min(left, x);
			this.lx[i] = x;
		}

		top += (contentSize.height - childContentHeightSum) / 2;
		finalizeDataSet(view, isLeft, left, childContentHeightSum, top);
	}

	private void finalizeDataSet(NodeView view, boolean isLeft, int left,
			int childContentHeight, int top) {
		if (!isLeft && this.leftDataSet || isLeft && this.rightDataSet) {
			this.left = Math.min(this.left, left);
			this.childContentHeight = Math.max(this.childContentHeight,
					childContentHeight);
			int deltaTop = top - this.top;
			final boolean changeLeft;
			if (deltaTop < 0) {
				this.top = top;
				changeLeft = !isLeft;
				deltaTop = -deltaTop;
			} else {
				changeLeft = isLeft;
			}
			for (int i = 0; i < childViewCount; i++) {
				NodeView child = (NodeView) view.getComponent(i);
				if (child.isLeft() == changeLeft
						&& (this.levels[i] > 0 || !this.free[i])) {
					this.ly[i] += deltaTop;
				}
			}
		} else {
			this.left = left;
			this.childContentHeight = childContentHeight;
			this.top = top;
		}
		if (isLeft)
			this.leftDataSet = true;
		else
			this.rightDataSet = true;
	}

	public void placeChildren(final NodeView view) {
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
			if (this.levels[i] == 0 && this.free[i]) {
				minY = Math.min(minY, contentY + this.ly[i]);
			} else
				minY = Math.min(minY, baseY + this.ly[i]);
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
			if (this.levels[i] == 0 && this.free[i]) {
				y = contentY + this.ly[i];
			} else {
				y = baseY + this.ly[i];
				if (!this.free[i])
					heigthWithoutOverlap = Math.max(
							heigthWithoutOverlap,
							y + child.getHeight() + cloudHeight / 2
									- child.getBottomOverlap());
			}
			final int x = contentX + this.lx[i];
			child.setLocation(x, y);
			width = Math.max(width, child.getX() + child.getWidth());
			height = Math.max(height, y + child.getHeight() + cloudHeight / 2);
		}

		view.setSize(width, height);
		view.setTopOverlap(topOverlap);
		view.setBottomOverlap(height - heigthWithoutOverlap);
	}
}