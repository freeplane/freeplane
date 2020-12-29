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

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class NumberProperty extends PropertyBean implements IPropertyControl {
//	final private int max;
//	final private int min;
//	final private int step;
	final private JSpinner spinner;
	final private boolean isDoubleProperty;

	/**
	 */
	public NumberProperty(final String name, final int min, final int max, final int step) {
		super(name);
//		this.min = min;
//		this.max = max;
//		this.step = step;
		spinner = new JSpinner(new SpinnerNumberModel(min, min, max, step));
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent pE) {
				firePropertyChangeEvent();
			}
		});
		isDoubleProperty = false;
	}

	public NumberProperty(final String name, final double min, final double max, final double step) {
		super(name);
		spinner = new JSpinner(new SpinnerNumberModel(min, min, max, step));
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent pE) {
				firePropertyChangeEvent();
			}
		});
		isDoubleProperty = true;
	}

	@Override
	public String getValue() {
		return spinner.getValue().toString();
	}

	public void appendToForm(final DefaultFormBuilder builder) {
		appendToForm(builder, spinner);
	}

	public void setEnabled(final boolean pEnabled) {
		spinner.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
		Number someValue;
		if (isDoubleProperty)
		{
			try {
				someValue = Double.parseDouble(value);
			}
			catch (final NumberFormatException e) {
				someValue = 1.0;
			}
		}
		else
		{
			try {
				someValue = Integer.parseInt(value);
			}
			catch (final NumberFormatException e) {
				someValue = 100;
			}
		}
		spinner.setValue(someValue);
	}

	public Number getNumberValue() {
		return (Number)spinner.getValue();
	}

	public void setValue(double value) {
		spinner.setValue(value);
	}

	public void setValue(int value) {
		spinner.setValue(value);
	}
}
