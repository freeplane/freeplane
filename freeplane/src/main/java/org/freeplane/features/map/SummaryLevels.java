package org.freeplane.features.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SummaryLevels{
	public static final int NODE_NOT_FOUND = -1;
	private static final boolean[] BOTH_SIDES = {true, false};
	private static final boolean[] LEFT_SIDE = {true};
	private static final boolean[] RIGHT_SIDE = {false};
	public  final int[] summaryLevels;
	public  final int highestSummaryLevel;
	public  final boolean[] sides;
	private final NodeModel parentNode;
	
	public SummaryLevels(NodeModel parentNode) {
		this(parentNode, false);
	}
	
	public static SummaryLevels of(NodeModel parentNode) {
		return new SummaryLevels(parentNode, false);
	}

	
	public static SummaryLevels ignoringChildNodes(NodeModel parentNode) {
		return new SummaryLevels(parentNode, true);
	}
	
	private SummaryLevels(NodeModel parentNode, boolean ignoreChildNodes) {
		this.parentNode = parentNode;
		int highestSummaryLevel = 0;
		int childCount = ignoreChildNodes ? 0 : parentNode.getChildCount();
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
			final ArrayList<NodeModel> arrayList = new ArrayList<NodeModel>();
			for(int i = summaryNodeIndex - 1; i >= 0; i--){
				final int level = summaryLevels[i];
				if(level >= summaryLevel) {
					if(sides != BOTH_SIDES || parentNode.getChildAt(i).isLeft() == summaryNode.isLeft())
						return arrayList;
				} else if (level == summaryLevel - 1) {
					final NodeModel child = parentNode.getChildAt(i);
					if (sides != BOTH_SIDES || child.isLeft() == summaryNode.isLeft()) {
						if(SummaryNode.isFirstGroupNode(child)) {
							if(level > 0)
								arrayList.add(child);
							return arrayList;
						}
						arrayList.add(child);
					}
				}
			}
			return arrayList;
		}
	}
	public NodeModel findSummaryNode(int index) {
		final int summaryNodeIndex = findSummaryNodeIndex(index);
		return parentNode.getChildAt(summaryNodeIndex);
	}

	public int findSummaryNodeIndex(int index) {
		final int nodeLevel = summaryLevels[index];
		final boolean leftSide = parentNode.getChildAt(index).isLeft();
		for (int i = index + 1; i < parentNode.getChildCount(); i++){
			final int level = summaryLevels[i];
			if(level == nodeLevel && SummaryNode.isFirstGroupNode(parentNode.getChildAt(i)))
				return NODE_NOT_FOUND;
			if(level > nodeLevel) {
				final NodeModel summaryNode = parentNode.getChildAt(i);
				if(summaryNode.isLeft() == leftSide)
					return i;
			}
		}
		return NODE_NOT_FOUND;
	}
	
	public NodeModel findGroupBeginNode(int index) {
		final int groupBeginNodeIndex = findGroupBeginNodeIndex(index);
		return parentNode.getChildAt(groupBeginNodeIndex);
	}
	
	public int findGroupBeginNodeIndex(int index) {
		if(index < 0)
			return NODE_NOT_FOUND;
		int nodeLevel = summaryLevels[index];
		final boolean leftSide = parentNode.getChildAt(index).isLeft();
		for (int i = index; i >= 0; i--){
			final int level = summaryLevels[i];
			final NodeModel groupBeginNode = parentNode.getChildAt(i);
			if(groupBeginNode.isLeft() == leftSide) {
				if(level > nodeLevel) {
					return parentNode.nextNodeIndex(i, leftSide);
				}
				if(level == nodeLevel) {
					if(SummaryNode.isFirstGroupNode(groupBeginNode))
						return i;
				}
			}
		}
		for (int i = 0; i <= index; i++){
			final NodeModel groupBeginNode = parentNode.getChildAt(i);
			if(groupBeginNode.isLeft() == leftSide &&  summaryLevels[i] == nodeLevel)
				return i;
		}
		return index;
	}
	
}