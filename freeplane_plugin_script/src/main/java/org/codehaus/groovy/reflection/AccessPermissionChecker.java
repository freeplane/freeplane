package org.codehaus.groovy.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ReflectPermission;
import java.security.AccessControlException;

import groovy.lang.GroovyObject;

class AccessPermissionChecker {

	private static final ReflectPermission REFLECT_PERMISSION = new ReflectPermission("suppressAccessChecks");

	static private void checkAccessPermission(Class<?> declaringClass, final int modifiers, boolean isAccessible, String memberType,
	                                  String name) {
		final SecurityManager securityManager = System.getSecurityManager();
		if (isAccessible && securityManager != null && (modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0
		        && !GroovyObject.class.isAssignableFrom(declaringClass)) {
			try {
				securityManager.checkPermission(REFLECT_PERMISSION);
			}
			catch (AccessControlException ex){
				throw new IllegalArgumentException("Illegal access to " + memberType + " " + name);
			}
		}
	}

	static public void checkAccessPermission(Method method) {
		checkAccessPermission(method.getDeclaringClass(), method.getModifiers(), method.isAccessible(), "method",
				method.getName());
	}

	public static void checkAccessPermission(Field field) {
		checkAccessPermission(field.getDeclaringClass(), field.getModifiers(), field.isAccessible(),
		    "field", field.getName());
	}

}
