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
package org.freeplane.core.frame;

import java.awt.Component;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.view.swing.map.MapView;

/**
 * @author Dimitry Polivaev
 * 10.01.2009
 */
class MapViewScrollPane extends JScrollPane {
	private static final String SCROLLBAR_INCREMENT = "scrollbar_increment";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MapViewScrollPane() {
		super();
		final int scrollbarIncrement = ResourceController.getResourceController().getIntProperty(SCROLLBAR_INCREMENT, 1);
		getHorizontalScrollBar().setUnitIncrement(scrollbarIncrement);
		getVerticalScrollBar().setUnitIncrement(scrollbarIncrement);
		ResourceController.getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
			
			public void propertyChanged(String propertyName, String newValue,
					String oldValue) {
				if(! propertyName.equals(SCROLLBAR_INCREMENT)){
					return;
				}
				final int scrollbarIncrement = Integer.valueOf(newValue);
				getHorizontalScrollBar().setUnitIncrement(scrollbarIncrement);
				getVerticalScrollBar().setUnitIncrement(scrollbarIncrement);
			}
		});
	}

	@Override
	protected void validateTree() {
		final Component view = getViewport().getView();
		if (view != null) {
			view.validate();
		}
		super.validateTree();
	}
}
