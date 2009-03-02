package org.freeplane.core.model;

/**
 * Container for triplets.
 * 
 * There are no restrictions on the types of the attributes.
 * 
 * @author robert.ladstaetter
 *
 * @param <A>
 * @param <B>
 * @param <C>
 */
public class Triple<A, B, C> {
	A a;
	B b;
	C c;
	public Triple(A a, B b, C c) {
	    super();
	    this.a = a;
	    this.b = b;
	    this.c = c;
    }
	public A getA() {
    	return a;
    }
	public void setA(A a) {
    	this.a = a;
    }
	public B getB() {
    	return b;
    }
	public void setB(B b) {
    	this.b = b;
    }
	public C getC() {
    	return c;
    }
	public void setC(C c) {
    	this.c = c;
    }
}
