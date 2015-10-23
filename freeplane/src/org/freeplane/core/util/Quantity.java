package org.freeplane.core.util;

public class Quantity <U extends Enum<U> & Convertible >{
	
	public static <U extends Enum<U> & Convertible>  Quantity<U> fromString(String valueString, U defaultUnit) {
		final int separatorPosition = valueString.lastIndexOf(' ');
		final String numberString;
		final U unit;
		if(separatorPosition >= 0){
			numberString = valueString.substring(0, separatorPosition);
			String unitString = valueString.substring(separatorPosition + 1);
			final Class<U> unitClass = (Class<U>)defaultUnit.getDeclaringClass();
			unit = Enum.valueOf(unitClass, unitString);
		}
		else {
			numberString = valueString;
			unit = defaultUnit;
		}
		double doubleValue = Double.parseDouble(numberString);
		return new Quantity<U>(doubleValue, unit);
	}

	final public double value;
	final public U unit;

	public Quantity(double value, U unit) {
		this.value = value;
		this.unit = unit;
	}

	double toBaseUnits() {
		return value * unit.factor();
	}

	public int toBaseUnitsRounded() {
		return (int) (toBaseUnits() + 0.5d);
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
