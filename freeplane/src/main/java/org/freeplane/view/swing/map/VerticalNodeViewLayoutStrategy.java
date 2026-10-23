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
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

import org.freeplane.api.ChildNodesAlignment;
import org.freeplane.api.ChildNodesAlignment.Placement;
import org.freeplane.api.ChildrenSides;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryLevels;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.treetable.TreeTableHeader;
import org.freeplane.features.treetable.TreeTableHeaderPlacement;
import org.freeplane.features.treetable.TreeTableModel;

class VerticalNodeViewLayoutStrategy {

    static private boolean wrongChildComponentsReported = false;

    private int childViewCount;
    private final int spaceAround;
    private final NodeViewLayoutHelper view;

    private final int[] xCoordinates;
    private final int[] yCoordinates;
    private final int[] yBottomCoordinates;
    private final boolean[] isChildFreeNode;
    private StepFunction bottomBoundary;
    private int yBottom;
    private int bottomContentY;
    private boolean lastChildHasSubtree;
    private StepFunction leftBottomBoundary;
    private StepFunction rightBottomBoundary;
    private SummaryLevels viewLevels;
    private int totalContentShift;
    private int totalSideContentShift;
    private boolean rightSideCoordinatesAreSet;
    private boolean leftSideCoordinaresAreSet;

    final private boolean allowsCompactLayout;
    final private boolean isAutoCompactLayoutEnabled;

    private final int defaultVGap;
    private static final int SUMMARY_DEFAULT_HGAP_PX = LocationModel.DEFAULT_HGAP_PX * 7 / 12;
    private final Dimension contentSize;

    private int minimalGapBetweenChildren;
    private ChildNodesAlignment childNodesAlignment;
    private int level;
    private int y;
    private int vGap;
    private int visibleLaidOutChildCounter;
    private int[] groupStartIndex;
    private StepFunction[] groupStartBoundaries;
    private int[] groupUpperYCoordinate;
    private int[] groupLowerYCoordinate;
    private boolean currentSideLeft;

    private final int baseRelativeDistanceToChildren;
    private boolean areChildrenSeparatedByY;
    private int[] summaryBaseX;

    private final int extraGapForChildren;
	private final int foldingMarkReservedSpace;

	private int lastMinimumDistanceConsideringHandles;

	private int childCloudHeight;




