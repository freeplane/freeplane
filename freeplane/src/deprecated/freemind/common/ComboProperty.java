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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.jgoodies.forms.builder.DefaultFormBuilder;

import deprecated.freemind.preferences.layout.OptionString;

public class ComboProperty extends PropertyBean implements IPropertyControl {
	String description;
	String label;
	JComboBox mComboBox = new JComboBox();
	private Vector possibleValues;

	public ComboProperty(final String description, final String label,
	                     final List possibles, final List possibleTranslations) {
		this.description = description;
		this.label = label;
		fillPossibleValues(possibles);
		mComboBox.setModel(new DefaultComboBoxModel(new Vector(
		    possibleTranslations)));
	}

	/**
	 * @param pTranslator
	 */
	public ComboProperty(final String description, final String label,
	                     final String[] possibles) {
		super();
		this.description = description;
		this.label = label;
		fillPossibleValues(possibles);
		final Vector possibleTranslations = new Vector();
		for (final Iterator i = possibleValues.iterator(); i.hasNext();) {
			final String key = (String) i.next();
			possibleTranslations.add(OptionString.getText(key));
		}
		mComboBox.setModel(new DefaultComboBoxModel(possibleTranslations));
		mComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent pE) {
				firePropertyChangeEvent();
			}
		});
	}

	public ComboProperty(final String description, final String label,
	                     final String[] possibles,
	                     final List possibleTranslations) {
		this.description = description;
		this.label = label;
		fillPossibleValues(possibles);
		mComboBox.setModel(new DefaultComboBoxModel(new Vector(
		    possibleTranslations)));
	}

	/**
	 */
	private void fillPossibleValues(final List possibles) {
		possibleValues = new Vector();
		possibleValues.addAll(possibles);
	}

	/**
	 */
	private void fillPossibleValues(final String[] possibles) {
		fillPossibleValues(Arrays.asList(possibles));
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
		return (String) possibleValues.get(mComboBox.getSelectedIndex());
	}

	public void layout(final DefaultFormBuilder builder) {
		final JLabel label = builder.append(OptionString.getText(getLabel()),
		    mComboBox);
		label.setToolTipText(OptionString.getText(getDescription()));
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
			System.err.println("Can't set the value:" + value
			        + " into the combo box " + getLabel() + "/"
			        + getDescription());
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
	public void updateComboBoxEntries(final List possibles,
	                                  final List possibleTranslations) {
		mComboBox.setModel(new DefaultComboBoxModel(new Vector(
		    possibleTranslations)));
		fillPossibleValues(possibles);
		if (possibles.size() > 0) {
			mComboBox.setSelectedIndex(0);
		}
	}
}
