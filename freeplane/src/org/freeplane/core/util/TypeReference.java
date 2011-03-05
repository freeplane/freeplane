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
private AccessibleObject factory;

	public TypeReference(String typeReference) {
		AccessibleObject constructor;
        try {
            final Class<?> clazz = getClass().getClassLoader().loadClass(typeReference);
            final FactoryMethod factoryAnnotation = clazz.getAnnotation(FactoryMethod.class);
            if(factoryAnnotation != null)
            	constructor = clazz.getMethod(factoryAnnotation.value(), String.class);
            
            else
            	constructor = clazz.getConstructor(String.class);
            this.factory = constructor;
        }
        catch (Exception e) {
        	this.factory = null;
            e.printStackTrace();
        }
    }
	public Object create(String spec){
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
	
	public static String toString(Object obj){
		final Class<? extends Object> clazz = obj.getClass();
		if(clazz.equals(String.class)){
			return obj.toString();
		}
		final SerializationMethod method = clazz.getAnnotation(SerializationMethod.class);
		if(method == null){
			return obj.toString();
		}
		try {
	        return clazz.getMethod(method.value(), obj.getClass()).invoke(null, obj).toString();
        }
		catch (Exception e) {
			LogUtils.warn(e);
			return obj.toString();
		}
	}
}