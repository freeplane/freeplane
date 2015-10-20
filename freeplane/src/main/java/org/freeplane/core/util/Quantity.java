package org.freeplane.core.util;

public class Quantity <U extends Enum<U> & Convertible >{

	public static <U extends Enum<U> & Convertible>  Quantity<U> fromString(String valueString, Class<U> unitClass) {
		int separatorPosition = valueString.lastIndexOf(' ');
		String numberString = valueString.substring(0, separatorPosition);
		double doubleValue = Double.parseDouble(numberString);
		String unitString = valueString.substring(separatorPosition + 1);
		U unit = Enum.valueOf(unitClass, unitString);
		return new Quantity<U>(doubleValue, unit);
	}

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

	@Override
	public String toString() {
		return value + " " + unit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + unit.hashCode();
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		Quantity other = (Quantity) obj;
		if (!unit.equals(other.unit))
			return false;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
			return false;
		return true;
	}
	
	

}
