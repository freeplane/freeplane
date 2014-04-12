/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.mode;

public interface IPropertyHandler<V, M> {
	final static public Integer DEFAULT = 1 << 9;
	final static public Integer DEFAULT_STYLE = 1 << 8;
	final static public Integer STYLE = 1 << 7;
	final static public Integer AUTO = 1 << 6;
	final static public Integer NODE = 1 << 5;
	final static public Integer MODIFICATION = 1 << 4;

	V getProperty(M model, V currentValue);
}
