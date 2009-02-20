/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.core.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceController;

/**
 * @author Dimitry Polivaev
 */
public abstract class AFreeplaneAction extends AbstractAction {
	private static final long serialVersionUID = -5779325074243496610L;

	static public boolean checkEnabledOnChange(final Action action) {
		final EnabledAction annotation = action.getClass().getAnnotation(EnabledAction.class);
		if (!(action instanceof AFreeplaneAction) || annotation == null) {
			return false;
		}
		return annotation.checkOnNodeChange();
	}

	static public boolean checkEnabledOnPopup(final Action action) {
		final EnabledAction annotation = action.getClass().getAnnotation(EnabledAction.class);
		if (annotation == null) {
			return false;
		}
		return annotation.checkOnPopup();
	}

	static public boolean checkSelectionOnChange(final Action action) {
		final SelectableAction annotation = action.getClass().getAnnotation(SelectableAction.class);
		if (!(action instanceof AFreeplaneAction) || annotation == null) {
			return false;
		}
		return annotation.checkOnNodeChange();
	}

	static public boolean checkSelectionOnPopup(final Action action) {
		final SelectableAction annotation = action.getClass().getAnnotation(SelectableAction.class);
		if (annotation == null) {
			return false;
		}
		return annotation.checkOnPopup();
	}

	static public boolean checkVisibilityOnChange(final Action action) {
		final VisibleAction annotation = action.getClass().getAnnotation(VisibleAction.class);
		if (!(action instanceof AFreeplaneAction) || annotation == null) {
			return false;
		}
		return annotation.checkOnNodeChange();
	}

	static public boolean checkVisibilityOnPopup(final Action action) {
		final VisibleAction annotation = action.getClass().getAnnotation(VisibleAction.class);
		if (annotation == null) {
			return false;
		}
		return annotation.checkOnPopup();
	}

	// TODO ARCH rladstaetter 18.02.2009 actions should not have a dependency on the controller
	final private Controller controller;
	private boolean selected;
	private boolean visible;

	public AFreeplaneAction(final Controller controller) {
		super();
		selected = false;
		visible = true;
		this.controller = controller;
	}

	public AFreeplaneAction(final Controller controller, final ActionDescriptor descriptor) {
		this(controller, descriptor.name(), descriptor.iconPath());
	}

	/**
	 * @param controller
	 * @param string
	 */
	public AFreeplaneAction(final Controller controller, final String title) {
		this(controller);
		if (title != null && !title.equals("")) {
			MenuBuilder.setLabelAndMnemonic(this, ResourceController.getText(title));
		}
	}

	public AFreeplaneAction(final Controller controller, final String title, final ImageIcon icon) {
		this(controller, title);
		putValue(SMALL_ICON, icon);
	}

	/**
	 * @param title
	 *            Title is a resource.
	 * @param iconPath
	 *            is a path to an icon.
	 */
	public AFreeplaneAction(final Controller controller, final String title, final String iconPath) {
		this(controller, title);
		if (iconPath != null && !iconPath.equals("")) {
			final ImageIcon icon = new ImageIcon(ResourceController.getResourceController().getResource(iconPath));
			putValue(Action.SMALL_ICON, icon);
		}
	}

	public Controller getController() {
		return controller;
	}

	public ModeController getModeController() {
		return controller.getModeController();
	}

	public boolean isSelected() {
		return selected;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setEnabled() {
	}

	public void setSelected() {
	}

	public void setSelected(final boolean newValue) {
		final boolean oldValue = selected;
		if (oldValue != newValue) {
			selected = newValue;
			firePropertyChange("selected", Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
		}
	}

	public void setTooltip(final String tooltip) {
		final String tooltipLocalized = ResourceController.getText(tooltip);
		putValue(Action.SHORT_DESCRIPTION, tooltipLocalized);
		putValue(Action.LONG_DESCRIPTION, tooltipLocalized);
	}

	public void setVisible() {
	}

	public void setVisible(final boolean newValue) {
		final boolean oldValue = visible;
		if (oldValue != newValue) {
			visible = newValue;
			firePropertyChange("visible", Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
		}
	}
}
