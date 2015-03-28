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
package org.freeplane.features.attribute;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.SelectableAction;

@SelectableAction(checkOnPopup = true)
class ShowAllAttributesAction extends AttributeViewTypeAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public ShowAllAttributesAction() {
		super("ShowAllAttributesAction");
	};

	public void actionPerformed(final ActionEvent e) {
		setAttributeViewType(AttributeTableLayoutModel.SHOW_ALL);
	}

	@Override
	public void setSelected() {
		setSelected(AttributeTableLayoutModel.SHOW_ALL.equals(getAttributeViewType()));
	}
}
