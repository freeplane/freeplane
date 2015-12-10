package org.freeplane.features.map;

public class SummaryLevels{
	private static final boolean[] BOTH_SIDES = {true, false};
	private static final boolean[] LEFT_SIDE = {true};
	private static final boolean[] RIGHT_SIDE = {false};
	public  final int[] summaryLevels;
	public  final int highestSummaryLevel;
	public  final boolean[] sides;
	public SummaryLevels(NodeModel parentNode) {
		int highestSummaryLevel = 0;
		int childCount = parentNode.getChildCount();
		this.summaryLevels = new int[childCount];
		this.sides = sidesOf(parentNode);
		for(boolean isLeft : sides){

			int level = Integer.MAX_VALUE;
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

}