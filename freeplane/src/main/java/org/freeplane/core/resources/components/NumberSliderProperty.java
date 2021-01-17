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

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class NumberSliderProperty extends PropertyBean implements IPropertyControl {

	final private JSlider transparencySlider;

	/**
	 */
	public NumberSliderProperty(final String name, final int min, final int max, final int step) {
		super(name);
	      transparencySlider = new JSlider(min, max, min);
	        transparencySlider.setMinorTickSpacing(step);
	        transparencySlider.setPaintTicks(true);
	        transparencySlider.setSnapToTicks(true);
	        transparencySlider.setPaintTrack(true);

	        transparencySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent pE) {
				firePropertyChangeEvent();
			}
		});
	}


	@Override
	public String getValue() {
		return Integer.toString(transparencySlider.getValue());
	}

	public void appendToForm(final DefaultFormBuilder builder) {
		appendToForm(builder, transparencySlider);
	}

	public void setEnabled(final boolean pEnabled) {
		transparencySlider.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
		transparencySlider.setValue(Integer.parseInt(value));
	}

	public Number getNumberValue() {
		return (Number)transparencySlider.getValue();
	}

	public void setValue(int value) {
		transparencySlider.setValue(value);
	}
}
