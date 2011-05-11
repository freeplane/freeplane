package org.freeplane.plugin.script.proxy;


public class ConvertibleNumber extends Convertible {
	final private Number number;

	public ConvertibleNumber(final Number number) {
	    super(number);
	    this.number = number;
    }

	@Override
    public Number getNum() {
	    return number;
    }

	@Override
    public Number getNum0() {
		if (number == null)
			return 0;
	    return number;
    }

	@Override
    public Object getObject() {
	    return number;
    }

	@Override
    public boolean isNum() {
		return true;
    }

	@Override
    public boolean isDate() {
		return false;
    }
}
