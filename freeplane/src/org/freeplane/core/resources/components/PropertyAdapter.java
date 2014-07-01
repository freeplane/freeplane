/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Dimitry Polivaev
 * 26.12.2008
 */
public class PropertyAdapter {
	private String tooltip;
	private String translationKeyLabel;
	void setLabel(String translationKeyLabel) {
    	this.translationKeyLabel = translationKeyLabel;
    }

	private final String name;

	public PropertyAdapter(final String name) {
		this(name, "OptionPanel." + name, "OptionPanel." + name + ".tooltip");
		if (ResourceController.getResourceController().getText(tooltip, null) == null) {
			tooltip = null;
		}
	}

	public PropertyAdapter(final String name, final String translatedKeyabel, final String tooltip) {
		super();
		assert name != null;
		this.name = name;
		this.translationKeyLabel = translatedKeyabel;
		this.tooltip = tooltip;
	}

	public String getTooltip() {
		return tooltip;
	}

	public String getTranslationKeyLabel() {
		return translationKeyLabel;
	}

	public String getName() {
		return name;
	}
	
	protected void layout(DefaultFormBuilder builder, JComponent component){
		final JLabel label = builder.append(TextUtils.getOptionalText(getTranslationKeyLabel()), component);
		String tooltip = TextUtils.getOptionalText(getTooltip());
		label.setToolTipText(tooltip);
		component.setToolTipText(tooltip);
	}
}
