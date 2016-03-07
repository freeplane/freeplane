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

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.freeplane.core.util.TextUtils;

/**
 * Utility Class for displaying local object names in GUI components.
 *
 * @author Dimitry Polivaev
 */
public class TranslatedObject {
	static public TranslatedObject literal(final String literal) {
		final TranslatedObject result = new TranslatedObject();
		result.object = literal;
		result.translation = literal;
		return result;
	}

	private String translation;
	private Object object;
	private Icon icon;
	private static ListCellRenderer listCellRenderer;

	private TranslatedObject() {
	}

	public TranslatedObject(final Object object, final String translation) {
		this.object = object;
		this.translation = translation;
	}

	public TranslatedObject(final String object) {
		this.object = object;
		translation = TextUtils.getText(object);
	}

	public Object getObject() {
		return object;
	}

	public boolean objectEquals(final Object o) {
		return object.equals(o);
	}

	@Override
	public String toString() {
		return translation;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof TranslatedObject)) {
			return false;
		}
		final TranslatedObject nobj = (TranslatedObject) obj;
		return object.equals(nobj.object);
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}

	public static TranslatedObject format(final String value) {
		final int separatorPos = value.indexOf(',');
		if (separatorPos == -1) {
			return new TranslatedObject(value);
		}
		final String key = value.substring(0, separatorPos);
		final String s1 = value.substring(separatorPos + 1);
		final String text = TextUtils.format(key, s1);
		return new TranslatedObject(value, text);
	}

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public static ListCellRenderer getIconRenderer() {
		if(listCellRenderer == null)
			listCellRenderer = new ListCellRenderer() {
			private ListCellRenderer delegate = new DefaultListCellRenderer();
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			                                              boolean cellHasFocus) {
				final Object renderedValue;
				if(value instanceof TranslatedObject){
					final Icon icon = ((TranslatedObject)value).getIcon();
					if(icon != null)
						renderedValue = icon;
					else
						renderedValue = value;
				}
				else
					renderedValue = value;
				return delegate.getListCellRendererComponent(list, renderedValue, index, isSelected, cellHasFocus);
			}
		};
		return listCellRenderer;
	}

	public static TranslatedObject[] fromEnum(Class<? extends Enum<?>> enumeration) {
		return fromEnum(enumeration.getSimpleName() + "." , enumeration);
	}
	public static TranslatedObject[] fromEnum(final String prefix, Class<? extends Enum<?>> enumeration) {
		final Enum<?>[] enumConstants=enumeration.getEnumConstants();
		TranslatedObject[] objs = new TranslatedObject[enumConstants.length];
		int i = 0;
		for(Enum<?> value : enumConstants){
			objs[i++] = new TranslatedObject(value, TextUtils.getText(prefix + value.toString()));
		}
		return objs;
    }
}
