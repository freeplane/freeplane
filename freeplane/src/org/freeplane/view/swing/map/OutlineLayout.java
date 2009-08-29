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


import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;

/**
 * @author Dimitry Polivaev
 * 29.08.2009
 */
public class OutlineLayout extends NodeViewLayoutAdapter{
	static private final OutlineLayout instance =  new OutlineLayout();

	static OutlineLayout getInstance() {
		return OutlineLayout.instance;
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
		final int x = getSpaceAround();
		final int y = x;
		if (getView().isContentVisible()) {
			getContent().setVisible(true);
			final Dimension contentPreferredSize = getContent().getPreferredSize();
			getContent().setBounds(x, y, contentPreferredSize.width, contentPreferredSize.height);
		}
		else {
			getContent().setVisible(false);
			getContent().setBounds(x, y, 0, 0);
		}
		placeRightChildren();
	}

    protected void placeRightChildren() {
	    final int baseX = getContent().getX() + getVGap();
        int y = getContent().getY() + getContent().getHeight()+ getVGap() - + getSpaceAround();
        int right = baseX +  + getContent().getWidth() + getSpaceAround();
        NodeView child = null;
        for (int i = 0; i < getChildCount(); i++) {
        	final NodeView component = (NodeView) getView().getComponent(i);
        	child = component;
        	final int additionalCloudHeigth = child.getAdditionalCloudHeigth() / 2;
        	y += additionalCloudHeigth;
        	final int shiftY = 0;
        	final int childHGap = child.getContent().isVisible() ? getVGap() : 0;
        	final int x = baseX + childHGap - child.getContent().getX();
        	if (shiftY < 0) {
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
        	    Math.max(bottom, child.getY() + child.getHeight() + child.getAdditionalCloudHeigth() / 2));
        }
        else {
        	getView().setSize(right, bottom);
        }
    }

	@Override
    protected void setUp(Container c) {
	    super.setUp(c);
		setVGap(getView().getMap().getZoomed(5));
    }

	public void layoutNodeMotionListenerView(final NodeMotionListenerView view) {
		view.setBounds(0, 0, 0, 0);
	}
}