    public VerticalNodeViewLayoutStrategy(NodeView view) {
        NodeViewLayoutHelper layoutHelper = view.getLayoutHelper();
        this.view = layoutHelper;
        this.contentSize = ContentSizeCalculator.INSTANCE.calculateContentSize(layoutHelper);
        childViewCount = view.getComponentCount() - 1;
        layoutChildViews(view);
        this.totalContentShift = 0;
        rightSideCoordinatesAreSet = false;
        leftSideCoordinaresAreSet = false;
        this.xCoordinates = new int[childViewCount];
        this.yCoordinates = new int[childViewCount];
        this.yBottomCoordinates = new int[childViewCount];
        this.isChildFreeNode = new boolean[childViewCount];
        this.spaceAround = view.getSpaceAround();
        this.allowsCompactLayout = view.allowsCompactLayout();
        this.childNodesAlignment = view.getChildNodesAlignment();
        this.isAutoCompactLayoutEnabled = view.isAutoCompactLayoutEnabled() && ! childNodesAlignment.isStacked();
        this.defaultVGap = view.getMap().getZoomed(LocationModel.DEFAULT_VGAP.toBaseUnits());
        this.minimalGapBetweenChildren = view.getMinimalDistanceBetweenChildren();
        this.extraGapForChildren = calculateExtraGapForChildren(minimalGapBetweenChildren);
        this.baseRelativeDistanceToChildren = view.getBaseDistanceToChildren(- LocationModel.DEFAULT_HGAP_PX);
        this.foldingMarkReservedSpace = Math.max(1, view.getBaseDistanceToChildren(0) - 3);
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

    private void setFreeChildNodes(final boolean laysOutLeftSide) {
        for (int i = 0; i < childViewCount; i++) {
            final NodeViewLayoutHelper child = view.getComponent(i);
            if (child.isLeft() == laysOutLeftSide)
                this.isChildFreeNode[i] = child.isFree();
        }
    }
    public void calculateLayoutData() {
        final NodeModel node = view.getNode();
        MapView map = view.getMap();
        Filter filter = map.getFilter();
        NodeModel selectionRoot = map.getRoot().getNode();
        viewLevels = childViewCount == 0 ? SummaryLevels.ignoringChildNodes(selectionRoot, node, filter) : SummaryLevels.of(selectionRoot, node, filter);
        for(boolean isLeft : viewLevels.sides)
            calculateLayoutData(isLeft);
        applyLayoutToChildComponents();
    }

    private void calculateLayoutData(final boolean isLeft) {
        setFreeChildNodes(isLeft);
        calculateLayoutX(isLeft);
        placeTreeTableHeader(isLeft);
        calculateLayoutY(isLeft);

    }

    private void placeTreeTableHeader(final boolean laysOutLeftSide) {
        if (laysOutLeftSide || !TreeTableModel.isTreeTableRoot(view.getNode()))
            return;
        int headerIndex = -1;
        final IntStream.Builder rowXs = IntStream.builder();
        final IntStream.Builder rowWidths = IntStream.builder();
        for (int i = 0; i < childViewCount; i++) {
            final NodeViewLayoutHelper child = view.getComponent(i);
            if (child.isLeft())
                continue;
            if (TreeTableHeader.isHeaderRoot(child.getNode()))
                headerIndex = i;
            else {
                rowXs.add(xCoordinates[i]);
                rowWidths.add(child.getWidth());
            }
        }
        if (headerIndex < 0)
            return;
        final int treeColumnRight = TreeTableHeaderPlacement.treeColumnRight(contentSize.width,
            rowXs.build().toArray(), rowWidths.build().toArray(), spaceAround);
        xCoordinates[headerIndex] = TreeTableHeaderPlacement.headerX(treeColumnRight,
            view.getZoomed(TreeTableHeaderPlacement.GAP), spaceAround);
    }

    private void calculateLayoutX(final boolean laysOutLeftSide) {
        currentSideLeft = laysOutLeftSide;
        areChildrenSeparatedByY = childNodesAlignment.isStacked();
        level = viewLevels.highestSummaryLevel + 1;
        summaryBaseX = new int[level];
        for (int i = 0; i < childViewCount; i++) {
            NodeViewLayoutHelper child = view.getComponent(i);
            if (child.isLeft() == currentSideLeft) {
                int oldLevel = level;
                level = viewLevels.summaryLevels[i];
                boolean isFreeNode = child.isFree();
                boolean isItem = level == 0;
                int childHGap = calculateChildHorizontalGap(child, isItem, isFreeNode, baseRelativeDistanceToChildren);
                if (isItem) {
                    assignRegularChildHorizontalPosition(i, child, oldLevel, childHGap);
                } else {
                    assignSummaryChildHorizontalPosition(i, child, childHGap);
                }
            }
        }
    }

    private int calculateChildHorizontalGap(NodeViewLayoutHelper child,
            boolean isItem,
            boolean isFreeNode,
            int baseDistanceToChildren) {
        int hGap;
        if (child.isContentVisible()) {
            hGap = calculateDistance(child, NodeViewLayoutHelper::getHGap);
        } else if (child.isSummary()) {
            hGap = child.getZoomed(SUMMARY_DEFAULT_HGAP_PX);
        } else {
            hGap = 0;
        }
        if (view.getNode().isHiddenSummary() && !child.getNode().isHiddenSummary()) {
            hGap -= child.getZoomed(SUMMARY_DEFAULT_HGAP_PX);
        }
        if (isItem && !isFreeNode && child.isSubtreeVisible()) {
            hGap += baseDistanceToChildren;
        }
        return hGap;
    }

    private void assignRegularChildHorizontalPosition(int index,
                            NodeViewLayoutHelper child,
                            int oldLevel,
                            int childHGap) {
        if (!child.isFree() && (oldLevel > 0 || child.isFirstGroupNode())) {
            summaryBaseX[0] = 0;
        }
        placeChildXCoordinate(index, child, childHGap);
    }

    private void assignSummaryChildHorizontalPosition(int index,
                            NodeViewLayoutHelper child,
                            int childHGap) {
        if (child.isFirstGroupNode()) {
            summaryBaseX[level] = 0;
        }
        placeChildXCoordinate(index, child, childHGap);
    }

    private void placeChildXCoordinate(int index,
                                    NodeViewLayoutHelper child,
                                    int childHGap) {
        int baseX;
        if (level > 0) {
            baseX = summaryBaseX[level - 1];
        } else {
            if (level == 0 && areChildrenSeparatedByY && view.childrenSides() == ChildrenSides.BOTH_SIDES) {
                baseX = contentSize.width / 2;
            } else if (child.isLeft() != (level == 0 && (child.isFree() || areChildrenSeparatedByY))) {
                baseX = 0;
            } else {
                baseX = contentSize.width;
            }
        }
        int x;
        if (child.isLeft()) {
            x = baseX - childHGap - child.getContentX() - child.getContentWidth();
            summaryBaseX[level] = Math.min(summaryBaseX[level], x + spaceAround);
        } else {
            x = baseX + childHGap - child.getContentX();
            summaryBaseX[level] = Math.max(summaryBaseX[level], x + child.getWidth() - spaceAround);
        }
        this.xCoordinates[index] = x;
    }

	private void calculateLayoutY(final boolean laysOutLeftSide) {
        currentSideLeft = laysOutLeftSide;
        totalSideContentShift = 0;
        level = viewLevels.highestSummaryLevel + 1;
        y = 0;
        yBottom = 0;
        bottomContentY = 0;
        lastChildHasSubtree = false;
        bottomBoundary = null;
        vGap = 0;
        lastMinimumDistanceConsideringHandles = 0;
        childCloudHeight = 0;
        visibleLaidOutChildCounter = 0;
        groupStartIndex = new int[level];
        groupStartBoundaries = new StepFunction[level];
        groupUpperYCoordinate = new int[level];
        groupLowerYCoordinate = new int[level];

        for (int index = 0; index < childViewCount; index++) {
            NodeViewLayoutHelper child = view.getComponent(index);
            if (child.isLeft() == currentSideLeft) {
                int oldLevel = level;
                int childRegularHeight = child.getHeight() - child.getTopOverlap() - child.getBottomOverlap() - 2 * spaceAround;
                level = viewLevels.summaryLevels[index];
                if(index >= viewLevels.summaryLevels.length){
                    final String errorMessage = "Bad node view child components: missing node for component " + index;
                    UITools.errorMessage(errorMessage);
                    System.err.println(errorMessage);
                    for (int i = 0; i < view.getComponentCount(); i++){
                        final String component = view.describeComponent(i);
                        System.err.println(component);
                    }
                }
                boolean isFreeNode = child.isFree();
                boolean isItem = level == 0;
                int childShiftY = calculateDistance(child, NodeViewLayoutHelper::getShift);

                if (level == 0) {
                	if (isFreeNode) {
                		assignFreeChildVerticalPosition(index, childShiftY, child);
                	} else {
                		if (childRegularHeight != 0) {
                			layoutRegularChild(index, child, childRegularHeight, childShiftY);
                			visibleLaidOutChildCounter++;
                		}
                		else {
                		   	yCoordinates[index] = calculateChildY(childShiftY);
                		}
                		initializeSummaryGroupStart(index, oldLevel, child.isFirstGroupNode());
                	}
                } else {
                	assignSummaryChildVerticalPosition(index, child, childRegularHeight, childShiftY);
                }
                if (!(isItem && isFreeNode)) {
                    updateSummaryGroupBounds(index, child, level, isItem, childRegularHeight);
                }
            }
        }
        if(childNodesAlignment.placement() != Placement.TOP) {
			totalSideContentShift -= contentSize.height;
		}
        if(childNodesAlignment.placement() == Placement.CENTER)
            totalSideContentShift /= 2;
        else if (childNodesAlignment == ChildNodesAlignment.BEFORE_PARENT
                && contentSize.height > 0
                && !isFirstVisibleLaidOutChild()) {
            totalSideContentShift += calculateAddedDistanceFromParentToChildren(minimalGapBetweenChildren, contentSize) + childCloudHeight/2;
        }
        calculateRelativeCoordinatesForContentAndBothSides(laysOutLeftSide);
    }

    private void initializeSummaryGroupStart(int childIndex,
            int oldLevel,
            boolean isFirstGroupNode) {
        if (oldLevel > 0) {
            for (int j = 0; j < oldLevel; j++) {
                groupStartBoundaries[j] = bottomBoundary;
                groupStartIndex[j] = childIndex;
                groupUpperYCoordinate[j] = Integer.MAX_VALUE;
                groupLowerYCoordinate[j] = Integer.MIN_VALUE;
            }
        } else if (isFirstGroupNode) {
            groupStartIndex[0] = childIndex;
            groupStartBoundaries[0] = bottomBoundary;
        }
    }

    private void calculateRelativeCoordinatesForContentAndBothSides(boolean isLeft) {
        if (! (leftSideCoordinaresAreSet || rightSideCoordinatesAreSet)) {
            totalContentShift = totalSideContentShift;
        } else {
            int delta =  this.totalContentShift - totalSideContentShift;
            if(delta != 0) {
                final boolean changeLeft;
                if (delta < 0) {
                    totalContentShift = totalSideContentShift;
                    changeLeft = !isLeft;
                    delta = -delta;
                } else {
                    changeLeft = isLeft;
                }
                for (int i = 0; i < childViewCount; i++) {
                    NodeViewLayoutHelper child = view.getComponent(i);
                    if (child.isLeft() == changeLeft
                            && (viewLevels.summaryLevels[i] > 0 || !isChildFreeNode[i])) {
                        yCoordinates[i] += delta;
                    }
                }
                if(bottomBoundary != null) {
                    bottomBoundary = bottomBoundary.translate(0, delta);
                }
            }
        }
        if (isLeft) {
            leftSideCoordinaresAreSet = true;
            leftBottomBoundary = bottomBoundary;
        } else {
            rightSideCoordinatesAreSet = true;
            rightBottomBoundary = bottomBoundary;
        }
    }


    private int calculateAddedDistanceFromParentToChildren(final int minimalDistance,
            final Dimension contentSize) {
        boolean usesHorizontalLayout = view.usesHorizontalLayout();
        int distance = Math.max(view.getMap().getZoomed(usesHorizontalLayout ? LocationModel.DEFAULT_VGAP_PX * 2 : LocationModel.DEFAULT_VGAP_PX), minimalDistance);
        return contentSize.height + distance;
    }

    private int calculateExtraGapForChildren(final int minimalDistanceBetweenChildren) {
        if(3 * defaultVGap > minimalDistanceBetweenChildren)
            return minimalDistanceBetweenChildren + 2 * defaultVGap;
        else
            return (minimalDistanceBetweenChildren + 11 * 2 * defaultVGap) / 6;
    }

    private int calculateDistance(final NodeViewLayoutHelper child, ToIntFunction<NodeViewLayoutHelper> nodeDistance) {
        if (!child.isContentVisible())
            return 0;
        int shift = nodeDistance.applyAsInt(child);
        for(NodeViewLayoutHelper ancestor = child.getParentView();
                ancestor != null && ! ancestor.isContentVisible();
                ancestor = ancestor.getParentView()) {
            if(ancestor.isFree())
                shift += nodeDistance.applyAsInt(ancestor);
        }
        return shift;
    }

    private boolean isNextNodeSummaryNode(int childViewIndex) {
        return childViewIndex + 1 < viewLevels.summaryLevels.length && viewLevels.summaryLevels[childViewIndex + 1] > 0;
    }

    private void assignFreeChildVerticalPosition(int childIndex, int shiftY, NodeViewLayoutHelper child) {
        this.yCoordinates[childIndex] = shiftY - child.getContentY();
    }


    private void layoutRegularChild(int index,
    		NodeViewLayoutHelper child,
    		int childRegularHeight,
    		int childShiftY) {
    	int y0 = y;
    	final int contentTop = getContentTop(child);
		final int topContentY = contentTop + y0;
    	final int spaceBetweenContent = topContentY - bottomContentY;
    	int lastChildCloudHeight = this.childCloudHeight;
    	this.childCloudHeight = CloudHeightCalculator.INSTANCE.getAdditionalCloudHeight(child);
    	if (isFirstVisibleLaidOutChild()
		        && childNodesAlignment == ChildNodesAlignment.AFTER_PARENT
		        && contentSize.height > 0) {
		    y += calculateAddedDistanceFromParentToChildren(minimalGapBetweenChildren, contentSize) + childCloudHeight/2;
		}
    	int availableSpace = 0;
    	int currentChildExtraGap = 0;
    	int upperGap = 0;
    	boolean childHasSubtree = childRegularHeight - child.getContentHeight() > childCloudHeight;
		if(! isFirstVisibleLaidOutChild()) {
			final int spaceForClouds = (childCloudHeight + lastChildCloudHeight)/2 + (lastChildCloudHeight & 1);
			final int spaceBetweenContentAndClouds = spaceBetweenContent - spaceForClouds;
			if (isAutoCompactLayoutEnabled) {
				availableSpace = calculateAvailableSpaceForCompactLayout(child, index, y0, spaceForClouds);
				y -= availableSpace;
			}
			int lastChildExtraGap = lastChildHasSubtree ? extraGapForChildren / 2 : 0;
			if(lastMinimumDistanceConsideringHandles > vGap + lastChildExtraGap) {
				lastChildExtraGap = lastMinimumDistanceConsideringHandles - vGap;
				currentChildExtraGap = childHasSubtree ? Math.max(extraGapForChildren - lastChildExtraGap, 0) : 0;
			} else {
				currentChildExtraGap = childHasSubtree ? extraGapForChildren - extraGapForChildren / 2 : 0;
				if (isAutoCompactLayoutEnabled && spaceBetweenContentAndClouds > availableSpace) {
					lastChildExtraGap = Math.min(spaceBetweenContentAndClouds - availableSpace, lastChildExtraGap);
					currentChildExtraGap = Math.min(spaceBetweenContentAndClouds - availableSpace - lastChildExtraGap, currentChildExtraGap);
					currentChildExtraGap = Math.max(0, currentChildExtraGap);
				}
			}
			upperGap = lastChildExtraGap + currentChildExtraGap;
			y += vGap + upperGap;
    	}

    	int yBegin = calculateChildY(childShiftY);
    	yCoordinates[index] = yBegin;

    	final Placement placement = childNodesAlignment.placement();
		if (!allowsCompactLayout && childShiftY < 0) {
		    totalSideContentShift -= placement == Placement.CENTER ? 2 * childShiftY : childShiftY;
		}

		if (childNodesAlignment == ChildNodesAlignment.FLOW ||
		    childNodesAlignment == ChildNodesAlignment.AUTO) {
			totalSideContentShift += child.getContentHeight() + vGap + 2 * Math.max(0, contentTop + currentChildExtraGap - availableSpace);
		}
		else if (placement != Placement.TOP) {
		    totalSideContentShift += child.getContentHeight() + vGap + upperGap + contentTop - availableSpace + y0 - bottomContentY;
		    if(placement == Placement.CENTER && isFirstVisibleLaidOutChild())
		    	totalSideContentShift += contentTop;
		}
        lastChildHasSubtree = childHasSubtree;
    	updateGapsAndBoundaries(index, child, childRegularHeight);
    }

    private int calculateAvailableSpaceForCompactLayout(NodeViewLayoutHelper child, int index, int y, int spaceForClouds) {
        if (bottomBoundary != null) {
            StepFunction childTopBoundary;
            if(view.usesHorizontalLayout() != child.usesHorizontalLayout() || child.getChildNodesAlignment().isStacked())
                childTopBoundary = StepFunction.segment(spaceAround, child.getWidth() - spaceAround, spaceAround + child.getTopOverlap());
            else
                childTopBoundary = child.getTopBoundary();
            if (childTopBoundary != null) {
                childTopBoundary = childTopBoundary.translate(xCoordinates[index], y - child.getTopOverlap());
                int topContentY = getContentTop(child) + y;
                final int distance = childTopBoundary.distance(bottomBoundary);
                final int contentDistance = topContentY - bottomContentY - spaceForClouds;
                if (distance >= contentDistance)
					return contentDistance;
				else
					return distance;
            }
        }
        return 0;
    }

    private int calculateChildY(int childShiftY) {
        int yBegin;
        if (childShiftY < 0 && ! allowsCompactLayout) {
            yBegin = y;
            y -= childShiftY;
        } else {
        	y += childShiftY;
        	yBegin = y;
        }
        return yBegin;
    }

    private int getContentTop(NodeViewLayoutHelper child) {
		return child.getContentY() - child.getTopOverlap() - spaceAround;
	}

    private void updateGapsAndBoundaries(int index, NodeViewLayoutHelper child, int childRegularHeight) {
        vGap = calculateNextVGap(index);
        bottomContentY = getContentTop(child) + y + child.getContentHeight();
        yBottom = Math.max(yBottom, y + childRegularHeight);
        yBottomCoordinates[index] = y;
        updateBottomBoundary(child, index, y, CombineOperation.FALLBACK);
        y += childRegularHeight;
        if(view.usesHorizontalLayout())
        	lastMinimumDistanceConsideringHandles = child.getMinimumDistanceConsideringHandles();
    }

    private int calculateNextVGap(int index) {
        int summaryNodeIndex = viewLevels.findSummaryNodeIndex(index);
        if (summaryNodeIndex == SummaryLevels.NODE_NOT_FOUND
                || summaryNodeIndex - 1 == index) {
            return minimalGapBetweenChildren;
        } else {
            if(defaultVGap >= minimalGapBetweenChildren)
                return minimalGapBetweenChildren;
            else
                return defaultVGap + (minimalGapBetweenChildren - defaultVGap) / 6;
        }
    }


    private void assignSummaryChildVerticalPosition(int index,
                                   NodeViewLayoutHelper child,
                                   int childRegularHeight,
                                   int childShiftY) {
        int itemLevel = level - 1;
        if (child.isFirstGroupNode()) {
            initializeGroupStartIndex(itemLevel);
        }

        initializeGroupCoordinates(itemLevel);

        int summaryY = calculateSummaryChildVerticalPosition(
                groupUpperYCoordinate[itemLevel],
                groupLowerYCoordinate[itemLevel],
                child,
                childShiftY);

        if (!child.isFree()) {
            yCoordinates[index] = summaryY;

            int deltaY = summaryY - groupUpperYCoordinate[itemLevel];

            if (isAutoCompactLayoutEnabled && groupStartBoundaries[itemLevel] != null) {
                deltaY += adjustForAutoCompactLayout(index, child, itemLevel);
            }

            if (deltaY < 0) {
                handleNegativeDeltaY(index, itemLevel, deltaY);
                summaryY -= deltaY;
            }

            if (childRegularHeight != 0) {
                updateBottomBoundary(child, index, summaryY + minimalGapBetweenChildren, CombineOperation.MAX);
                summaryY += childRegularHeight + minimalGapBetweenChildren;
            }
            y = Math.max(y, summaryY);
            yBottom = Math.max(yBottom, summaryY);
            bottomContentY = Math.max(bottomContentY, summaryY);
        }
    }

    private void initializeGroupStartIndex(int itemLevel) {
        groupStartIndex[level] = groupStartIndex[itemLevel];
    }

    private void initializeGroupCoordinates(int itemLevel) {
        if (groupUpperYCoordinate[itemLevel] == Integer.MAX_VALUE) {
            groupUpperYCoordinate[itemLevel] = y;
            groupLowerYCoordinate[itemLevel] = y;
        }
    }

    private int adjustForAutoCompactLayout(int index, NodeViewLayoutHelper child, int itemLevel) {
        StepFunction childTopBoundary = child.getTopBoundary();
        if (childTopBoundary != null) {
            childTopBoundary = childTopBoundary.translate(xCoordinates[index], y);
            int distance = childTopBoundary.distance(groupStartBoundaries[itemLevel]) - minimalGapBetweenChildren;
            if (distance < 0) {
                return distance;
            }
        }
        return 0;
    }

    private void handleNegativeDeltaY(int index, int itemLevel, int deltaY) {
        final Placement placement = childNodesAlignment.placement();
        totalSideContentShift -= placement == Placement.CENTER ? 2 * deltaY : deltaY;

        y -= deltaY;
        bottomBoundary = groupStartBoundaries[itemLevel];

        // Adjust coordinates for all group items
        for (int j = groupStartIndex[itemLevel]; j <= index; j++) {
            final NodeViewLayoutHelper childItem = view.getComponent(j);
            NodeViewLayoutHelper groupItem = childItem;
            if (groupItem.isLeft() == currentSideLeft
                    && (viewLevels.summaryLevels[j] > 0
                    || !isChildFreeNode[j])) {
                yCoordinates[j] -= deltaY;
                if (j != index) {
                    final NodeViewLayoutHelper child = view.getComponent(j);
                    int childRegularHeight = child.getHeight() - child.getTopOverlap() - child.getBottomOverlap() - 2 * spaceAround;
                    if(childRegularHeight != 0) {
                        yBottomCoordinates[j] -= deltaY;
                        updateBottomBoundary(child, j, yBottomCoordinates[j], CombineOperation.FALLBACK);
                    }
                }
            }
        }
    }

    private void updateBottomBoundary(NodeViewLayoutHelper child, int index, int y, CombineOperation combineOperation) {
        if(isAutoCompactLayoutEnabled) {
            StepFunction childBottomBoundary;
            if(view.usesHorizontalLayout() != child.usesHorizontalLayout() || child.getChildNodesAlignment().isStacked())
                childBottomBoundary = StepFunction.segment(spaceAround, child.getWidth() - spaceAround,
                        child.getHeight() - spaceAround + child.getTopOverlap());
            else
                childBottomBoundary = child.getBottomBoundary();
            childBottomBoundary = childBottomBoundary == null ? null : childBottomBoundary.translate(xCoordinates[index], y - child.getTopOverlap());
            bottomBoundary = bottomBoundary == null ? childBottomBoundary
            		: childBottomBoundary == null ? bottomBoundary
            				: childBottomBoundary.combine(bottomBoundary, combineOperation);
        }
    }

    private int calculateSummaryChildVerticalPosition(int groupUpper, int groupLower,
            NodeViewLayoutHelper child, int childShiftY) {
        int childCloudHeight = CloudHeightCalculator.INSTANCE.getAdditionalCloudHeight(child);
        int childContentHeight = child.getContentHeight() + childCloudHeight;
        return (groupUpper + groupLower) / 2
                - childContentHeight / 2 + childShiftY
                - (child.getContentY() - childCloudHeight / 2 - spaceAround);
    }

    private void updateSummaryGroupBounds(int childIndex,
            NodeViewLayoutHelper child,
            int level,
            boolean isItem,
            int childRegularHeight) {
        int childUpper = yCoordinates[childIndex];
        int childBottom = yCoordinates[childIndex] + childRegularHeight;
        if (child.isFirstGroupNode()) {
            if (isItem) {
                groupUpperYCoordinate[level] = Integer.MAX_VALUE;
                groupLowerYCoordinate[level] = Integer.MIN_VALUE;
            } else {
                groupUpperYCoordinate[level] = childUpper;
                groupLowerYCoordinate[level] = childBottom;
            }
        }
        else if (childRegularHeight != 0 || isNextNodeSummaryNode(childIndex)) {
            groupUpperYCoordinate[level] = Math.min(groupUpperYCoordinate[level], childUpper);
            groupLowerYCoordinate[level] = Math.max(childBottom, groupLowerYCoordinate[level]);
        }
    }

    private void applyLayoutToChildComponents() {
        int spaceAround = view.getSpaceAround();
        int cloudHeight = CloudHeightCalculator.INSTANCE.getAdditionalCloudHeight(view);
        int leftMostX = IntStream.of(xCoordinates).min().orElse(0);
        int contentX = Math.max(spaceAround, -leftMostX);
        int contentY = spaceAround + cloudHeight/2 + Math.max(0, totalContentShift);
        view.setContentVisible(view.isContentVisible());
        int baseY = contentY - totalContentShift - spaceAround;
        final int minYFree = calculateMinYFree(contentY, cloudHeight);
        final int minYRegular = calculateMinYRegular(baseY);
        final int shift = Math.min(minYRegular, minYFree);
        if (shift < 0) {
            contentY -= shift;
            baseY -= shift;
        }
        int topOverlap = Math.max(0, minYRegular-minYFree);
        arrangeChildComponents(contentX, contentY, baseY, cloudHeight, spaceAround, topOverlap);
    }

    private int calculateMinYFree(int contentY, int cloudHeight) {
        int minYFree = 0;
        for (int i = 0; i < childViewCount; i++) {
            final int topOverlap = view.getComponent(i).getTopOverlap();
            if (viewLevels.summaryLevels[i] == 0 && isChildFreeNode[i]) {
                minYFree = Math.min(minYFree, contentY + yCoordinates[i] - cloudHeight/2 - topOverlap);
            }
        }
        return minYFree;
    }

    private int calculateMinYRegular(int baseY) {
        int minYRegular = 0;
        for (int i = 0; i < childViewCount; i++) {
            final int topOverlap = view.getComponent(i).getTopOverlap();
            if (viewLevels.summaryLevels[i] != 0 || !isChildFreeNode[i]) {
                minYRegular = Math.min(minYRegular, baseY + yCoordinates[i] - topOverlap);
            }
        }
        return minYRegular;
    }

    private void arrangeChildComponents(int contentX, int contentY,
                                int baseY, int cloudHeight,
                                int spaceAround, int topOverlap) {
        view.setContentBounds(contentX, contentY, contentSize.width, contentSize.height);
        final int cloudTop = cloudHeight/2;
        int width = contentX + contentSize.width + spaceAround;
        final int cloudBottom = cloudHeight - cloudTop;
		int height = contentY + contentSize.height + cloudBottom + spaceAround;
        int heightWithoutOverlap = height;

        for (int i = 0; i < childViewCount; i++) {
            NodeViewLayoutHelper child = view.getComponent(i);
            final int childTopOverlap = child.getTopOverlap();
            boolean free = isChildFreeNode[i];

            int x = contentX + xCoordinates[i];
            int y = (viewLevels.summaryLevels[i] == 0 && free)
            ? contentY + yCoordinates[i]
            : baseY + yCoordinates[i] - childTopOverlap;

            child.setLocation(x, y);

            if (!free) {
                heightWithoutOverlap = Math.max(
                    heightWithoutOverlap,
                    y + child.getHeight() + cloudBottom - child.getBottomOverlap());
            }

            width = Math.max(width, x + child.getWidth());
            height = Math.max(height, y + child.getHeight() + cloudBottom);
        }

        view.setSize(width, height);
        view.setTopOverlap(topOverlap);
        view.setBottomOverlap(height - heightWithoutOverlap);

        if(! view.isFree() && view.isAutoCompactLayoutEnabled() && width > 2 * spaceAround) {
			if (cloudHeight == 0 && ! childNodesAlignment.isStacked())
				calculateAndSetBoundaries(contentX, contentY, baseY, spaceAround, width, height);
			else {
				final StepFunction topBoundary = StepFunction.segment(spaceAround, width - spaceAround, spaceAround + topOverlap);
				view.setTopBoundary(topBoundary);
			    view.setBottomBoundary(topBoundary.translate(0, heightWithoutOverlap - 2 * spaceAround));
			}
		} else {
            view.setTopBoundary(null);
            view.setBottomBoundary(null);
        }

    }

    private void calculateAndSetBoundaries(int contentX, int contentY, int baseY,
                                         int spaceAround, int width, int height) {
        NodeViewLayoutHelper parentView = view.getParentView();
        if (parentView == null || width <= spaceAround || height <= spaceAround) {
            return;
        }
        final int segmentStart = view.isLeft() ? contentX - foldingMarkReservedSpace : contentX;
        final int segmentEnd = view.isRight() ? contentX + contentSize.width + foldingMarkReservedSpace : contentX + contentSize.width;

        StepFunction viewBottomBoundary = calculateBottomBoundary(contentX, contentY, baseY, segmentStart, segmentEnd);
        StepFunction viewTopBoundary = calculateTopBoundary(contentY, segmentStart, segmentEnd);

        view.setTopBoundary(viewTopBoundary);
        view.setBottomBoundary(viewBottomBoundary);
    }

    private StepFunction calculateBottomBoundary(int contentX, int contentY, int baseY,
                                               int segmentStart, int segmentEnd) {
        StepFunction viewBottomBoundary = contentSize.width <= 0 ? null :
            StepFunction.segment(segmentStart, segmentEnd, contentY + contentSize.height);

        if (leftBottomBoundary != null) {
            viewBottomBoundary = viewBottomBoundary == null ?
                leftBottomBoundary.translate(contentX, baseY) :
                viewBottomBoundary.combine(leftBottomBoundary.translate(contentX, baseY), CombineOperation.MAX);
        }

        if (rightBottomBoundary != null) {
            viewBottomBoundary = viewBottomBoundary == null ?
                rightBottomBoundary.translate(contentX, baseY) :
                viewBottomBoundary.combine(rightBottomBoundary.translate(contentX, baseY), CombineOperation.MAX);
        }

        return viewBottomBoundary;
    }

    private StepFunction calculateTopBoundary(int contentY, int segmentStart,
                                            int segmentEnd) {
        StepFunction viewTopBoundary = contentSize.width <= 0 ? null :
            StepFunction.segment(segmentStart, segmentEnd, contentY);

        for (int i = childViewCount - 1; i >= 0; i--) {
            NodeViewLayoutHelper child = view.getComponent(i);
            StepFunction childTopBoundary = child.getTopBoundary();
            if (childTopBoundary != null) {
                childTopBoundary = childTopBoundary.translate(child.getX(), child.getY());
                viewTopBoundary = viewTopBoundary == null ? childTopBoundary :
                    viewTopBoundary.combine(childTopBoundary, CombineOperation.MIN);
            }
        }

        return viewTopBoundary;
    }

    private boolean isFirstVisibleLaidOutChild() {
        return visibleLaidOutChildCounter == 0;
    }
}
