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
package org.freeplane.features.filter.condition;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import org.freeplane.core.ui.components.TagIcon;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.svgicons.FixedSizeUIIcon;
import org.freeplane.features.icon.Tag;
import org.freeplane.features.icon.UIIcon;

/**
 * @author Dimitry Polivaev
 */
public class DefaultConditionRenderer implements ListCellRenderer, TableCellRenderer {
	private final String noValueText;
	private final boolean renderNamedConditions;

	public DefaultConditionRenderer(String noValueText, boolean renderNamedConditions) {
	    this.noValueText = noValueText;
		this.renderNamedConditions = renderNamedConditions;
    }

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean isSelected, final boolean cellHasFocus) {
		JComponent cellRendererComponent = (JComponent) getCellRendererComponent(
		        list.getFontMetrics(list.getFont()),
		        value, isSelected);
		cellRendererComponent.setOpaque(true);
        if (isSelected) {
        	cellRendererComponent.setBackground(list.getSelectionBackground());
        	cellRendererComponent.setForeground(list.getSelectionForeground());
        }
        else {
        	cellRendererComponent.setBackground(list.getBackground());
        	cellRendererComponent.setForeground(list.getForeground());
        }
		return cellRendererComponent;
	}

	public Component getCellRendererComponent(FontMetrics fontMetrics, final Object value, final boolean isSelected) {
		final JComponent component;
		if (value == null) {
			component =  new JLabel(noValueText);
			component.setOpaque(true);
		}
        else if (value instanceof UIIcon) {
            JLabel label = new JLabel();
            Font font = fontMetrics.getFont();
            label.setFont(font);
            final int fontHeight = label.getFontMetrics(font).getHeight();
            UIIcon uiIcon = (UIIcon) value;
            Icon icon = FixedSizeUIIcon.withHeigth(uiIcon.getUrl(), fontHeight, uiIcon.hasStandardSize());
            label.setIcon(icon);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(false);
            component = label;
        }
        else if (value instanceof Tag) {
            JLabel label = new JLabel();
            Tag tag = (Tag) value;
            Icon icon = new TagIcon(tag, fontMetrics.getFont());
            label.setIcon(icon);
            label.setHorizontalAlignment(SwingConstants.LEADING);
            label.setOpaque(false);
            component = label;
        }
		else if (value instanceof ASelectableCondition) {
			final ASelectableCondition cond = (ASelectableCondition) value;
			final String userName = cond.getUserName();
			if(renderNamedConditions || userName == null)
				component = cond.getListCellRendererComponent(fontMetrics);
            else {
	            component = new JLabel(userName);
	            component.setToolTipText(cond.createDescription());
	            component.setOpaque(true);
            }
		} else {
            component = new JLabel(value.toString());
            component.setOpaque(true);
        }
		component.setAlignmentX(Component.LEFT_ALIGNMENT);
		return component;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
		Component cellRendererComponent = getCellRendererComponent(
		        table.getFontMetrics(table.getFont()),
		        value, isSelected);
	       if (isSelected) {
	    	   cellRendererComponent.setBackground(table.getSelectionBackground());
	    	   cellRendererComponent.setForeground(table.getSelectionForeground());
	        }
	        else {
	        	cellRendererComponent.setBackground(table.getBackground());
	        	cellRendererComponent.setForeground(table.getForeground());
	        }
		return cellRendererComponent;
    }
}
