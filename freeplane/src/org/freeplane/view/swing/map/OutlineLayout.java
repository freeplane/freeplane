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

import javax.swing.JComponent;

import org.freeplane.core.resources.ResourceController;

/**
 * @author Dimitry Polivaev
 * 29.08.2009
 */
public class OutlineLayout implements INodeViewLayout {
	private int hGap;

	protected int getHGap() {
		return hGap;
	}

	static private final INodeViewLayout instance = new OutlineLayout();

    static INodeViewLayout getInstance() {
        return OutlineLayout.instance;
    }
    
    public void layoutContainer(final Container c) {
        if(setUp(c)){
        	layout();
        }
        implementation.shutDown();
    }

	private void layout() {
		final int x = implementation.getSpaceAround();
		final int y = x;
		final JComponent content = implementation.getContent();
		final NodeView view = implementation.getView();
		if (view.isContentVisible()) {
			implementation.getContent().setVisible(true);
			final Dimension contentProfSize = implementation.calculateContentSize(view);
			content.setBounds(x, y, contentProfSize.width, contentProfSize.height);
		}
		else {
			content.setVisible(false);
			content.setBounds(x, y, 0, 0);
		}
		placeChildren();
	}

	private void placeChildren() {
		int baseX = implementation.getContent().getX();
		int y = implementation.getContent().getY() + implementation.getContent().getHeight() - implementation.getSpaceAround();
		if (implementation.getContent().isVisible()) {
			baseX += getHGap();
			y += implementation.getVGap();
		}
		int right = baseX + implementation.getContent().getWidth() + implementation.getSpaceAround();
		NodeView child = null;
		for (int i = 0; i < implementation.getChildCount(); i++) {
			final NodeView component = (NodeView) implementation.getView().getComponent(i);
			child = component;
			final int additionalCloudHeigth = implementation.getAdditionalCloudHeigth(child) / 2;
			y += additionalCloudHeigth;
			final int childHGap = child.getContent().isVisible() ? getHGap() : 0;
			final int x = baseX + childHGap - child.getContent().getX();
			child.setLocation(x, y);
			final int childHeight = child.getHeight() - 2 * implementation.getSpaceAround();
			if (childHeight != 0) {
				y += childHeight + implementation.getVGap() + additionalCloudHeigth;
			}
			right = Math.max(right, x + child.getWidth() + additionalCloudHeigth);
		}
		final int bottom = implementation.getContent().getY() + implementation.getContent().getHeight() + implementation.getSpaceAround();
		if (child != null) {
			implementation.getView().setSize(right,
			    Math.max(bottom, child.getY() + child.getHeight() + implementation.getAdditionalCloudHeigth(child) / 2));
		}
		else {
			implementation.getView().setSize(right, bottom);
		}
	}

	protected boolean setUp(final Container c) {
		if (! implementation.setUp(c)){
			return false;
		}
		final int vgap = ResourceController.getResourceController().getIntProperty("outline_vgap", 0);
		final int hgap = ResourceController.getResourceController().getIntProperty("outline_hgap", 0);
		implementation.setVGap(implementation.getView().getMap().getZoomed(vgap));
		hGap = implementation.getView().getMap().getZoomed(hgap);
		return true;
	}
	private NodeViewLayoutAdapter implementation = new NodeViewLayoutAdapter();
	public void addLayoutComponent(String name, Component comp) {
	}

	public void removeLayoutComponent(Component comp) {
	}

	public Dimension preferredLayoutSize(Container parent) {
		return implementation.preferredLayoutSize(parent);
	}

	public Dimension minimumLayoutSize(Container parent) {
		return NodeViewLayoutAdapter.minDimension;
	}
}
