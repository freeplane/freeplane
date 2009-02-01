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
public class LeftNodeViewLayout extends NodeViewLayoutAdapter {
	static private LeftNodeViewLayout instance = null;

	static LeftNodeViewLayout getInstance() {
		if (LeftNodeViewLayout.instance == null) {
			LeftNodeViewLayout.instance = new LeftNodeViewLayout();
		}
		return LeftNodeViewLayout.instance;
	}

	public Point getMainViewInPoint(final NodeView view) {
		final MainView mainView = view.getMainView();
		return mainView.getRightPoint();
	}

	public Point getMainViewOutPoint(final NodeView view, final NodeView targetView, final Point destinationPoint) {
		final MainView mainView = view.getMainView();
		return mainView.getLeftPoint();
	}

	@Override
	protected void layout() {
		final int contentHeight = getChildContentHeight(true);
		int childVerticalShift = getChildVerticalShift(true);
		final int childHorizontalShift = getChildHorizontalShift();
		final int x = Math.max(getSpaceAround(), -childHorizontalShift);
		if (getView().isContentVisible()) {
			getContent().setVisible(true);
			final Dimension contentPreferredSize = getContent().getPreferredSize();
			childVerticalShift += (contentPreferredSize.height - contentHeight) / 2;
			final int y = Math.max(getSpaceAround(), -childVerticalShift);
			getContent().setBounds(x, y, contentPreferredSize.width, contentPreferredSize.height);
		}
		else {
			getContent().setVisible(false);
			final int y = Math.max(getSpaceAround(), -childVerticalShift);
			getContent().setBounds(x, y, 0, contentHeight);
		}
		placeLeftChildren(childVerticalShift);
	}

	public void layoutNodeMotionListenerView(final NodeMotionListenerView view) {
		final NodeView movedView = view.getMovedView();
		final JComponent content = movedView.getContent();
		location.x = content.getWidth();
		location.y = 0;
		UITools.convertPointToAncestor(content, location, view.getParent());
		view.setLocation(location);
		view.setSize(LISTENER_VIEW_WIDTH, content.getHeight());
	}
}
