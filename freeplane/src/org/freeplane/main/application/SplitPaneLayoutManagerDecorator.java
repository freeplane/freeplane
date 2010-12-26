/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 dimitry
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
import java.awt.LayoutManager;

import javax.swing.JSplitPane;

/**
 * @author Dimitry Polivaev
 * Dec 26, 2010
 */
final class SplitPaneLayoutManagerDecorator implements LayoutManager {
    private final LayoutManager lm;

    SplitPaneLayoutManagerDecorator(LayoutManager lm) {
	    this.lm = lm;
    }

    public void removeLayoutComponent(Component comp) {
    	lm.removeLayoutComponent(comp);
    }

    public Dimension preferredLayoutSize(Container parent) {
    	setDividerVisible(parent);
    	return lm.preferredLayoutSize(parent);
    }

    public Dimension minimumLayoutSize(Container parent) {
    	return lm.minimumLayoutSize(parent);
    }

    public void layoutContainer(Container parent) {
    	setDividerVisible(parent);
    	lm.layoutContainer(parent);
    }

    private void setDividerVisible(Container parent) {
    	final JSplitPane splitPane = (JSplitPane) parent;
        final Component leftComponent = splitPane.getLeftComponent();
        final Component rightComponent = splitPane.getRightComponent();
    	final boolean bothComponentsVisible = rightComponent != null&& rightComponent.isVisible();
    	for(int i = 0; i < splitPane.getComponentCount(); i++){
    		final Component c = splitPane.getComponent(i);
    		if(c != leftComponent && c.isVisible() != bothComponentsVisible)
    			c.setVisible(bothComponentsVisible);
    	}
    }

    public void addLayoutComponent(String name, Component comp) {
    	lm.addLayoutComponent(name, comp);
    }
}