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
import javax.swing.JLabel;

import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.n3.nanoxml.XMLElement;

public class NoFilteringCondition implements ISelectableCondition {
	private static NoFilteringCondition condition;
	private static String description;
	private static JComponent renderer;

	public static ISelectableCondition createCondition() {
		if (NoFilteringCondition.condition == null) {
			NoFilteringCondition.condition = new NoFilteringCondition();
		}
		return NoFilteringCondition.condition;
	}

	private NoFilteringCondition() {
		super();
	}

	public boolean checkNode(final NodeModel node) {
		return true;
	}

	public JComponent getListCellRendererComponent() {
		if (NoFilteringCondition.renderer == null) {
			NoFilteringCondition.renderer = new JLabel(toString());
		}
		return NoFilteringCondition.renderer;
	}

	@Override
	public String toString() {
		if (NoFilteringCondition.description == null) {
			NoFilteringCondition.description = ResourceBundles.getText("filter_no_filtering");
		}
		return NoFilteringCondition.description;
	}

	public void toXml(final XMLElement element) {
	}
}
