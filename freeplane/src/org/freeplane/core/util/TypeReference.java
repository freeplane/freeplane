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


/**
 * @author Dimitry Polivaev
 * Mar 2, 2011
 */
public class TypeReference{

	static public Object create(final String objSpec){
		final int sep = objSpec.indexOf('|');
		final String type = objSpec.substring(0, sep);
		final AccessibleObject factory = getFactory(type);
		final String spec = objSpec.substring(sep + 1);
		try {
			if(factory instanceof Method)
				return ((Method) factory).invoke(null, spec);
			return ((Constructor<?>) factory).newInstance(spec);
		}
		catch (Exception e) {
			LogUtils.warn(e);
			return spec;
		}
	}
	
	private static AccessibleObject getFactory (String typeReference) {
		AccessibleObject constructor;
        try {
            final Class<?> clazz = TypeReference.class.getClassLoader().loadClass(typeReference);
            final FactoryMethod factoryAnnotation = clazz.getAnnotation(FactoryMethod.class);
            if(factoryAnnotation != null)
            	constructor = clazz.getMethod(factoryAnnotation.value(), String.class);
            
            else
            	constructor = clazz.getConstructor(String.class);
            return constructor;
        }
        catch (Exception e) {
            LogUtils.warn(e);
        	return null;
        }
    }
	public static String toSpec(Object obj){
		final Class<? extends Object> clazz = obj.getClass();
		if(clazz.equals(String.class)){
			return obj.toString();
		}
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
}