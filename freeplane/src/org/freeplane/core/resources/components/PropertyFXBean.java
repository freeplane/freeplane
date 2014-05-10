/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 home
 *
 *  This file author is home
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
package org.freeplane.core.resources.components;


public abstract class PropertyFXBean extends PropertyFXAdapter {
	public PropertyFXBean(final String name) {
		super(name);
	}

	public PropertyFXBean(final String name, final String label, final String description) {
		super(name, label, description);
	}

	public abstract String getValue();

	public abstract void setValue(String value);

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + getName() + "->" + getValue() + ")";
	}
}
