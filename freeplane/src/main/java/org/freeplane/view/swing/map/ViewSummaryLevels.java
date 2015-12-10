package org.freeplane.view.swing.map;

class ViewSummaryLevels{
	final int[] summaryLevels;
	final int highestSummaryLevel;
	boolean[] sides;
	ViewSummaryLevels(NodeView view, boolean[] sides) {
		int highestSummaryLevel = 0;
		this.sides = sides;
		int childViewCount = view.getComponentCount() - 1;
		this.summaryLevels = new int[childViewCount];
		for(boolean isLeft : sides){

			int level = Integer.MAX_VALUE;
			boolean useSummaryAsItem = true;
			for (int i = 0; i < childViewCount; i++) {
				final NodeView child = (NodeView) view.getComponent(i);
				if (child.isLeft() == isLeft) {
					final boolean isItem = !child.isSummary() || useSummaryAsItem;
					if (isItem) {
						if (level > 0)
							useSummaryAsItem = true;
						level = 0;
						if (child.isContentVisible()) {
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