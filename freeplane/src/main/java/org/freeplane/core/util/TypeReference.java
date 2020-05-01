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
package org.freeplane.core.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.FormattedNumber;


/**
 * @author Dimitry Polivaev
 * Mar 2, 2011
 */
public class TypeReference{

	static public Object create(final String objSpec) {
		return create(objSpec, true);
	}

	static public Object create(final String objSpec, final boolean verbose) {
		try {
			final int sep = objSpec.indexOf('|');
			if (sep == -1)
				return objSpec;
			final String type = objSpec.substring(0, sep);
			final String spec = objSpec.substring(sep + 1);
			if(String.class.getName().equals(type))
				return spec;
			final AccessibleObject factory = getFactory(type);
			if (factory instanceof Method)
				return ((Method) factory).invoke(null, spec);
			return ((Constructor<?>) factory).newInstance(spec);
		}
		catch (Exception e) {
			if (verbose)
				LogUtils.warn("cannot create for type reference " + objSpec, e);
			return objSpec;
		}
	}

	private static AccessibleObject getFactory(String typeReference) throws ClassNotFoundException, SecurityException,
	        NoSuchMethodException {
		// backward compatibility
		if (typeReference.equals("org.freeplane.core.util.FreeplaneDate")
		        || typeReference.equals("org.freeplane.features.common.format.FormattedDate"))
			typeReference = FormattedDate.class.getName();
		else if (typeReference.equals("org.freeplane.features.common.format.FormattedNumber"))
		    typeReference = FormattedNumber.class.getName();
		final Class<?> clazz = TypeReference.class.getClassLoader().loadClass(typeReference);
		final FactoryMethod factoryAnnotation = clazz.getAnnotation(FactoryMethod.class);
		if (factoryAnnotation != null)
			return clazz.getMethod(factoryAnnotation.value(), String.class);
		else
			return clazz.getConstructor(String.class);
	}

	public static String toSpec(Object obj){
		final Class<? extends Object> clazz = obj.getClass();
		final SerializationMethod method = clazz.getAnnotation(SerializationMethod.class);
		final String type = clazz.getName() + '|';
		if(method == null){
			return type + obj.toString();
		}
		try {
	        return type + clazz.getMethod(method.value(), obj.getClass()).invoke(null, obj).toString();
        }
		catch (Exception e) {
			LogUtils.warn(e);
			return obj.toString();
		}
	}

	/** copy of HtmlUtils.unicodeToHTMLUnicodeEntity() with the exception that the separator char '|' and the XML
     * special chars '"' and '&' are escaped too. */
    public static String encode(String text) {
    	final StringBuilder result = new StringBuilder((int) (text.length() * 1.2));
    	int intValue;
    	char myChar;
    	for (int i = 0; i < text.length(); ++i) {
    		myChar = text.charAt(i);
    		intValue = text.charAt(i);
    		if (intValue < 32 || intValue == 34 || intValue == 38 || intValue == 124) {
    			result.append("&#x").append(Integer.toString(intValue, 16)).append(';');
    		}
    		else {
    			result.append(myChar);
    		}
    	}
    	return result.toString();
    }

	public static String decode(final String spec) {
        return HtmlUtils.unescapeHTMLUnicodeEntity(spec);
    }
}