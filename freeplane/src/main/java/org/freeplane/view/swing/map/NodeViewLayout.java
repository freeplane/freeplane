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

import org.freeplane.features.treetable.TreeTableModel;

/**
 * @author Dimitry Polivaev
 */
public class NodeViewLayout implements INodeViewLayout{
	static private NodeViewLayout instance = null;

	static INodeViewLayout getInstance() {
		if (NodeViewLayout.instance == null) {
			NodeViewLayout.instance = new NodeViewLayout();
		}
		return NodeViewLayout.instance;
	}

    public void layoutContainer(final Container c) {
        NodeView view = (NodeView) c;
 		if(view.getContent() != null){
			final VerticalNodeViewLayoutStrategy layoutData = new VerticalNodeViewLayoutStrategy(view);
        	layoutData.calculateLayoutData();
        	if (TreeTableModel.isTreeTableRoot(view.getNode()))
        		TreeTableCellPlacement.place(view);
        }
    }

	public void addLayoutComponent(String name, Component c) {
	}

	public void removeLayoutComponent(Component c) {
	}

	public Dimension preferredLayoutSize(Container c) {
		return ImmediatelyValidatingPreferredSizeCalculator.INSTANCE.preferredLayoutSize(c);
	}

	public Dimension minimumLayoutSize(Container c) {
		return INodeViewLayout.ZERO_DIMENSION;
	}
}
