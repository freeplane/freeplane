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

import javax.swing.Icon;

/**
 * @author Dimitry Polivaev
 * Mar 17, 2011
 */
public class ObjectAndIcon {
	final Object object;
	final Icon icon;
	public ObjectAndIcon(Object object, Icon icon) {
	    super();
	    this.object = object;
	    this.icon = icon;
    }
	@Override
    public String toString() {
	    return object.toString();
    }
	public Object getObject() {
    	return object;
    }
	public Icon getIcon() {
    	return icon;
    }
}
