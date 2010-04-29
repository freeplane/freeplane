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

import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogTool;

/**
 * @author Dimitry Polivaev
 */
public abstract class AFreeplaneAction extends AbstractAction implements IFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static public boolean checkEnabledOnChange(final AFreeplaneAction action) {
		final EnabledAction annotation = action.getClass().getAnnotation(EnabledAction.class);
		if (annotation == null) {
			return false;
		}
		return annotation.checkOnNodeChange();
	}

	static public boolean checkEnabledOnPopup(final AFreeplaneAction action) {
		final EnabledAction annotation = action.getClass().getAnnotation(EnabledAction.class);
		if (annotation == null) {
			return false;
		}
		return annotation.checkOnPopup();
	}

	static public boolean checkSelectionOnChange(final AFreeplaneAction action) {
		final SelectableAction annotation = action.getClass().getAnnotation(SelectableAction.class);
		if (annotation == null) {
			return false;
		}
		return annotation.checkOnNodeChange();
	}

	static public boolean checkSelectionOnPropertyChange(final AFreeplaneAction action) {
		final SelectableAction annotation = action.getClass().getAnnotation(SelectableAction.class);
		if (annotation == null) {
			return false;
		}
		return !"".equals(annotation.checkOnPropertyChange());
	}

	static public boolean checkSelectionOnPopup(final AFreeplaneAction action) {
		final SelectableAction annotation = action.getClass().getAnnotation(SelectableAction.class);
		if (annotation == null) {
			return false;
		}
		return annotation.checkOnPopup();
	}

	// TODO ARCH rladstaetter 18.02.2009 actions should not have a dependency on the controller
	final private Controller controller;
	final private String key;
	private boolean selected = false;

	public AFreeplaneAction(final String key, final Controller controller) {
		super();
		this.controller = controller;
		this.key = key;
		MenuBuilder.setLabelAndMnemonic(this, ResourceBundles.getText(getTextKey()));
		final String iconResource = ResourceController.getResourceController().getProperty(getIconKey(), null);
		if (iconResource != null) {
			final URL url = ResourceController.getResourceController().getResource(iconResource);
			if (url == null) {
				LogTool.severe("can not load icon '" + iconResource + "'");
			}
			else {
				final ImageIcon icon = new ImageIcon(url);
				putValue(SMALL_ICON, icon);
			}
		}
		final String tooltip = ResourceBundles.getText(getTooltipKey(), null);
		if (tooltip != null && !"".equals(tooltip)) {
			putValue(Action.SHORT_DESCRIPTION, tooltip);
			putValue(Action.LONG_DESCRIPTION, tooltip);
		}
		//		System.out.println(key);
	}

	//	/**
	//	 * @param controller
	//	 * @param string
	//	 */
	//	private AFreeplaneAction(final Controller controller, final String titleKey) {
	//		this(controller);
	//	}
	//
	public AFreeplaneAction(final String key, final Controller controller, final String title, final ImageIcon icon) {
		this.controller = controller;
		putValue(SMALL_ICON, icon);
		if (title != null && !title.equals("")) {
			MenuBuilder.setLabelAndMnemonic(this, title);
		}
		this.key = key;
	}

	public void afterMapChange(final Object newMap) {
		if (newMap == null) {
			if (isEnabled()) {
				setEnabled(false);
			}
		}
		else {
			if (!isEnabled()) {
				setEnabled(true);
			}
		}
	}

	public Controller getController() {
		return controller;
	}

	final String getIconKey() {
		return key + ".icon";
	}

	public String getKey() {
		return key;
	}

	public ModeController getModeController() {
		return controller.getModeController();
	}

	final String getTextKey() {
		return key + ".text";
	}

	final String getTooltipKey() {
		return key + ".tooltip";
	}

	public boolean isSelected() {
		return selected;
	}

	public void setEnabled() {
		setEnabled(true);
	}

	public void setSelected() {
	}

	public void setSelected(final boolean newValue) {
		final boolean oldValue = selected;
		if (oldValue != newValue) {
			selected = newValue;
			firePropertyChange(SelectableAction.SELECTION_PROPERTY, Boolean.valueOf(oldValue), Boolean
			    .valueOf(newValue));
		}
	}

	public void setVisible() {
	}
}
