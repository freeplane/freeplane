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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class EditableComboProperty extends PropertyBean implements IPropertyControl {
	final JComboBox comboBox;
	private String selected;

	public EditableComboProperty(final String name, List<String> values) {
		super(name);
		comboBox = createFormatChooser(values);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent pE) {
				firePropertyChangeEvent();
			}
		});
	}

	private JComboBox createFormatChooser(final List<String> values) {
    	final JComboBox formatChooser = new JComboBox(new Vector<String>(values));
    	formatChooser.setEditable(true);
    	formatChooser.addItemListener(new ItemListener() {
    		public void itemStateChanged(final ItemEvent e) {
    			selected = (String) e.getItem();
    		}
    	});
    	return formatChooser;
    }

	@Override
	public String getValue() {
		return selected;
	}

	public void layout(final DefaultFormBuilder builder) {
		layout(builder, comboBox);
	}

	public void setEnabled(final boolean pEnabled) {
		comboBox.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
		comboBox.setSelectedItem(value);
	}

	public void setToolTipText(String text) {
	    comboBox.setToolTipText(text);
    }

	@Override
    protected Component[] getComponents() {
	    return comboBox.getComponents();
    }
}
