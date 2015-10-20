package org.freeplane.core.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.Test;

public class QuantityShould {

	enum Metrics implements Convertible{
		m(1d), cm(0.01d);

		Metrics(double factor){
			this.factor = factor;
		}

		final private double factor;
		@Override
		public double factor() {
			return factor;
		}

	}

	@Test
	public void calculateValueInBaseUnits() throws Exception {
		Quantity<Metrics> quantity = new Quantity<Metrics>(1, Metrics.cm);
		assertThat(quantity.inBaseUnits(), equalTo(0.01));
	}


	@Test
	public void returnNumericValue() throws Exception {
		Quantity<Metrics> quantity = new Quantity<Metrics>(1, Metrics.cm);
		assertThat(quantity.value, equalTo(1d));
	}


	@Test
	public void returnUnit() throws Exception {
		Quantity<Metrics> quantity = new Quantity<Metrics>(1, Metrics.cm);
		assertThat(quantity.unit, equalTo(Metrics.cm));
	}
	

	@Test
	public void calculateRoundedDownValueInBaseUnits() throws Exception {
		Quantity<Metrics> quantity = new Quantity<Metrics>(49, Metrics.cm);
		assertThat(quantity.inBaseUnitsRounded(), equalTo(0));
	}


	@Test
	public void calculateRoundedUpValueInBaseUnits() throws Exception {
		Quantity<Metrics> quantity = new Quantity<Metrics>(51, Metrics.cm);
		assertThat(quantity.inBaseUnitsRounded(), equalTo(1));
	}
	
	@Test
	public void convertToString() throws Exception {
		Quantity<Metrics> quantity = new Quantity<Metrics>(51, Metrics.cm);
		assertEquals("51.0 cm", quantity.toString());
	}
	
	@Test
	public void fromString_0_cm() throws Exception {
		Quantity<Metrics> quantity = Quantity.fromString("0 cm", Metrics.class);
		assertEquals("0.0 cm", quantity.toString());
	}

	@Test
	public void fromString_1_cm() throws Exception {
		Quantity<Metrics> quantity = Quantity.fromString("1 cm", Metrics.class);
		assertEquals("1.0 cm", quantity.toString());
	}

	@Test
	public void fromString_2_m() throws Exception {
		Quantity<Metrics> quantity = Quantity.fromString("1 m", Metrics.class);
		assertEquals("1.0 m", quantity.toString());
	}
}
