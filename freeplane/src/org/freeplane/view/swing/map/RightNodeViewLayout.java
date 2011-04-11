/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
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
import java.awt.Point;

import javax.swing.JComponent;

import org.freeplane.core.ui.components.UITools;

/**
 * @author Dimitry Polivaev
 */
public class RightNodeViewLayout extends NodeViewLayoutAdapter {
	static private final RightNodeViewLayout instance = new RightNodeViewLayout();

	static RightNodeViewLayout getInstance() {
		return RightNodeViewLayout.instance;
	}

	public Point getMainViewInPoint(final NodeView view) {
		final MainView mainView = view.getMainView();
		return mainView.getLeftPoint();
	}

	public Point getMainViewOutPoint(final NodeView view, final NodeView targetView, final Point destinationPoint) {
		final MainView mainView = view.getMainView();
		return mainView.getRightPoint();
	}

	@Override
	protected void layout() {
		final int[] lx = new int[getChildCount() + 2];
		final int[] ly = new int[getChildCount() + 2];
		calcLayout(false, lx, ly);
		placeRightChildren(lx, ly);
	}

	void calcLayout(final boolean isLeft, final int[] lx, final int[] ly) {
	    int y = 0;
		int right = 0;
		int childContentHeight = 0;
		boolean visibleChildFound = false;
		int childHorizontalShift = 0;
		int childVerticalShift = 0;
		for (int i = 0; i < getChildCount(); i++) {
			final NodeView child = (NodeView) getView().getComponent(i);
			if (child.isLeft() != isLeft) {
				continue;
			}
			final int additionalCloudHeigth = getAdditionalCloudHeigth(child);
			final int h = child.getContent().getHeight();
			childContentHeight += h + additionalCloudHeigth;
			if (child.getHeight() - 2 * getSpaceAround() != 0) {
				if (visibleChildFound)
					childContentHeight +=  getVGap();
				else
					visibleChildFound = true;
			}
			
			int shiftCandidate;
			shiftCandidate = -child.getContent().getX();
			if (child.isContentVisible()) {
				shiftCandidate += child.getHGap();
			}
			childHorizontalShift = Math.min(childHorizontalShift, shiftCandidate);

			final int childShift = child.getShift();
			if (childShift < 0 || i == 0) {
				childVerticalShift += childShift;
			}
			final int contentShift = child.getContent().getY() - getSpaceAround();
			childVerticalShift -= contentShift;
			
			y += additionalCloudHeigth/2;
			final int shiftY = child.getShift();
			final int childHGap = child.getContent().isVisible() ? child.getHGap() : 0;
			final int x = childHGap - child.getContent().getX();
			if(i == 0){
				lx[i] = x; ly[i] = y;
			}
			else if (shiftY < 0) {
				lx[i] = x; ly[i] = y;
				y -= shiftY;
			}
			else {
				y += shiftY;
				lx[i] = x; ly[i] = y;
			}
			final int childHeight = child.getHeight() - 2 * getSpaceAround();
			if (childHeight != 0) {
				y += childHeight + getVGap() + additionalCloudHeigth/2;
			}
			right = Math.max(right, x + child.getWidth() + additionalCloudHeigth/2);
		}
		if (getView().isContentVisible()) {
			final Dimension contentPreferredSize = getContent().getPreferredSize();
			final int contentVerticalShift = (contentPreferredSize.height - childContentHeight) / 2;
			childVerticalShift += contentVerticalShift;
		}
		
		lx[lx.length - 2] = right;
		ly[ly.length - 2] = childContentHeight;
		lx[lx.length - 1] = childHorizontalShift;
		ly[ly.length - 1] = childVerticalShift;
    }

	void placeRightChildren(final int[] lx, final int[] ly) {
		int right = lx[lx.length - 2];
		int childContentHeight = ly[ly.length - 2];
		int childHorizontalShift = lx[lx.length - 1];
		int childVerticalShift = ly[ly.length - 1];
	    final int contentX;
		final int contentY;
		final int contentHeight;
		final int contentWidth;
		if (getView().isContentVisible()) {
			final Dimension contentPreferredSize = getContent().getPreferredSize();
			contentWidth = contentPreferredSize.width;
			contentHeight = contentPreferredSize.height;
			contentX = Math.max(getSpaceAround(), -contentWidth - childHorizontalShift);
			contentY = getSpaceAround() + Math.max(0, -childVerticalShift);
		}
		else {
			contentX = Math.max(getSpaceAround(), -childHorizontalShift);
			contentY = getSpaceAround() + Math.max(0, -childVerticalShift);
			contentWidth = 0;
			contentHeight = childContentHeight;
		}
		
		if (getView().isContentVisible()) {
			getContent().setVisible(true);
		}
		else {
			getContent().setVisible(false);
		}
		getContent().setBounds(contentX, contentY, contentWidth, contentHeight);
		
		final int baseX = contentX + contentWidth;
		final int baseY = contentY - getSpaceAround() + ly[ly.length - 1];

		NodeView child = null;
		for (int i = 0; i < getChildCount(); i++) {
			child = (NodeView) getView().getComponent(i);
			child.setLocation(baseX + lx[i], baseY + ly[i]);
		}
		
		final int bottom = contentY + contentHeight + getSpaceAround();
		if (child != null) {
			getView().setSize(baseX + Math.max(right, getSpaceAround()),
			    Math.max(bottom, child.getY() + child.getHeight() + getAdditionalCloudHeigth(child) / 2));
		}
		else {
			getView().setSize(baseX + Math.max(right, getSpaceAround()), bottom);
		}
    }

	public void layoutNodeMotionListenerView(final NodeMotionListenerView view) {
		final NodeView movedView = view.getMovedView();
		final JComponent content = movedView.getContent();
		location.x = -LISTENER_VIEW_WIDTH;
		location.y = 0;
		UITools.convertPointToAncestor(content, location, view.getParent());
		view.setLocation(location);
		view.setSize(LISTENER_VIEW_WIDTH, content.getHeight());
	}
}
