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

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.util.LogUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class FontProperty extends PropertyBean implements IPropertyControl {
	final private String[] mAvailableFontFamilyNames;
	JComboBox mFontComboBox = new JComboBoxWithBorder();

	/**
	 */
	public FontProperty(final String name) {
		super(name);
		mAvailableFontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		mFontComboBox.setModel(new DefaultComboBoxModel(mAvailableFontFamilyNames));
		mFontComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent pE) {
				firePropertyChangeEvent();
			}
		});
	}

	@Override
	public String getValue() {
		return mAvailableFontFamilyNames[mFontComboBox.getSelectedIndex()];
	}

	public void appendToForm(final DefaultFormBuilder builder) {
		appendToForm(builder, mFontComboBox);
	}

	public void setEnabled(final boolean pEnabled) {
		mFontComboBox.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String pValue) {
		for (int i = 0; i < mAvailableFontFamilyNames.length; i++) {
			final String fontName = mAvailableFontFamilyNames[i];
			if (fontName.equals(pValue)) {
				mFontComboBox.setSelectedIndex(i);
				return;
			}
		}
		LogUtils.severe("Unknown value:" + pValue);
		if (mFontComboBox.getModel().getSize() > 0) {
			mFontComboBox.setSelectedIndex(0);
		}
	}
}
