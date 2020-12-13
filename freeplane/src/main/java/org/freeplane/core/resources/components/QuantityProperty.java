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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.freeplane.api.PhysicalUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.ui.components.JComboBoxWithBorder;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class QuantityProperty<U extends Enum<U> & PhysicalUnit> extends PropertyBean implements IPropertyControl {
	final private JSpinner numberSpinner;
	@SuppressWarnings("rawtypes")
	final private JComboBox unitBox;
	final private U defaultUnit;
	private U currentUnit;

	public QuantityProperty(final String name, final double min, final double max, final double step, U defaultUnit) {
		super(name);
		this.defaultUnit = defaultUnit;
		numberSpinner = new JSpinner(new SpinnerNumberModel(min, min, max, step));
		TranslatedObject[] units = TranslatedObject.fromEnum(defaultUnit.getDeclaringClass());
		unitBox = new JComboBoxWithBorder(units);
		addChangeListeners();
	}

	private void addChangeListeners() {
		numberSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent pE) {
				firePropertyChangeEvent();
			}
		});
		unitBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					final U newUnit = getCurrentUnit();
					double value = (Double) numberSpinner.getValue();
					final Quantity<U> newQuantity = new Quantity<U>(value, currentUnit).in(newUnit);
					currentUnit = newUnit;
					if(value != newQuantity.value)
						numberSpinner.setValue(newQuantity.value);
					else
						firePropertyChangeEvent();
				}
			}
		});
	}

	@Override
	public String getValue() {
		return getQuantifiedValue().toString();
	}

	@Override
	public void layout(final DefaultFormBuilder builder) {
		Box box = new Box(BoxLayout.X_AXIS) {

			@Override
			public void setEnabled(boolean enabled) {
				QuantityProperty.this.setEnabled(enabled);
			}

		};
		box.add(numberSpinner);
		box.add(unitBox);
		layout(builder, box);
	}

	@Override
	public void setEnabled(final boolean pEnabled) {
		numberSpinner.setEnabled(pEnabled);
		unitBox.setEnabled(pEnabled);
	}

	public void setQuantifiedValue(Quantity<U> quantity){
		this.currentUnit = quantity.unit;
		numberSpinner.setValue(quantity.value);
		unitBox.setSelectedIndex(quantity.unit.ordinal());
	}

	public Quantity<U> getQuantifiedValue(){
		double value = (Double) numberSpinner.getValue();
		U unit = getCurrentUnit();
		return new Quantity<U>(value, unit);
	}

	public U getCurrentUnit() {
		return defaultUnit.getDeclaringClass().getEnumConstants()[unitBox.getSelectedIndex()];
	}

	@Override
	public void setValue(final String value)
	{
		Quantity<U> quantity = Quantity.fromString(value, defaultUnit);
		setQuantifiedValue(quantity);
	}

}
