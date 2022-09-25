package org.freeplane.view.swing.map;

import java.awt.Graphics2D;
import java.awt.Point;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.view.swing.map.edge.EdgeView;
import org.freeplane.view.swing.map.edge.SummaryEdgeView;

class SummaryEdgePainter {
	SummaryEdgePainter(NodeView parent, boolean isLeft){
		this.isLeft = isLeft;
		int maxSize = parent.getComponentCount();
		xs = new int[maxSize];
		yMins = new int[maxSize];
		yMaxs = new int[maxSize];
		level = 0;
		maxLevel = 0;
		resetLevelValues(0);
		saveCurrentValues();
	}
	private void resetLevelValues(int level) {
		if(this.isLeft)
			xs[level] = Integer.MAX_VALUE;
		else
			xs[level] = Integer.MIN_VALUE;
		yMins[level] = Integer.MAX_VALUE;
		yMaxs[level] = Integer.MIN_VALUE;
	}
	private void saveCurrentValues() {
		currentX = xs[level];
		currentY1 = yMins[level];
		currentY2 = yMaxs[level];
	}
	
	final private int yMins[];
	final private int yMaxs[];
	final private int xs[];
	private int currentX;
	private int currentY1;
	private int currentY2;
	final private boolean isLeft;
	private int level;
	private int maxLevel;
	void addChild(NodeView child){
		if(child.isLeft() != isLeft)
			return;
		setCurrentLevel(child);
        updateLevelValues(child);
		
		
	}
	private void updateLevelValues(NodeView child) {
		resetLevelValuesForStart(child);
		int spaceAround = child.getSpaceAround();
		NodeViewLayoutHelper helper = child.getLayoutHelper();
        if(helper.getHeight() == 2 * spaceAround)
        	return;
		int yMin = helper.getY() + spaceAround;
		int yMax = helper.getY() + helper.getHeight() - helper.getSpaceAround();
        int x;
        if (isLeft) {
            x = helper.getX() + spaceAround;
        }
        else {
            x = helper.getX() + helper.getWidth() - spaceAround;
        }
        yMins[level] = Math.min(yMin, yMins[level]);
        yMaxs[level] = Math.max(yMax, yMaxs[level]);
		if (isLeft) {
			xs[level] = Math.min(x, xs[level]);
		}
		else {
			xs[level] = Math.max(x, xs[level]);
		}
	}
	private void resetLevelValuesForStart(NodeView child) {
		if(child.isFirstGroupNode())
			resetLevelValues(level);
		if(level > maxLevel){
			maxLevel = level;
			resetLevelValues(level);
		}
	}
	private void setCurrentLevel(NodeView child) {
		if(child.isSummary()){
			saveCurrentValues();
			resetLevelValues(level);
			level++;
		}
		else{
			level = 0;
		}
	}
	private boolean hasSummaryEdge(){
		return level > 0 && currentY1 != Integer.MAX_VALUE;
	}
	
	boolean paintSummaryEdge(Graphics2D g, NodeView source, NodeView target) {
		if(! hasSummaryEdge())
			return false;
		boolean usesHorizontalLayout = source.usesHorizontalLayout();
		final Point start1 = usesHorizontalLayout ? new Point(currentY1, currentX) : new Point(currentX, currentY1);
		final Point start2 = usesHorizontalLayout ? new Point(currentY2, currentX) : new Point(currentX, currentY2);
		final NodeView parentView = target.getParentView();
		UITools.convertPointToAncestor(parentView, start1, source);
		UITools.convertPointToAncestor(parentView, start2, source);
		final EdgeView edgeView = new SummaryEdgeView(source, target, source);
		edgeView.setStart(start1);
		edgeView.paint(g);
		edgeView.setStart(start2);
		edgeView.paint(g);
		return true;
	}
}
