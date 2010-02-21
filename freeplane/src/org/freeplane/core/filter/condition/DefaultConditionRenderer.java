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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.freeplane.core.icon.UIIcon;
import org.freeplane.core.resources.ResourceBundles;

/**
 * @author Dimitry Polivaev
 */
public class DefaultConditionRenderer implements ListCellRenderer {
	final public static Color SELECTED_BACKGROUND = new Color(207, 247, 202);

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean isSelected, final boolean cellHasFocus) {
		if (value == null) {
			return new JLabel(ResourceBundles.getText("filter_no_filtering"));
		}
		JComponent component;
		if (value instanceof UIIcon) {
			component = new JLabel(((UIIcon) value).getIcon());
		}
		else if (value instanceof ISelectableCondition) {
			final ISelectableCondition cond = (ISelectableCondition) value;
			component = cond.getListCellRendererComponent();
		}
		else {
			component = new JLabel(value.toString());
		}
		component.setOpaque(true);
		if (isSelected) {
			component.setBackground(DefaultConditionRenderer.SELECTED_BACKGROUND);
		}
		else {
			component.setBackground(Color.WHITE);
		}
		component.setAlignmentX(Component.LEFT_ALIGNMENT);
		//		return new JLabel(value.toString());
		return component;
	}
}
