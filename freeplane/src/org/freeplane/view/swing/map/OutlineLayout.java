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

	static private final INodeViewLayout instance = new OutlineLayout();

    static INodeViewLayout getInstance() {
        return OutlineLayout.instance;
    }
    
    public void layoutContainer(final Container c) {
 		final NodeView view = (NodeView) c;
 		JComponent content = view.getContent();
        
        if(content == null)
        	return;
        content.setVisible(view.isContentVisible());
		final int x = view.getSpaceAround();
		final int y = x;
		final Dimension contentProfSize = ContentSizeCalculator.INSTANCE.calculateContentSize(view);
		content.setBounds(x, y, contentProfSize.width, contentProfSize.height);
		placeChildren(view);
	}

	private void placeChildren(NodeView view) {
        final int childCount = view.getComponentCount() - 1;
        for (int i = 0; i < childCount; i++) {
            final Component component = view.getComponent(i);
            ((NodeView) component).validateTree();
        }
        int spaceAround = view.getSpaceAround();
		final int hgapProperty = ResourceController.getResourceController().getIntProperty("outline_hgap", 0);
		int hgap = view.getMap().getZoomed(hgapProperty);
		final int vgapPropertyValue = ResourceController.getResourceController().getIntProperty("outline_vgap", 0);
		int vgap = view.getMap().getZoomed(vgapPropertyValue);
		JComponent content = view.getContent();
		int baseX = content.getX();
		int y = content.getY() + content.getHeight() - spaceAround;
		if (content.isVisible()) {
			baseX += hgap;
			y += vgap;
		}
		int right = baseX + content.getWidth() + spaceAround;
		NodeView child = null;
		for (int i = 0; i < childCount; i++) {
			final NodeView component = (NodeView) view.getComponent(i);
			child = component;
			final int additionalCloudHeigth = CloudHeightCalculator.INSTANCE.getAdditionalCloudHeigth(child) / 2;
			y += additionalCloudHeigth;
			final int childHGap = child.getContent().isVisible() ? hgap : 0;
			final int x = baseX + childHGap - child.getContent().getX();
			child.setLocation(x, y);
			final int childHeight = child.getHeight() - 2 * spaceAround;
			if (childHeight != 0) {
				y += childHeight + vgap + additionalCloudHeigth;
			}
			right = Math.max(right, x + child.getWidth() + additionalCloudHeigth);
		}
		final int bottom = content.getY() + content.getHeight() + spaceAround;
		if (child != null) {
			final NodeView node = child;
			view.setSize(right,
			    Math.max(bottom, child.getY() + child.getHeight() + CloudHeightCalculator.INSTANCE.getAdditionalCloudHeigth(node) / 2));
		}
		else {
			view.setSize(right, bottom);
		}
	}

	
	public void addLayoutComponent(String name, Component comp) {
	}

	public void removeLayoutComponent(Component comp) {
	}

	public Dimension preferredLayoutSize(Container parent) {
		return ImmediatelyValidatingPreferredSizeCalculator.INSTANCE.preferredLayoutSize(parent);
	}

	public Dimension minimumLayoutSize(Container parent) {
		return INodeViewLayout.ZERO_DIMENSION;
	}
}
