/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Felix Natter in 2013.
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

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import com.jgoodies.forms.builder.DefaultFormBuilder;

public class TextBoxProperty extends PropertyBean implements IPropertyControl {
	final JTextArea mTextArea;

	/**
	 */
	public TextBoxProperty(final String name, final int lines) {
		super(name);
		mTextArea = new JTextArea(lines, 70);
//		mTextArea.setBorder(BorderFactory.createLineBorder(Color.black));
		mTextArea.setLineWrap(true);
	}

	@Override
	public String getValue() {
		return mTextArea.getText();
	}

	public void appendToForm(final DefaultFormBuilder builder) {
		JScrollPane scrollPane = new JScrollPane(mTextArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		appendToForm(builder, scrollPane);
	}

	public void setEnabled(final boolean pEnabled) {
		mTextArea.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
		mTextArea.setText(value);
		mTextArea.selectAll();
	}
}
