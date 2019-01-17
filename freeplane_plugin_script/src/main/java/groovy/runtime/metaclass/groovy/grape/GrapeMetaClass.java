package groovy.runtime.metaclass.groovy.grape;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.MetaClass;
import org.codehaus.groovy.reflection.ReflectionUtils;

public class GrapeMetaClass extends DelegatingMetaClass {
	public GrapeMetaClass(MetaClass delegate) {
		super(delegate);
	}

	@Override
	public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
		if (methodName.equals("grab")
				&& arguments.length > 1
				&& (arguments[0] instanceof Map)) {
			return AccessController.doPrivileged(new PrivilegedAction<Object>() {
				@Override
				public Object run() {
					final Map map = (Map) arguments[0];
					if (map.get("refObject") == null && map.get("classLoader") == null) {
						final Class callingClass = ReflectionUtils.getCallingClass(2);
						final ClassLoader classLoader = callingClass.getClassLoader();
						map.put("classLoader", classLoader);
					}
					return GrapeMetaClass.super.invokeStaticMethod(object, methodName, arguments);
				}
			});
		}
		else
			return GrapeMetaClass.super.invokeStaticMethod(object, methodName, arguments);
	}
}
