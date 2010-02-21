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
package org.freeplane.core.filter.condition;

import javax.swing.JComponent;

import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public abstract class NodeCondition implements ISelectableCondition {
	private String description;
	private JComponent renderer;

	protected NodeCondition() {
	}

	public void attributesToXml(final XMLElement child) {
	}

	abstract protected String createDesctiption();

	public JComponent getListCellRendererComponent() {
		if (renderer == null) {
			renderer = ConditionFactory.createCellRendererComponent(toString());
		}
		return renderer;
	}

	protected void setListCellRendererComponent(final JComponent renderer) {
		this.renderer = renderer;
	}

	@Override
	public String toString() {
		if (description == null) {
			description = createDesctiption();
		}
		return description;
	}
}
