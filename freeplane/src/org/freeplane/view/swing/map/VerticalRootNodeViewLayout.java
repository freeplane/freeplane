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

import java.awt.Container;

/**
 * @author Dimitry Polivaev
 */
public class VerticalRootNodeViewLayout extends NodeViewLayoutAdapter implements INodeViewLayout {
	static private INodeViewLayout instance = null;

	static INodeViewLayout getInstance() {
		if (VerticalRootNodeViewLayout.instance == null) {
			VerticalRootNodeViewLayout.instance = new VerticalRootNodeViewLayout();
		}
		return VerticalRootNodeViewLayout.instance;
	}

    public void layoutContainer(final Container c) {
        if(setUp(c)){
        	layout();
        }
    	shutDown();
    }

	private void layout() {
		final LayoutData layoutData = new LayoutData(getChildCount());
		calcLayout(true, layoutData);
		calcLayout(false, layoutData);
		placeChildren(layoutData);
	}
}
