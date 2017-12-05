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
package org.freeplane.features.map;

import java.awt.AWTEvent;

/**
 * @author Dimitry Polivaev 27.11.2008
 */
public class MapChangeEvent extends AWTEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private MapModel map;
	final private Object newValue;
	final private Object oldValue;
	final private Object property;

	public MapChangeEvent(final Object source, final MapModel map, final Object property, final Object oldValue,
	                      final Object newValue) {
		super(source, 0);
		this.map = map;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.property = property;
	}

	public MapModel getMap() {
		return map;
	}

	public Object getNewValue() {
		return newValue;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getProperty() {
		return property;
	}
}
