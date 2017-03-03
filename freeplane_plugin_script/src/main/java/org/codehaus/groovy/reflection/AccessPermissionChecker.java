package org.codehaus.groovy.reflection;

import java.lang.reflect.Modifier;
import java.lang.reflect.ReflectPermission;
import java.security.AccessControlException;

class AccessPermissionChecker {

	private static final ReflectPermission REFLECT_PERMISSION = new ReflectPermission("suppressAccessChecks");

	static void checkAccessPermission(Class<?> declaringClass, String memberType, String name, final int modifiers){
		final SecurityManager securityManager = System.getSecurityManager();
		if (securityManager != null && (modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) {
			try {
				securityManager.checkPermission(REFLECT_PERMISSION);
			}
			catch (AccessControlException ex){
				throw new IllegalArgumentException("Illegal access to " + memberType + " " + name);
			}
		}
	}

}
