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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

public class ComboFXProperty extends PropertyFXBean implements IPropertyFXControl {
	private final ComboBox<String> mComboBox = new ComboBox<>();
	private ArrayList<String> possibleValues;

	public ComboFXProperty(final String name, final String[] strings) {
		this(name, Arrays.asList(strings), ComboFXProperty.translate(strings));
	}

	static public Vector<String> translate(final String[] possibles) {
		final Vector<String> translatedPossibles = new Vector<String>(possibles.length);
		for (int i = 0; i < possibles.length; i++) {
			translatedPossibles.add(TextUtils.getText("OptionPanel." + possibles[i]));
		}
		return translatedPossibles;
	}

	public ComboFXProperty(final String name, final Collection<String> possibles,
	                       final Collection<String> translatedPossibles) {
		super(name);
		updateComboBoxEntries(possibles, translatedPossibles);
	}

	/**
	 * If your combo base changes, call this method to update the values. The
	 * old selected value is not selected, but the first in the list.
	 */
	public void updateComboBoxEntries(final Collection<String> possibles, final Collection<String> translatedPossibles) {
		fillPossibleValues(possibles);
		ObservableList<String> translatedPossiblesList = FXCollections.observableArrayList(translatedPossibles);
		mComboBox.setItems(translatedPossiblesList);
		if (!possibles.isEmpty()) {
			mComboBox.getSelectionModel().select(0);
		}
	}

	private void fillPossibleValues(final Collection<String> possibles) {
		possibleValues = new ArrayList<>();
		possibleValues.addAll(possibles);
	}

	@Override
	public String getValue() {
		if (mComboBox.getSelectionModel().getSelectedIndex() == -1)
			return mComboBox.getSelectionModel().getSelectedItem().toString();
		return possibleValues.get(mComboBox.getSelectionModel().getSelectedIndex());
	}

	@Override
	public void setValue(final String value) {
		if (possibleValues.contains(value)) {
			mComboBox.getSelectionModel().select(possibleValues.indexOf(value));
		}
		else if (mComboBox.isEditable()) {
			mComboBox.setValue(value);
		}
		else {
			LogUtils.severe("Can't set the value:" + value + " into the combo box " + getName() + " containing values "
			        + possibleValues);
			if (!possibleValues.isEmpty()) {
				mComboBox.getSelectionModel().select(0);
			}
		}
	}

	@Override
	public void layout(int row, GridPane formPane) {
		layout(row, formPane, mComboBox);
	}

	public ArrayList<String> getPossibleValues() {
		return possibleValues;
	}

	@Override
	public void setEnabled(final boolean pEnabled) {
		mComboBox.setDisable(!pEnabled);
	}

	public void setEditable(boolean aFlag) {
		mComboBox.setEditable(aFlag);
	}

	public boolean isEditable() {
		return mComboBox.isEditable();
	}

}
