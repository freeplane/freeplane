package org.freeplane.plugin.script.groovypatch;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

public class CachedFieldInterceptor {
	public static Object intercept(@SuperCall Callable<?> c, @FieldValue("field") Field field){
		try {
			AccessPermissionChecker.checkAccessPermission(field);
			return c.call();
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}