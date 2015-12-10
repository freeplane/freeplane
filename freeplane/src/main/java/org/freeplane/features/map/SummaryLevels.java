package org.freeplane.features.map;

public class SummaryLevels{
	public  final int[] summaryLevels;
	public  final int highestSummaryLevel;
	public SummaryLevels(NodeModel parentNode, boolean[] sides) {
		int highestSummaryLevel = 0;
		int childCount = parentNode.getChildCount();
		this.summaryLevels = new int[childCount];
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

}