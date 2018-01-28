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
import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;

/**
 * @author Dimitry Polivaev
 */
public abstract class AFreeplaneAction extends AbstractAction implements IFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public boolean checkEnabledOnChange() {
		final EnabledAction annotation = getClass().getAnnotation(EnabledAction.class);
		if (annotation == null) {
			return false;
		}
		return annotation.checkOnNodeChange();
	}

	public boolean checkSelectionOnChange() {
		final SelectableAction annotation = getClass().getAnnotation(SelectableAction.class);
		if (annotation == null) {
			return false;
		}
		return annotation.checkOnNodeChange();
	}

	public boolean checkSelectionOnPropertyChange() {
		final SelectableAction annotation = getClass().getAnnotation(SelectableAction.class);
		if (annotation == null) {
			return false;
		}
		return !"".equals(annotation.checkOnPropertyChange());
	}

	public boolean checkSelectionOnPopup() {
		final SelectableAction annotation = getClass().getAnnotation(SelectableAction.class);
		if (annotation == null) {
			return false;
		}
		return annotation.checkOnPopup();
	}

	final private String key;
	private boolean selected = false;
	final private String rawText;

	public AFreeplaneAction(final String key) {
		super();
		this.key = key;
		rawText = TextUtils.getRawText(getTextKey());
		LabelAndMnemonicSetter.setLabelAndMnemonic(this, rawText);
		setIcon(getIconKey());
		setTooltip(getTooltipKey());
		//		System.out.println(key);
	}

	protected void setIcon(final String iconKey) {
		Icon icon = ResourceController.getResourceController().getIcon(iconKey);
		setIcon(icon);
	}

	protected void setIcon(Icon icon) {
		putValue(SMALL_ICON, icon);
	}

	protected void setTooltip(String tooltipKey) {
		final String tooltip = TextUtils.getRawText(tooltipKey, null);
		if (tooltip != null && !"".equals(tooltip)) {
			putValue(Action.SHORT_DESCRIPTION, tooltip);
			putValue(Action.LONG_DESCRIPTION, tooltip);
		}
	}

	//	/**
	//	 * @param controller
	//	 * @param string
	//	 */
	//	private AFreeplaneAction( final String titleKey) {
	//		this();
	//	}
	//
	public AFreeplaneAction(final String key, final String title, final Icon icon) {
		//		this.controller = controller;
		putValue(SMALL_ICON, icon);
		if (title != null && !title.equals("")) {
			LabelAndMnemonicSetter.setLabelAndMnemonic(this, title);
		}
		this.rawText = title;
		this.key = key;
	}

	@Override
	public void afterMapChange(final Object newMap) {
		if (newMap == null) {
			if (super.isEnabled()) {
				setEnabled(false);
			}
		}
		else {
			if (!super.isEnabled()) {
				setEnabled(true);
			}
			setEnabled();
		}
	}

	@Override
	public final String getIconKey() {
		return key + ".icon";
	}

	public String getKey() {
		return key;
	}

	public String getTextKey() {
		return key + ".text";
	}

	public String getTooltipKey() {
		return key + ".tooltip";
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	public void setEnabled() {
	}

	public void setSelected() {
	}

	@Override
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

	public boolean isSelectable() {
		return getClass().getAnnotation(SelectableAction.class) != null;
	}

	public String getRawText() {
		return rawText;
	}
}
