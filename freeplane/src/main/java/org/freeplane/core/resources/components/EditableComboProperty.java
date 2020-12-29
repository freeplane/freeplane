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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;

import org.freeplane.core.ui.components.JComboBoxWithBorder;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public abstract class EditableComboProperty<T> extends PropertyBean implements IPropertyControl {
	final JComboBox comboBox;
	private T selected;

	public EditableComboProperty(final String name, final List<? extends T> values) {
		super(name);
		comboBox = createFormatChooser(values);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent pE) {
				firePropertyChangeEvent();
			}
		});
	}

    private JComboBox createFormatChooser(final List<? extends T> list) {
    	final JComboBox formatChooser = new JComboBoxWithBorder(new Vector<T>(list));
    	formatChooser.setEditable(true);
    	formatChooser.addItemListener(new ItemListener() {
    		public void itemStateChanged(final ItemEvent e) {
    			final T valueObject = toValueObject(e.getItem());
    			if (valueObject != null)
    			    selected = valueObject;
    		}
    	});
    	return formatChooser;
    }

	@Override
	public String getValue() {
		return selected == null ? null : selected.toString();
	}

    public T getSelected() {
        return selected;
    }

	public void appendToForm(final DefaultFormBuilder builder) {
		appendToForm(builder, comboBox);
	}

	public void setEnabled(final boolean pEnabled) {
		comboBox.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
		comboBox.setSelectedItem(value == null ? null : toValueObject(value));
	}

    public void setToolTipText(String text) {
	    comboBox.setToolTipText(text);
    }

    abstract public T toValueObject(Object value);
}
