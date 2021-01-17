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
package org.freeplane.core.resources.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class StringProperty extends PropertyBean implements IPropertyControl {
	final JTextField mTextField;

	/**
	 */
	public StringProperty(final String name) {
		super(name);
		mTextField = new JTextField();
		mTextField.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent pE) {
				firePropertyChangeEvent();
			}
		});
	}

	@Override
	public String getValue() {
		return mTextField.getText();
	}

	public void appendToForm(final DefaultFormBuilder builder) {
		appendToForm(builder, mTextField);
	}

	public void setEnabled(final boolean pEnabled) {
		mTextField.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
		mTextField.setText(value);
		mTextField.selectAll();
	}
}
