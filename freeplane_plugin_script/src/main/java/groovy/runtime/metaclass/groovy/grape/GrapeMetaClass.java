package groovy.runtime.metaclass.groovy.grape;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyClassLoader;
import groovy.lang.MetaClass;
import org.codehaus.groovy.reflection.ReflectionUtils;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class GrapeMetaClass extends DelegatingMetaClass {
	static final private Collection<String> EXTRA_IGNORED_PACKAGES = Arrays.asList(
			GrapeMetaClass.class.getPackage().getName(),
			AccessController.class.getPackage().getName());
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
						final Class callingClass = ReflectionUtils.getCallingClass(0, EXTRA_IGNORED_PACKAGES);
						final ClassLoader classLoader = callingClass.getClassLoader();
						if(! (classLoader instanceof GroovyClassLoader))
							return null;
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
