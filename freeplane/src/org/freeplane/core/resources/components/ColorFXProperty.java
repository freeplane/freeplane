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

import javafx.scene.control.ColorPicker;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import org.freeplane.core.util.ColorFXUtils;

public class ColorFXProperty extends PropertyFXBean implements IPropertyFXControl {
	Color color;
	final private String defaultColor;
	ColorPicker chooser;

	public ColorFXProperty(final String name, final String defaultColor) {
		super(name);
		this.defaultColor = defaultColor;
		this.color = Color.BLACK;
		chooser = new ColorPicker();
	}

	public Color getColorValue() {
		return color;
	}

	@Override
	public void layout(int row, GridPane pane) {
		layout(row, pane, chooser);
		//		final JMenuItem item = new JFreeplaneMenuItem(TextUtils.getOptionalText("ColorProperty.ResetColor"));
		//		item.addActionListener(new ActionListener() {
		//			public void actionPerformed(final ActionEvent e) {
		//				setValue(defaultColor);
		//			}
		//		});
		//		menu.add(item);
		//		mButton.addMouseListener(new MouseAdapter() {
		//			@Override
		//			public void mousePressed(final MouseEvent evt) {
		//				if (evt.isPopupTrigger()) {
		//					menu.show(evt.getComponent(), evt.getX(), evt.getY());
		//				}
		//			}
		//
		//			@Override
		//			public void mouseReleased(final MouseEvent evt) {
		//				if (evt.isPopupTrigger()) {
		//					menu.show(evt.getComponent(), evt.getX(), evt.getY());
		//				}
		//			}
		//		});
	}

	@Override
	public void setEnabled(boolean pEnabled) {
		chooser.setDisable(!pEnabled);
	}

	@Override
	public String getValue() {
		return ColorFXUtils.colorToString(getColorValue());
	}

	@Override
	public void setValue(String value) {
		color = ColorFXUtils.stringToColor(value);
		if (color == null) {
			color = Color.WHITE;
		}
		chooser.setValue(color);
	}
}
