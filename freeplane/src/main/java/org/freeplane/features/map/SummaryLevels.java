package org.freeplane.features.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SummaryLevels{
	private static final boolean[] BOTH_SIDES = {true, false};
	private static final boolean[] LEFT_SIDE = {true};
	private static final boolean[] RIGHT_SIDE = {false};
	public  final int[] summaryLevels;
	public  final int highestSummaryLevel;
	public  final boolean[] sides;
	private final NodeModel parentNode;
	public SummaryLevels(NodeModel parentNode) {
		this.parentNode = parentNode;
		int highestSummaryLevel = 0;
		int childCount = parentNode.getChildCount();
		this.summaryLevels = new int[childCount];
		this.sides = sidesOf(parentNode);
		for(boolean isLeft : sides){

			int level = 1;
			boolean useSummaryAsItem = true;
			for (int i = 0; i < childCount; i++) {
				final NodeModel child = parentNode.getChildAt(i);
				if (child.isLeft() == isLeft) {
					final boolean isItem = !SummaryNode.isSummaryNode(child) || useSummaryAsItem;
					if (isItem) {
						if (level > 0)
							useSummaryAsItem = true;
						level = 0;
						if (child.hasVisibleContent()) {
							useSummaryAsItem = false;
						}
					} else {
						level++;
						highestSummaryLevel = Math.max(highestSummaryLevel, level);
					}
					summaryLevels[i] = level;
				}
			}
		}
		this.highestSummaryLevel = highestSummaryLevel;
	}
	static private boolean[] sidesOf(NodeModel parentNode) {
		return parentNode.isRoot() ? BOTH_SIDES : parentNode.isLeft() ? LEFT_SIDE : RIGHT_SIDE;
	}
	
	public Collection<NodeModel> summarizedNodes(NodeModel summaryNode){
		if(summaryNode.getParentNode() != parentNode)
			return Collections.emptyList();
		final int summaryNodeIndex = parentNode.getIndex(summaryNode);
		final int summaryLevel = summaryLevels[summaryNodeIndex];
		if(summaryLevel == 0)
			return Collections.emptyList();
		else {
			final ArrayList<NodeModel> arrayList = new ArrayList<>();
			for(int i = summaryNodeIndex - 1; i >= 0; i--){
				if(summaryLevels[i] >= summaryLevel) {
					if(sides != BOTH_SIDES || parentNode.getChildAt(i).isLeft() == summaryNode.isLeft())
						return arrayList;
				} else if (summaryLevels[i] == summaryLevel - 1) {
					final NodeModel child = parentNode.getChildAt(i);
					if (sides != BOTH_SIDES || child.isLeft() == summaryNode.isLeft()) {
						arrayList.add(child);
					}
					if(SummaryNode.isFirstGroupNode(child))
						return arrayList;
				}
			}
			return arrayList;
		}
	}
	public NodeModel findSummaryNode(int index) {
		final int nodeLevel = summaryLevels[index];
		final boolean leftSide = parentNode.getChildAt(index).isLeft();
		for (int i = index + 1; i < parentNode.getChildCount(); i++){
			final int level = summaryLevels[i];
			if(level == nodeLevel && SummaryNode.isFirstGroupNode(parentNode.getChildAt(i)))
				return null;
			if(level > nodeLevel) {
				final NodeModel summaryNode = parentNode.getChildAt(i);
				if(summaryNode.isLeft() == leftSide)
					return summaryNode;
			}
		}
		return null;
	}
	
	public NodeModel findGroupBeginNode(int index) {
		int nodeLevel = summaryLevels[index];
		final boolean leftSide = parentNode.getChildAt(index).isLeft();
		for (int i = index - 1; i >= 0; i--){
			final int level = summaryLevels[i];
			if(level > nodeLevel)
				return null;
			if(level == nodeLevel) {
				final NodeModel groupBeginNode = parentNode.getChildAt(i);
				if(groupBeginNode.isLeft() == leftSide && SummaryNode.isFirstGroupNode(groupBeginNode))
					return groupBeginNode;
			}
		}
		return null;
	}
}