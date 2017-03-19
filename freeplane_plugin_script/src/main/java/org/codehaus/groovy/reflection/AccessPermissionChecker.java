package org.codehaus.groovy.reflection;

import java.lang.reflect.Modifier;
import java.lang.reflect.ReflectPermission;
import java.security.AccessControlException;

import groovy.lang.GroovyObject;

class AccessPermissionChecker {

	private static final ReflectPermission REFLECT_PERMISSION = new ReflectPermission("suppressAccessChecks");

	static void checkAccessPermission(Class<?> declaringClass, final int modifiers, boolean isAccessible, String memberType,
	                                  String name) {
		final SecurityManager securityManager = System.getSecurityManager();
		if (isAccessible && securityManager != null) {
			try {
				if ((modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0
						&& !GroovyObject.class.isAssignableFrom(declaringClass)) {
                        securityManager.checkPermission(REFLECT_PERMISSION);
                }
                else if ((modifiers & (Modifier.PUBLIC)) == 0
					&& declaringClass.equals(ClassLoader.class)){
					securityManager.checkCreateClassLoader();
				}
			}
			catch (AccessControlException ex) {
				throw new IllegalArgumentException("Illegal access to " + memberType + " " + name);
			}
		}
	}

}
