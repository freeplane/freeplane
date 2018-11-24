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
import org.freeplane.core.ui.components.resizer.OneTouchCollapseResizer.ComponentCollapseListener;

/**
 * @author Dimitry Polivaev
 * 01.02.2014
 */
public class CollapseableBoxBuilder {
	private String propertyNameBase;
	private boolean resizeable = true;
	public CollapseableBoxBuilder setResizeable(boolean resizeable) {
		this.resizeable = resizeable;
		return this;
	}
	public CollapseableBoxBuilder(){

	}
	public CollapseableBoxBuilder setPropertyNameBase(String name) {
	    this.propertyNameBase = name;
	    return this;
    }
	public Box createBox(final Component component, final Direction direction) {
	    Box resisableComponent = direction.createBox();
		UIComponentVisibilityDispatcher.install(resisableComponent, propertyNameBase);
		final UIComponentVisibilityDispatcher dispatcher = UIComponentVisibilityDispatcher.of(resisableComponent);
		final String sizePropertyName = dispatcher.getPropertyName() +  ".size";
		final boolean expanded = dispatcher.isVisible();

		OneTouchCollapseResizer resizer = new OneTouchCollapseResizer(direction);
		dispatcher.setResizer(resizer);
		switch(direction){
			case RIGHT:
			case DOWN:
				resisableComponent.add(resizer);
				resisableComponent.add(component);
				break;
			default:
				resisableComponent.add(component);
				resisableComponent.add(resizer);
				break;
		}
		if(resizeable){
			try {
				int size = ResourceController.getResourceController().getIntProperty(sizePropertyName, 0);
				if(size > 10) {
					direction.setPreferredSize(component, size);
				}
			}
			catch (Exception e) {
				// blindly accept
			}
			resizer.addResizerListener(new ResizerListener() {
				@Override
				public void componentResized(ResizeEvent event) {
					if(event.getComponent().equals(component)) {
						ResourceController.getResourceController().setProperty(sizePropertyName, String.valueOf(direction.getPreferredSize(component)));
					}
				}

			});
		}
		else
			resizer.setSliderLocked(true);
		resizer.addCollapseListener(new ComponentCollapseListener() {
			@Override
			public void componentCollapsed(ResizeEvent event) {
				if(event.getComponent().equals(component)) {
					dispatcher.setProperty(false);
				}
			}

			@Override
			public void componentExpanded(ResizeEvent event) {
				if(event.getComponent().equals(component)) {
					dispatcher.setProperty(true);
				}
			}
		});
		resizer.setExpanded(expanded);
	    return resisableComponent;
    }
}
