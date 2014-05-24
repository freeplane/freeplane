package org.freeplane.plugin.script;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.typehandling.NumberMath;
import org.freeplane.plugin.script.proxy.Proxy;

/** provides class Integer, Double etc. with support for arithmetics with nodes (<code>Number <operator> Node</code>).
 * @see {@link NodeProxy} for <code>Node <operator> Node</code> and <code>Node <operator> Number</code>
 */
public class NodeArithmeticsCategory {
	public static Number and(final Number self, final Proxy.Node node) {
		return NumberMath.and(self, node.getTo().getNum0());
	}

	public static Number div(final Number self, final Proxy.Node node) {
		return NumberMath.divide(self, node.getTo().getNum0());
	}

	public static Number minus(final Number self, final Proxy.Node node) {
		return NumberMath.subtract(self, node.getTo().getNum0());
	}

	public static Number mod(final Number self, final Proxy.Node node) {
		return NumberMath.mod(self, node.getTo().getNum0());
	}

	public static Number multiply(final Number self, final Proxy.Node node) {
		return NumberMath.multiply(self, node.getTo().getNum0());
	}

	public static Number or(final Number self, final Proxy.Node node) {
		return NumberMath.or(self, node.getTo().getNum0());
	}

	public static Number plus(final Number self, final Proxy.Node node) {
		return NumberMath.add(self, node.getTo().getNum0());
	}

	public static Number power(final Number self, final Proxy.Node node) {
		return DefaultGroovyMethods.power(self, node.getTo().getNum0());
	}

	public static Number xor(final Number self, final Proxy.Node node) {
		return NumberMath.xor(self, node.getTo().getNum0());
	}
}
