package groovy.runtime.metaclass.java.lang;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.MetaClass;

import org.freeplane.plugin.script.proxy.Convertible;

/** Make Convertible known to class String and let String handle Convertibles as if they were Strings
 * (via Convertible.getText(). */
public class StringMetaClass extends DelegatingMetaClass {
	public StringMetaClass(MetaClass delegate) {
		super(delegate);
	}

	@Override
	public Object invokeMethod(Object object, String methodName, Object[] arguments) {
		replaceConvertibleByText(arguments);
		return super.invokeMethod(object, methodName, arguments);
	}

	@Override
    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
		replaceConvertibleByText(arguments);
	    return super.invokeStaticMethod(object, methodName, arguments);
    }

	@Override
    public Object invokeConstructor(Object[] arguments) {
		replaceConvertibleByText(arguments);
	    return super.invokeConstructor(arguments);
    }

	private void replaceConvertibleByText(Object[] arguments) {
	    for (int i = 0; i < arguments.length; i++) {
			if (arguments[i] instanceof Convertible)
				arguments[i] = ((Convertible) arguments[i]).getText();
		}
    }
}
