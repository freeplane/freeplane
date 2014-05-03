/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 home
 *
 *  This file author is home
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

import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

public class PropertyFXAdapter {
	private String description;
	private String label;

	void setLabel(String label) {
		this.label = label;
	}

	private final String name;

	public PropertyFXAdapter(final String name) {
		this(name, "OptionPanel." + name, "OptionPanel." + name + ".tooltip");
		//		if (ResourceController.getResourceController().getText(description, null) == null) {
		//			description = null;
		//		}
	}

	public PropertyFXAdapter(final String name, final String label, final String description) {
		super();
		assert name != null;
		this.name = name;
		this.label = label;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getLabel() {
		return label;
	}

	public String getName() {
		return name;
	}

	protected void layout(int row, GridPane formPane, Control component) {
		Label label = createLabel();
		handleToolTips(component, label);
		formPane.add(label, 0, row);
		formPane.add(component, 1, row);
	}

	private Label createLabel() {
		//	    String labelText = TextUtils.getOptionalText(getLabel());
		String labelText = getLabel();
		Label label = new Label(labelText);
		return label;
	}

	private void handleToolTips(Control component, Label label) {
		//	    String tooltipText = TextUtils.getOptionalText(getDescription());
		String tooltipText = getDescription();
		Tooltip tooltip = new Tooltip(tooltipText);
		label.setTooltip(tooltip);
		component.setTooltip(tooltip);
	}
}
