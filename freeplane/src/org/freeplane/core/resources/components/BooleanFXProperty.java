/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 home
 *
 *  This file author is home
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

import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;

public class BooleanFXProperty extends PropertyFXBean implements IPropertyFXControl {
	CheckBox mCheckBox = new CheckBox();

	public BooleanFXProperty(final String name) {
		super(name);
	}

	@Override
	public String getValue() {
		return mCheckBox.isSelected() ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
	}

	@Override
	public void setValue(final String value) {
		final boolean booleanValue = Boolean.parseBoolean(value);
		mCheckBox.setSelected(booleanValue);
	}

	@Override
	public void layout(int row, final GridPane gridPane) {
		layout(row, gridPane, mCheckBox);
	}

	@Override
	public void setEnabled(final boolean pEnabled) {
		mCheckBox.setDisable(!pEnabled);
	}
}
