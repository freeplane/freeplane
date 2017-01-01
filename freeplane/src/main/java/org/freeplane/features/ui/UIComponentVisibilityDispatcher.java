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

import java.awt.Container;

import javax.swing.JComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.OneTouchCollapseResizer;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * 01.02.2014
 */
public class UIComponentVisibilityDispatcher {
	private static String KEY = UIComponentVisibilityDispatcher.class.getName() + ".KEY";
	private final FrameController frameController;
	private final String key;
	private final JComponent component;
	private OneTouchCollapseResizer resizer;

	public void setResizer(OneTouchCollapseResizer resizer) {
		this.resizer = resizer;
	}

	public static void install(FrameController frameController, JComponent component, String key){
		component.putClientProperty(KEY, new UIComponentVisibilityDispatcher(frameController, component, key));
	}

	public static UIComponentVisibilityDispatcher dispatcher(JComponent component){
		return (UIComponentVisibilityDispatcher) component.getClientProperty(KEY);
	}

	public UIComponentVisibilityDispatcher(FrameController frameController, JComponent component, String key) {
		this.frameController = frameController;
		this.component = component;
		this.key = key;
    }

	public String completeVisiblePropertyKey() {
		final String completeKeyString;
		if (frameController.isMenuComponentInFullScreenMode()) {
			completeKeyString = key + ".fullscreen";
		}
		else {
			completeKeyString = key;
		}
		return frameController.getPropertyKeyPrefix() + completeKeyString;
	}

	public void toggleVisibility() {
	    final ResourceController resourceController = ResourceController.getResourceController();
		final boolean wasVisible = resourceController.getBooleanProperty(getPropertyName());
		setVisible(!wasVisible);
    }

	public void setVisible(final boolean visible) {
	    setProperty(visible);
		makeComponentVisible(visible);
		final Container parent = component.getParent();
		if(parent != null)
			((JComponent) parent).revalidate();
		if (!visible) {
			IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
			mapViewManager.moveFocusFromDescendantToSelection(component);
		}

    }

	public void setProperty(final boolean visible) {
	    final ResourceController resourceController = ResourceController.getResourceController();
	    resourceController.setProperty(getPropertyName(), visible);
    }

	private void makeComponentVisible(final boolean visible) {
		if(resizer == null)
			component.setVisible(visible);
		else {
			if (visible || frameController.isMenuComponentInFullScreenMode() && ! visible)
				resizer.setVisible(visible);
			resizer.setExpanded(visible);
		}
    }

	public String getPropertyName() {
		final String propertyName = completeVisiblePropertyKey();
		return propertyName;
	}

	public boolean isVisible() {
		final String completeKeyString = completeVisiblePropertyKey();
		if (completeKeyString == null) {
			return true;
		}
		return !"false".equals(ResourceController.getResourceController().getProperty(completeKeyString, "true"));
	}

	public void resetVisible() {
	    setVisible(isVisible());
    }
}
