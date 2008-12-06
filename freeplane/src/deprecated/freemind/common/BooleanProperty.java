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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import com.jgoodies.forms.builder.DefaultFormBuilder;

import deprecated.freemind.preferences.layout.OptionString;

public class BooleanProperty extends PropertyBean implements IPropertyControl {
	static public final String FALSE_VALUE = "false";
	static public final String TRUE_VALUE = "true";
	String description;
	String label;
	JCheckBox mCheckBox = new JCheckBox();
	protected String mFalseValue = BooleanProperty.FALSE_VALUE;
	protected String mTrueValue = BooleanProperty.TRUE_VALUE;

	/**
	 */
	public BooleanProperty(final String description, final String label) {
		super();
		this.description = description;
		this.label = label;
		mCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent pE) {
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
		return mCheckBox.isSelected() ? mTrueValue : mFalseValue;
	}

	public void layout(final DefaultFormBuilder builder) {
		final JLabel label = builder.append(OptionString.getText(getLabel()),
		    mCheckBox);
		label.setToolTipText(OptionString.getText(getDescription()));
	}

	public void setEnabled(final boolean pEnabled) {
		mCheckBox.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
		if (value == null
		        || !(value.toLowerCase().equals(mTrueValue) || value
		            .toLowerCase().equals(mFalseValue))) {
			throw new IllegalArgumentException("Cannot set a boolean to "
			        + value);
		}
		mCheckBox.setSelected(value.toLowerCase().equals(mTrueValue));
	}
}
