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

import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Dimitry Polivaev
 * 26.12.2008
 */
public class PropertyAdapter {
	private String tooltip;
	private String label;

	private final String name;

	public PropertyAdapter(final String name) {
		this(name, "OptionPanel." + name, "OptionPanel." + name + ".tooltip");
	}

	public PropertyAdapter(final String name, final String label, final String tooltip) {
		super();
		assert name != null;
		this.name = name;
		this.label = label;
		this.tooltip = tooltip;
	}

	public String getTooltip() {
		return tooltip;
	}

	public String getLabel() {
		return label;
	}
	
	void setLabel(String label) {
    	this.label = label;
    }

	public String getName() {
		return name;
	}
	
	protected void layout(DefaultFormBuilder builder, JComponent component){
		final String labelKey = getLabel();
		final String optionalText = TextUtils.getOptionalText(labelKey);
		final JLabel label = builder.append(optionalText, component);
		if(optionalText != null)
			TranslatedElement.TEXT.setKey(label, labelKey);
		String tooltipKey = getTooltip();
		String tooltip = TextUtils.getOptionalText(tooltipKey, null);
		if (tooltipKey != null)
			TranslatedElement.TOOLTIP.setKey(label, tooltipKey);
		label.setToolTipText(tooltip);
		component.setToolTipText(tooltip);
	}
}
