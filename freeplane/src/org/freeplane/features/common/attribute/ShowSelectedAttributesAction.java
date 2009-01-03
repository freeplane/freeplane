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
package org.freeplane.features.common.attribute;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.MapModel;
import org.freeplane.core.ui.MenuBuilder;

class ShowSelectedAttributesAction extends AbstractAction {
	/**
	 *
	 */
	public ShowSelectedAttributesAction() {
		MenuBuilder.setLabelAndMnemonic(this, Controller.getText("attributes_show_selected"));
	};

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Controller.getController().getMap();
		setAttributeViewType(map);
	}

	void setAttributeViewType(final MapModel map) {
		final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
		if (attributes.getAttributeViewType() != AttributeTableLayoutModel.SHOW_SELECTED) {
			attributes.setAttributeViewType(AttributeTableLayoutModel.SHOW_SELECTED);
		}
	}
}
