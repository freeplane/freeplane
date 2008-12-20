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

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.jgoodies.forms.builder.DefaultFormBuilder;

import deprecated.freemind.preferences.layout.OptionString;

public class FontProperty extends PropertyBean implements IPropertyControl {
	String description;
	Font font = null;
	String label;
	final private String[] mAvailableFontFamilyNames;
	JComboBox mFontComboBox = new JComboBox();

	/**
	 */
	public FontProperty(final String description, final String label) {
		super();
		this.description = description;
		this.label = label;
		mAvailableFontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment()
		    .getAvailableFontFamilyNames();
		mFontComboBox.setModel(new DefaultComboBoxModel(mAvailableFontFamilyNames));
		mFontComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent pE) {
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
		return mAvailableFontFamilyNames[mFontComboBox.getSelectedIndex()];
	}

	public void layout(final DefaultFormBuilder builder) {
		final JLabel label = builder.append(OptionString.getText(getLabel()), mFontComboBox);
		label.setToolTipText(OptionString.getText(getDescription()));
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
		System.err.println("Unknown value:" + pValue);
		if (mFontComboBox.getModel().getSize() > 0) {
			mFontComboBox.setSelectedIndex(0);
		}
	}
}
