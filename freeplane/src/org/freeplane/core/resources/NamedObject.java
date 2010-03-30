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
package org.freeplane.core.resources;

import org.freeplane.core.util.TextUtil;

/**
 * Utility Class for displaying local object names in GUI components.
 *
 * @author Dimitry Polivaev
 */
public class NamedObject {
	static public NamedObject literal(final String literal) {
		final NamedObject result = new NamedObject();
		result.object = literal;
		result.name = literal;
		return result;
	}

	private String name;
	private Object object;

	private NamedObject() {
	}

	public NamedObject(final Object object, final String name) {
		this.object = object;
		this.name = name;
	}

	public NamedObject(String object) {
		this.object = object;
		this.name = TextUtil.getText(object);
    }

	public Object getObject() {
		return object;
	}

	public boolean objectEquals(final Object o) {
		return getObject().equals(o);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
    public boolean equals(Object obj) {
		if(! (obj instanceof NamedObject)){
			return false;
		}
		NamedObject nobj = (NamedObject) obj;
		return object.equals(nobj.object) && name.equals(nobj.name);
    }

	@Override
    public int hashCode() {
	    return 37 * object.hashCode() + name.hashCode();
    }
	
	static public String toKeyString(Object obj){
		if(obj instanceof NamedObject){
			return ((NamedObject)obj).getObject().toString();
		}
		return obj.toString();
	}

	public static NamedObject formatText(String value) {
		final int separatorPos = value.indexOf(',');
		if(separatorPos == -1){
			return new NamedObject(value);
		}
		String key = value.substring(0, separatorPos);
		String s1 = value.substring(separatorPos+1);
		final String text = TextUtil.formatText(key, s1);
		return new NamedObject(value, text);
    }
}
