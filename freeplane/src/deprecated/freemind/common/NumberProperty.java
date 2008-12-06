/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package deprecated.freemind.common;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;

import deprecated.freemind.preferences.layout.OptionString;

public class NumberProperty extends PropertyBean implements IPropertyControl {
	String description;
	String label;
	final private int max;
	final private int min;
	final private JSpinner spinner;
	final private int step;

	/**
	 */
	public NumberProperty(final String description, final String label,
	                      final int min, final int max, final int step) {
		this.min = min;
		this.max = max;
		this.step = step;
		spinner = new JSpinner(new SpinnerNumberModel(min, min, max, step));
		this.description = description;
		this.label = label;
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent pE) {
				firePropertyChangeEvent();
			}
		});
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getValue() {
		return spinner.getValue().toString();
	}

	public void layout(final DefaultFormBuilder builder) {
		final JLabel label = builder.append(OptionString.getText(getLabel()),
		    spinner);
		label.setToolTipText(OptionString.getText(getDescription()));
	}

	public void setEnabled(final boolean pEnabled) {
		spinner.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
		int intValue = 100;
		try {
			intValue = Integer.parseInt(value);
			final int stepModul = (intValue - min) % step;
			if (intValue < min || intValue > max || (stepModul != 0)) {
				System.err.println("Actual value of property " + getLabel()
				        + " is not in the allowed range: " + value);
				intValue = min;
			}
		}
		catch (final NumberFormatException e) {
			org.freeplane.main.Tools.logException(e);
		}
		spinner.setValue(new Integer(intValue));
	}
}
