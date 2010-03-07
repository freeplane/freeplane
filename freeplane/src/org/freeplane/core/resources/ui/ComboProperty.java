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
package org.freeplane.core.resources.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.util.LogTool;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class ComboProperty extends PropertyBean implements IPropertyControl {
	static public Vector<String> translate(final String[] possibles) {
		final Vector<String> possibleTranslations = new Vector<String>(possibles.length);
		for (int i = 0; i < possibles.length; i++) {
			possibleTranslations.add(ResourceBundles.getText("OptionPanel." + possibles[i]));
		}
		return possibleTranslations;
	}

	JComboBox mComboBox = new JComboBox();
	private Vector<String> possibleValues;

	public ComboProperty(final String name, final Collection<String> possibles, final Collection<String> possibleTranslations) {
		super(name);
		fillPossibleValues(possibles);
		mComboBox.setModel(new DefaultComboBoxModel(new Vector<String>(possibleTranslations)));
	}

	public ComboProperty(final String name, final String[] strings) {
		this(name, Arrays.asList(strings), ComboProperty.translate(strings));
	}

	/**
	 */
	private void fillPossibleValues(final Collection<String> possibles) {
		possibleValues = new Vector<String>();
		possibleValues.addAll(possibles);
	}

	@Override
	public String getValue() {
		return (String) possibleValues.get(mComboBox.getSelectedIndex());
	}

	public void layout(final DefaultFormBuilder builder) {
		final JLabel label = builder.append(FpStringUtils.getOptionalText(getLabel()), mComboBox);
		label.setToolTipText(FpStringUtils.getOptionalText(getDescription()));
	}

	public void setEnabled(final boolean pEnabled) {
		mComboBox.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
		if (possibleValues.contains(value)) {
			mComboBox.setSelectedIndex(possibleValues.indexOf(value));
		}
		else {
			LogTool.severe("Can't set the value:" + value + " into the combo box " + getName() + "/" + getLabel());
			if (mComboBox.getModel().getSize() > 0) {
				mComboBox.setSelectedIndex(0);
			}
		}
	}

	/**
	 * If your combo base changes, call this method to update the values. The
	 * old selected value is not selected, but the first in the list. Thus, you
	 * should call this method only shortly before setting the value with
	 * setValue.
	 */
	public void updateComboBoxEntries(final List<String> possibles, final List<String> possibleTranslations) {
		mComboBox.setModel(new DefaultComboBoxModel(new Vector<String>(possibleTranslations)));
		fillPossibleValues(possibles);
		if (possibles.size() > 0) {
			mComboBox.setSelectedIndex(0);
		}
	}
}
