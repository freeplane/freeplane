/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Felix Natter in 2013.
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
package org.freeplane.core.resources.components;

import javax.swing.JButton;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class ButtonProperty extends PropertyBean implements IPropertyControl {
	final JButton mButton;

	/**
	 */
	public ButtonProperty(final String name, JButton button) {
		super(name);
		mButton = button;
	}

	@Override
	public String getValue() {
		return "";
	}

	public void appendToForm(final DefaultFormBuilder builder) {
		appendToForm(builder, mButton);
	}

	public void setEnabled(final boolean pEnabled) {
		mButton.setEnabled(pEnabled);
		super.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
	}

	public void setToolTipText(String text) {
		mButton.setToolTipText(text);
	}
}
