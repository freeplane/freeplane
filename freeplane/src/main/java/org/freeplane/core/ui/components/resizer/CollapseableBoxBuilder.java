/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 Dimitry
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
package org.freeplane.core.ui.components.resizer;

import java.awt.Component;

import javax.swing.Box;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.resizer.JResizer.Direction;

/**
 * @author Dimitry Polivaev
 * 01.02.2014
 */
public class CollapseableBoxBuilder {
	private String propertyNameBase;
	private boolean resizeable = true;
	public CollapseableBoxBuilder disableResizer() {
		this.resizeable = false;
		return this;
	}
	public CollapseableBoxBuilder(String propertyNameBase){
		this.propertyNameBase = propertyNameBase;

	}
	public Box createBox(final Component component, final Direction direction) {
	    Box resizerBox = direction.createBox();
		UIComponentVisibilityDispatcher.install(resizerBox, propertyNameBase);
		final UIComponentVisibilityDispatcher dispatcher = UIComponentVisibilityDispatcher.of(resizerBox);
		final String sizePropertyName = dispatcher.getPropertyName() +  ".size";
		final boolean expanded = dispatcher.isVisible();

		OneTouchCollapseResizer resizer = new OneTouchCollapseResizer(direction);
		dispatcher.setResizer(resizer);
		switch(direction){
			case RIGHT:
			case DOWN:
				resizerBox.add(resizer);
				resizerBox.add(component);
				break;
			default:
				resizerBox.add(component);
				resizerBox.add(resizer);
				break;
		}
		if(resizeable){
			int size = ResourceController.getResourceController().getIntProperty(sizePropertyName, 0);
			if(size > resizer.getDividerSize()) {
				direction.setPreferredSize(component, size);
			}
		}
		else
			resizer.setSliderLocked(true);
		resizer.setExpanded(expanded);
		return resizerBox;
    }
}
