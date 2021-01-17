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

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class SeparatorProperty implements IPropertyControl {
	private final String label;

	public SeparatorProperty(final String label) {
		super();
		this.label = label;
	}

	@Override
	public String getTooltip() {
		return null;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void appendToForm(final DefaultFormBuilder builder) {
		final String labelKey = getLabel();
		final String text = TextUtils.getOptionalText(labelKey);
		if (builder.getColumn() > 1)
			builder.nextLine();
		final JComponent separator = builder.appendSeparator(text);
		if(text != null) {
			for (Component child : separator.getComponents()) {
				if(child instanceof JLabel)
					TranslatedElement.TEXT.setKey((JComponent) child, labelKey);
				break;
			}
		}
	}

	@Override
	public void setEnabled(final boolean pEnabled) {
	}
}
