/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.core.ui.components;

import java.awt.Component;
import java.net.URI;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.icon.factory.ImageIconFactory;

/**
 * @author Dimitry Polivaev
 * Mar 16, 2011
 */
public class TypedListCellRenderer extends DefaultListCellRenderer{

	final private static TypedListCellRenderer instance = new TypedListCellRenderer();
	static TypedListCellRenderer getInstance() {
    	return instance;
    }

	public TypedListCellRenderer() {
	    super();
    }

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	private static Icon textIcon;
	private static Icon numberIcon;
	private static Icon dateIcon;
	private static Icon dateTimeIcon;
	private static Icon linkIcon;
	
	static {
		final ResourceController resourceController = ResourceController.getResourceController();
		textIcon = resourceController.getIcon("text_icon");
		numberIcon = resourceController.getIcon("number_icon");
		dateIcon = resourceController.getIcon("date_icon");
		dateTimeIcon = resourceController.getIcon("date_time_icon");
		linkIcon = resourceController.getIcon("link_icon");
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
	                                              boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		final Icon icon;
		if (value instanceof String) {
			icon = textIcon;
		}
		else if (value instanceof FormattedDate) {
			final FormattedDate fd = (FormattedDate) value;
			if (fd.containsTime())
				icon = dateTimeIcon;
			else
				icon = dateIcon;
		}
		else if (value instanceof URI) {
			icon = linkIcon;
		}
		else if (value instanceof Number) {
			icon = numberIcon;
		}
		else if (value instanceof ObjectAndIcon) {
			icon = ((ObjectAndIcon) value).getIcon();
		}
		else
			icon = null;
		final ImageIconFactory iconFactory = ImageIconFactory.getInstance();
		if(icon != null && iconFactory.canScaleIcon(icon)){
			final int fontSize = getFont().getSize();
			setIcon(iconFactory.getScaledIcon(icon, new Quantity<LengthUnits>(fontSize, LengthUnits.px)));
		}
		else
			setIcon(icon);
		return this;
	}
	
}
