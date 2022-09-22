package org.freeplane.features.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog.MessageType;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.Filter;

public class SummaryLevels{
    private static final Filter TRANSPARENT_FILTER = new Filter(null, false, false, false, false, null);
	public static final int NODE_NOT_FOUND = -1;
	private static final boolean[] BOTH_SIDES = {true, false};
	private static final boolean[] LEFT_SIDE = {true};
	private static final boolean[] RIGHT_SIDE = {false};
	public  final int[] summaryLevels;
	public  final int highestSummaryLevel;
	public  final boolean[] sides;
	private final NodeModel parentNode;
	private final NodeModel root;
	
	public SummaryLevels(NodeModel root, NodeModel parentNode) {
		this(root, parentNode, TRANSPARENT_FILTER, false);
	}
	
	public static SummaryLevels of(NodeModel root, NodeModel parentNode, Filter filter) {
		return new SummaryLevels(root, parentNode, filter, false);
	}

	
	public static SummaryLevels ignoringChildNodes(NodeModel root, NodeModel parentNode, Filter filter) {
		return new SummaryLevels(root, parentNode, filter, true);
	}
	
	private SummaryLevels(NodeModel root, NodeModel parentNode, Filter filter, boolean ignoreChildNodes) {
		this.root = root;
		this.parentNode = parentNode;
		int highestSummaryLevel = 0;
		int childCount = ignoreChildNodes ? 0 : parentNode.getChildCount();
		this.summaryLevels = new int[childCount];
		this.sides = sidesOf(root, parentNode);
		for(boolean isLeft : sides){

			int level = 1;
			boolean useSummaryAsItem = true;
			for (int i = 0; i < childCount; i++) {
				final NodeModel child = parentNode.getChildAt(i);
				if (child.isLeft(root) == isLeft) {
					final boolean isItem = !SummaryNode.isSummaryNode(child) || useSummaryAsItem;
					if (isItem) {
						if (level > 0)
							useSummaryAsItem = true;
						level = 0;
						if (child.hasVisibleContent(filter)) {
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
	static private boolean[] sidesOf(NodeModel root, NodeModel parentNode) {
		return parentNode == root ? BOTH_SIDES : parentNode.isLeft(root) ? LEFT_SIDE : RIGHT_SIDE;
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
					if(sides != BOTH_SIDES || parentNode.getChildAt(i).isLeft(root) == summaryNode.isLeft(root))
						return arrayList;
				} else if (level == summaryLevel - 1) {
					final NodeModel child = parentNode.getChildAt(i);
					if (sides != BOTH_SIDES || child.isLeft(root) == summaryNode.isLeft(root)) {
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
		final boolean leftSide = parentNode.getChildAt(index).isLeft(root);
		for (int i = index + 1; i < parentNode.getChildCount(); i++){
			final int level = summaryLevels[i];
			if(level == nodeLevel && SummaryNode.isFirstGroupNode(parentNode.getChildAt(i)))
				return NODE_NOT_FOUND;
			if(level > nodeLevel) {
				final NodeModel summaryNode = parentNode.getChildAt(i);
				if(summaryNode.isLeft(root) == leftSide)
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
		final boolean leftSide = parentNode.getChildAt(index).isLeft(root);
		for (int i = index; i >= 0; i--){
			final int level = summaryLevels[i];
			final NodeModel groupBeginNode = parentNode.getChildAt(i);
			if(groupBeginNode.isLeft(root) == leftSide) {
				if(level > nodeLevel) {
					return parentNode.nextNodeIndex(root, i, leftSide);
				}
				if(level == nodeLevel) {
					if(SummaryNode.isFirstGroupNode(groupBeginNode))
						return i;
				}
			}
		}
		for (int i = 0; i <= index; i++){
			final NodeModel groupBeginNode = parentNode.getChildAt(i);
			if(groupBeginNode.isLeft(root) == leftSide &&  summaryLevels[i] == nodeLevel)
				return i;
		}
		return index;
	}

	public boolean canInsertSummaryNode(int start, int end, boolean isLeft) {
    	int summaryLevel = summaryLevels[start];
    	if (summaryLevel != summaryLevels[end]) {
			UITools.errorMessage(TextUtils.getText("summary_not_possible"));
			return false;
		}
    	
    	boolean nodesOnOtherSideFound = false;
        for(int i = start+1; i <= end; i++){
        	 NodeModel node = parentNode.getChildAt(i);
             boolean nodeIsOnTheSameSide = isLeft == node.isLeft(root);
			if(nodeIsOnTheSameSide && 
            		 (summaryLevels[i] > summaryLevel
            		 || summaryLevels[i] == summaryLevel && SummaryNode.isFirstGroupNode(node))) {
            	 UITools.errorMessage(TextUtils.getText("summary_not_possible"));
            	 return false;
             }
			nodesOnOtherSideFound = nodesOnOtherSideFound || ! nodeIsOnTheSameSide;
        }
        if(findSummaryNodeIndex(end) != NODE_NOT_FOUND) {
        	UITools.errorMessage(TextUtils.getText("summary_not_possible"));
        	return false;
        }
        if(nodesOnOtherSideFound
        		&&
        	OptionalDontShowMeAgainDialog.show("ignoreNodesOnOtherSide", 
        			MessageType.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED) != JOptionPane.OK_OPTION)
        	return false;
        return true;
	}
	
}