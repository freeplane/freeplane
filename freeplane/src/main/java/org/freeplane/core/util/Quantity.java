package org.freeplane.core.util;

public class Quantity <U extends Enum<U> & Convertible >{

	final public double value;
	final public U unit;

	public Quantity(double value, U unit) {
		this.value = value;
		this.unit = unit;
	}

	public double inBaseUnits() {
		return value * unit.factor();
	}

	public int inBaseUnitsRounded() {
		return (int) (inBaseUnits() + 0.5d);
	}

}
