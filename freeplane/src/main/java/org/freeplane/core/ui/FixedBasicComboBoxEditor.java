/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry Polivaev
 *
 *  This file's author is Felix Natter
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
package org.freeplane.core.ui;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 * This class fixes a problem with BasicComboBoxEditor (too few left padding
 * in the embedded JTextField => first character is half-way outside text field)
 * 
 * @author Felix Natter
 *
 */
public class FixedBasicComboBoxEditor extends BasicComboBoxEditor.UIResource {

	private static final EmptyBorder INSIDE_BORDER = new EmptyBorder(0, 4, 0, 0);

	@Override
	protected JTextField createEditorComponent() {
		JTextField editor = super.createEditorComponent();
		Border border = editor.getBorder();
		if(border != null)
			editor.setBorder(BorderFactory.createCompoundBorder(
				border, INSIDE_BORDER));
		else
			editor.setBorder(INSIDE_BORDER);
		return editor;
	}
	
}
