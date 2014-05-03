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

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

import org.freeplane.core.util.LogUtils;

public class FontFXProperty extends PropertyFXBean implements IPropertyFXControl {
	private final ArrayList<String> mAvailableFontFamilyNames;
	ComboBox<String> mFontComboBox = new ComboBox<>();

	public FontFXProperty(final String name) {
		super(name);
		mAvailableFontFamilyNames = new ArrayList<>(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment()
		    .getAvailableFontFamilyNames()));
		ObservableList<String> mAvailableFontFamilyNamesList = FXCollections
		    .observableArrayList(mAvailableFontFamilyNames);
		mFontComboBox.setItems(mAvailableFontFamilyNamesList);
	}

	@Override
	public String getValue() {
		return mAvailableFontFamilyNames.get(mFontComboBox.getSelectionModel().getSelectedIndex());
	}

	public void layout(int row, final GridPane pane) {
		layout(row, pane, mFontComboBox);
	}

	public void setEnabled(final boolean pEnabled) {
		mFontComboBox.setDisable(!pEnabled);
	}

	@Override
	public void setValue(final String pValue) {
		for (String fontName : mAvailableFontFamilyNames) {
			if (fontName.equals(pValue)) {
				mFontComboBox.getSelectionModel().select(fontName);
				return;
			}
		}
		LogUtils.severe("Unknown value: " + pValue);
		if (!mAvailableFontFamilyNames.isEmpty()) {
			mFontComboBox.getSelectionModel().select(0);
		}
	}
}
