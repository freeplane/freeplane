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
package org.freeplane.view.swing.map.attribute;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollPane;

class AttributeViewScrollPane extends JScrollPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 */
	AttributeViewScrollPane(final AttributeTable attributeTable) {
		super(attributeTable);
		setAlignmentX(Component.CENTER_ALIGNMENT);
		setOpaque(false);
		getViewport().setOpaque(false);
	}
	
	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	@Override
	public Dimension getPreferredSize() {
		validate();
		return super.getPreferredSize();
	}

	@Override
	public boolean isVisible() {
		final Component view = getViewport().getView();
		return super.isVisible() && view != null && view.isVisible();
	}

	@Override
    public boolean isValidateRoot() {
	    return false;
    }
	
}
