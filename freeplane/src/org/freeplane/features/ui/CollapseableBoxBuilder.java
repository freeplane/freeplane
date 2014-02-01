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
package org.freeplane.features.ui;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.JResizer.Direction;
import org.freeplane.core.ui.components.OneTouchCollapseResizer;
import org.freeplane.core.ui.components.OneTouchCollapseResizer.ComponentCollapseListener;
import org.freeplane.core.ui.components.ResizeEvent;
import org.freeplane.core.ui.components.ResizerListener;

/**
 * @author Dimitry Polivaev
 * 01.02.2014
 */
public class CollapseableBoxBuilder {
	private final FrameController frameController;
	public CollapseableBoxBuilder(final FrameController frameController){
		this.frameController = frameController;

	}
	public Box createBox(final JTabbedPane component) {
	    Box resisableComponent = Box.createHorizontalBox();
		UIComponentVisibilityDispatcher.install(frameController, resisableComponent, "styleScrollPaneVisible");
		final UIComponentVisibilityDispatcher dispatcher = UIComponentVisibilityDispatcher.dispatcher(resisableComponent);
		final String TABBEDPANE_VIEW_WIDTH = "tabbed_pane.width";
		final boolean expanded = dispatcher.isVisible();

		OneTouchCollapseResizer propertyPanelResizer = new OneTouchCollapseResizer(Direction.RIGHT);
		resisableComponent.add(propertyPanelResizer);
		//resisableTabs.add(new JResizer(Direction.RIGHT));
		resisableComponent.add(component);
		propertyPanelResizer.addResizerListener(new ResizerListener() {
			public void componentResized(ResizeEvent event) {
				if(event.getComponent().equals(component)) {
					ResourceController.getResourceController().setProperty(TABBEDPANE_VIEW_WIDTH, String.valueOf(((JComponent) event.getComponent()).getPreferredSize().width));
				}
			}
		});
		propertyPanelResizer.addCollapseListener(new ComponentCollapseListener() {
			public void componentCollapsed(ResizeEvent event) {
				if(event.getComponent().equals(component)) {
					dispatcher.setProperty(false);
				}
			}

			public void componentExpanded(ResizeEvent event) {
				if(event.getComponent().equals(component)) {
					dispatcher.setProperty(true);
				}
			}
		});
		try {
			int width = Integer.parseInt(ResourceController.getResourceController().getProperty(TABBEDPANE_VIEW_WIDTH, "350"));
			if(width <= 10) {
				width = 350;
			}
			component.setPreferredSize(new Dimension(width, 40));
		}
		catch (Exception e) {
			// blindly accept
		}
		propertyPanelResizer.setExpanded(expanded);
	    return resisableComponent;
    }
}
