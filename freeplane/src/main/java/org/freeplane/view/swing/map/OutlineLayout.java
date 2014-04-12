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

import javax.swing.JComponent;

import org.freeplane.core.resources.ResourceController;

/**
 * @author Dimitry Polivaev
 * 29.08.2009
 */
public class OutlineLayout extends NodeViewLayoutAdapter {
	private int hGap;

	protected int getHGap() {
		return hGap;
	}

	static private final NodeViewLayoutAdapter instance = new OutlineLayout();

    static NodeViewLayoutAdapter getInstance() {
        return OutlineLayout.instance;
    }
    
	@Override
	protected void layout() {
		final int x = getSpaceAround();
		final int y = x;
		final JComponent content = getContent();
		final NodeView view = getView();
		if (view.isContentVisible()) {
			getContent().setVisible(true);
			final Dimension contentProfSize = calculateContentSize(view);
			content.setBounds(x, y, contentProfSize.width, contentProfSize.height);
		}
		else {
			content.setVisible(false);
			content.setBounds(x, y, 0, 0);
		}
		placeChildren();
	}

	private void placeChildren() {
		int baseX = getContent().getX();
		int y = getContent().getY() + getContent().getHeight() - getSpaceAround();
		if (getContent().isVisible()) {
			baseX += getHGap();
			y += getVGap();
		}
		int right = baseX + getContent().getWidth() + getSpaceAround();
		NodeView child = null;
		for (int i = 0; i < getChildCount(); i++) {
			final NodeView component = (NodeView) getView().getComponent(i);
			child = component;
			final int additionalCloudHeigth = getAdditionalCloudHeigth(child) / 2;
			y += additionalCloudHeigth;
			final int childHGap = child.getContent().isVisible() ? getHGap() : 0;
			final int x = baseX + childHGap - child.getContent().getX();
			child.setLocation(x, y);
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

	@Override
	protected boolean setUp(final Container c) {
		if (! super.setUp(c)){
			return false;
		}
		final int vgap = ResourceController.getResourceController().getIntProperty("outline_vgap", 0);
		final int hgap = ResourceController.getResourceController().getIntProperty("outline_hgap", 0);
		setVGap(getView().getMap().getZoomed(vgap));
		hGap = getView().getMap().getZoomed(hgap);
		return true;
	}
}
