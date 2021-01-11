package org.freeplane.api;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class Quantity <U extends Enum<U> & PhysicalUnit >{
    private final static DecimalFormat  ROUNDING_FORMAT = new DecimalFormat("###.#####", DecimalFormatSymbols.getInstance(Locale.US));
	
	public static <U extends Enum<U> & PhysicalUnit>  Quantity<U> fromString(String valueString, U defaultUnit) {
		if(valueString == null)
			return null;
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
		try {
            double doubleValue = ROUNDING_FORMAT.parse(numberString).doubleValue();
            return new Quantity<U>(doubleValue, unit);
        } catch (ParseException e) {
            throw new NumberFormatException("Invalid number " + numberString);
        }
	}

	final public double value;
	final public U unit;

	public Quantity(double value, U unit) {
		this.value = value;
		this.unit = unit;
	}

	public double toBaseUnits() {
		return value * unit.factor();
	}

	public int toBaseUnitsRounded() {
		return (int) (toBaseUnits() + 0.5d);
	}

	@Override
	public String toString() {
	    String rounded = ROUNDING_FORMAT.format(value);
		return rounded + " " + unit;
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

	public Quantity<U> in(U unit) {
		return new Quantity<U>(value * (this.unit.factor() / unit.factor()), unit);
	}
	
	public void assertNonNegative() {
		if(value < 0)
			throw new IllegalStateException("non negative value required");
	}

	public static <U extends Enum<U> & PhysicalUnit> void assertNonNegativeOrNull(Quantity<U> quantity) {
		if(quantity != null)
			quantity.assertNonNegative();
	}

	public Quantity<U> add(Quantity<U> second) {
		if(unit == second.unit)
			return new Quantity<U>(value + second.value, unit);
		else {
			final double sum = value + second.in(unit).value;
			return new Quantity<U>(sum, unit);
		}
	}

	public Quantity<U> add(double value, U unit) {
		return add(new Quantity<U>(value, unit));
	}

	public Quantity<U> zoomBy(double zoom) {
		return new Quantity<U>(value * zoom, unit);
	}
}
