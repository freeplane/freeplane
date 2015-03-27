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
import java.awt.Container;
import java.awt.Dimension;

/**
 * @author Dimitry Polivaev
 */
public class LeftNodeViewLayout implements INodeViewLayout{
	static private LeftNodeViewLayout instance = null;

	static INodeViewLayout getInstance() {
		if (LeftNodeViewLayout.instance == null) {
			LeftNodeViewLayout.instance = new LeftNodeViewLayout();
		}
		return LeftNodeViewLayout.instance;
	}

    public void layoutContainer(final Container c) {
        NodeView view = (NodeView) c;
 		if(view.getContent() != null){
        	final VerticalNodeViewLayoutStrategy layoutData = new VerticalNodeViewLayoutStrategy(view);
        	layoutData.layoutLeftSide();
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
