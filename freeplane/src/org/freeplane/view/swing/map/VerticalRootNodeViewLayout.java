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

import java.awt.Point;

import javax.swing.JComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;

/**
 * @author Dimitry Polivaev
 */
public class VerticalRootNodeViewLayout extends NodeViewLayoutAdapter {
	static private VerticalRootNodeViewLayout instance = null;

	static VerticalRootNodeViewLayout getInstance() {
		if (VerticalRootNodeViewLayout.instance == null) {
			VerticalRootNodeViewLayout.instance = new VerticalRootNodeViewLayout();
		}
		return VerticalRootNodeViewLayout.instance;
	}

	@Override
	protected void layout() {
		final LayoutData layoutData = new LayoutData(getChildCount());
		calcLayout(true, layoutData);
		calcLayout(false, layoutData);
		placeChildren(layoutData);
	}

	public void layoutNodeMotionListenerView(final NodeMotionListenerView view) {
		final NodeView movedView = view.getMovedView();
		final JComponent content = movedView.getContent();
		location.x = 0;
		location.y = -LISTENER_VIEW_WIDTH;
		UITools.convertPointToAncestor(content, location, view.getParent());
		view.setLocation(location);
		view.setSize(content.getWidth(), LISTENER_VIEW_WIDTH);
	}
}
