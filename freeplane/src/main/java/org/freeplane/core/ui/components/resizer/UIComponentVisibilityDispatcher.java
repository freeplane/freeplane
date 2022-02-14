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

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JRootPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;

/**
 * @author Dimitry Polivaev
 * 01.02.2014
 */
public class UIComponentVisibilityDispatcher {
	private static String DISPATCHER_CLIENT_PROPERTY_NAME = UIComponentVisibilityDispatcher.class.getName() + ".KEY";
	private final String propertyBaseName;
	private final JComponent component;
	private OneTouchCollapseResizer resizer;

	public void setResizer(OneTouchCollapseResizer resizer) {
		this.resizer = resizer;
	}

	public static UIComponentVisibilityDispatcher install(JComponent component, String propertyBaseName){
		UIComponentVisibilityDispatcher dispatcher = new UIComponentVisibilityDispatcher(component, propertyBaseName);
		component.putClientProperty(DISPATCHER_CLIENT_PROPERTY_NAME, dispatcher);
		return dispatcher;
	}
	
	public static void uninstall(JComponent component){
		component.putClientProperty(DISPATCHER_CLIENT_PROPERTY_NAME, null);
	}

	static public UIComponentVisibilityDispatcher of(JComponent component) {
		return ((UIComponentVisibilityDispatcher)component.getClientProperty(DISPATCHER_CLIENT_PROPERTY_NAME));
	}

	private UIComponentVisibilityDispatcher(JComponent component, String propertyBaseName) {
		this.component = component;
		this.propertyBaseName = propertyBaseName;
    }

	private String completeVisiblePropertyName() {
		final String completeKeyString;
		if (isContainedInFullScreenWindow()) {
			completeKeyString = propertyBaseName + ".fullscreen";
		}
		else {
			completeKeyString = propertyBaseName;
		}
		return completeKeyString;
	}

	private boolean isContainedInFullScreenWindow() {
		JRootPane rootPane = component.getRootPane();
		return rootPane != null && Boolean.TRUE.equals(rootPane.getClientProperty(ViewController.FULLSCREEN_ENABLED_PROPERTY));
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

	void setProperty(final boolean visible) {
	    final ResourceController resourceController = ResourceController.getResourceController();
	    resourceController.setProperty(getPropertyName(), visible);
    }

	private void makeComponentVisible(final boolean visible) {
		if(resizer == null)
			component.setVisible(visible);
		else {
			final boolean containedInFullWindow = isContainedInFullScreenWindow();
			if (visible || ! containedInFullWindow || containedInFullWindow && ! visible)
				resizer.setVisible(visible || ! containedInFullWindow);
			resizer.setExpanded(visible);
		}
    }

	public String getPropertyName() {
		return completeVisiblePropertyName();
	}
	
	public String getPropertyBaseName() {
		return propertyBaseName;
	}

	public boolean isVisible() {
		final String completeKeyString = completeVisiblePropertyName();
		if (completeKeyString == null) {
			return true;
		}
		return !"false".equals(ResourceController.getResourceController().getProperty(completeKeyString, "true"));
	}

	public void resetVisible() {
	    setVisible(isVisible());
    }
}
