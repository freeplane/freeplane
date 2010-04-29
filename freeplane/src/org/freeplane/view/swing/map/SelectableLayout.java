/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;

import org.freeplane.features.common.addins.mapstyle.MapViewLayout;

/**
 * @author Dimitry Polivaev
 * 29.08.2009
 */
public class SelectableLayout implements INodeViewLayout {
	static final SelectableLayout selectableLayoutInstance = new SelectableLayout();

	public Point getMainViewInPoint(final NodeView view) {
		return getLayout(view).getMainViewInPoint(view);
	}

	public Point getMainViewOutPoint(final NodeView view, final NodeView targetView, final Point destinationPoint) {
		return getLayout(view).getMainViewOutPoint(view, targetView, destinationPoint);
	}

	public void layoutNodeMotionListenerView(final NodeMotionListenerView view) {
		getLayout(view.getMovedView()).layoutNodeMotionListenerView(view);
	}

	public void addLayoutComponent(final String name, final Component comp) {
	}

	public void layoutContainer(final Container parent) {
		getLayout(parent).layoutContainer(parent);
	}

	public Dimension minimumLayoutSize(final Container parent) {
		return getLayout(parent).minimumLayoutSize(parent);
	}

	public Dimension preferredLayoutSize(final Container parent) {
		return getLayout(parent).preferredLayoutSize(parent);
	}

	public void removeLayoutComponent(final Component comp) {
	}

	private INodeViewLayout getLayout(final Container parent) {
		final NodeView view = (NodeView) parent;
		final MapViewLayout layout = view.getMap().getLayoutType();
		if (layout == MapViewLayout.OUTLINE) {
			return OutlineLayout.getInstance();
		}
		if (view.isRoot()) {
			return VerticalRootNodeViewLayout.getInstance();
		}
		else {
			if (view.isLeft()) {
				return LeftNodeViewLayout.getInstance();
			}
			else {
				return RightNodeViewLayout.getInstance();
			}
		}
	}

	static LayoutManager getInstance() {
		return selectableLayoutInstance;
	}
}
