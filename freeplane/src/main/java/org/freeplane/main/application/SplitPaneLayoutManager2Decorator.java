/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2022 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.main.application;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;

import javax.swing.JSplitPane;

/**
 * @author Dimitry Polivaev
 */
class SplitPaneLayoutManager2Decorator extends SplitPaneLayoutManagerDecorator implements LayoutManager2 {
    private final LayoutManager2 lm;

    SplitPaneLayoutManager2Decorator(LayoutManager2 lm) {
    	super(lm);
	    this.lm = lm;
    }

    public void addLayoutComponent(String name, Component comp) {
    	lm.addLayoutComponent(name, comp);
    }

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		lm.addLayoutComponent(comp, constraints);
		
	}

	public Dimension maximumLayoutSize(Container parent) {
	    final JSplitPane splitPane = (JSplitPane) parent;
    	if(isDividerRequired(splitPane))
    		return lm.maximumLayoutSize(parent);
    	return splitPane.getLeftComponent().getMaximumSize();  		
	}

	public float getLayoutAlignmentX(Container target) {
		return lm.getLayoutAlignmentX(target);
	}

	public float getLayoutAlignmentY(Container target) {
		return lm.getLayoutAlignmentY(target);
	}

	public void invalidateLayout(Container target) {
		lm.invalidateLayout(target);
	}

	
}