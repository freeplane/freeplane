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
		final int contentHeight;
		{
			final int childCount = getChildCount();
			if (childCount == 0) {
				contentHeight = 0;
			}
			else{
			int height = 0;
			int count = 0;
			for (int i = 0; i < childCount; i++) {
				final NodeView child = (NodeView) getView().getComponent(i);
				if (child.isLeft() == false) {
					final int additionalCloudHeigth = getAdditionalCloudHeigth(child);
					final int h = child.getContent().getHeight();
					height += h + additionalCloudHeigth;
					if (child.getHeight() - 2 * getSpaceAround() != 0) {
						count++;
					}
				}
			}
			if (count <= 1) {
				contentHeight = height;
			}
			else{
				contentHeight = height + getVGap() * (count - 1);
			}
			}
			
		}
		int childVerticalShift;
		{
			if (getChildCount() == 0) {
				childVerticalShift = 0;
			}
			else{
			int shift = 0;
			for (int i = 0; i < getChildCount(); i++) {
				final NodeView child = (NodeView) getView().getComponent(i);
				if (child.isLeft() == false) {
					final int childShift = child.getShift();
					if (childShift < 0 || i == 0) {
						shift += childShift;
					}
					final int contentShift = child.getContent().getY() - getSpaceAround();
					shift -= contentShift;
				}
			}
			childVerticalShift = shift;
			}
		}
		final int childHorizontalShift;
		{
			if (getChildCount() == 0) {
				childHorizontalShift = 0;
			}
			else{
				int shift = 0;
				for (int i = 0; i < getChildCount(); i++) {
					final NodeView child = (NodeView) getView().getComponent(i);
					int shiftCandidate;
					if (child.isLeft()) {
						shiftCandidate = -child.getContent().getX() - child.getContent().getWidth();
						if (child.isContentVisible()) {
							shiftCandidate -= child.getHGap() + getAdditionalCloudHeigth(child) / 2;
						}
					}
					else {
						shiftCandidate = -child.getContent().getX();
						if (child.isContentVisible()) {
							shiftCandidate += child.getHGap();
						}
					}
					shift = Math.min(shift, shiftCandidate);
				}
				childHorizontalShift = shift;
			}
		}
		if (getView().isContentVisible()) {
			getContent().setVisible(true);
			final Dimension contentPreferredSize = getContent().getPreferredSize();
			final int x = Math.max(getSpaceAround(), -contentPreferredSize.width - childHorizontalShift);
			final int contentVerticalShift = (contentPreferredSize.height - contentHeight) / 2;
			childVerticalShift += contentVerticalShift;
			final int y = getSpaceAround() + Math.max(0, -childVerticalShift);
			getContent().setBounds(x, y, contentPreferredSize.width, contentPreferredSize.height);
		}
		else {
			getContent().setVisible(false);
			final int x = Math.max(getSpaceAround(), -childHorizontalShift);
			final int y = getSpaceAround() + Math.max(0, -childVerticalShift);
			getContent().setBounds(x, y, 0, contentHeight);
		}
		final int[] lx = new int[getChildCount()];
		final int[] ly = new int[getChildCount()];
		final int baseX = getContent().getX() + getContent().getWidth();
		int y = getContent().getY() - getSpaceAround() + childVerticalShift;
		int right = baseX + getSpaceAround();;
		NodeView child = null;
		for (int i = 0; i < getChildCount(); i++) {
			final NodeView component = (NodeView) getView().getComponent(i);
			if (component.isLeft()) {
				continue;
			}
			child = component;
			final int additionalCloudHeigth = getAdditionalCloudHeigth(child) / 2;
			y += additionalCloudHeigth;
			final int shiftY = child.getShift();
			final int childHGap = child.getContent().isVisible() ? child.getHGap() : 0;
			final int x = baseX + childHGap - child.getContent().getX();
			if(i == 0){
				child.setLocation(x, y);
			}
			else if (shiftY < 0) {
				child.setLocation(x, y);
				y -= shiftY;
			}
			else {
				y += shiftY;
				child.setLocation(x, y);
			}
			final int childHeight = child.getHeight() - 2 * getSpaceAround();
			if (childHeight != 0) {
				y += childHeight + getVGap() + additionalCloudHeigth;
			}
			right = Math.max(right, x + child.getWidth() + additionalCloudHeigth);
		}
		final int bottom = getContent().getY() + getContent().getHeight() + getSpaceAround();
		if (child != null) {
			getView().setSize(right,
			    Math.max(bottom, child.getY() + child.getHeight() + getAdditionalCloudHeigth(child) / 2));
		}
		else {
			getView().setSize(right, bottom);
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
