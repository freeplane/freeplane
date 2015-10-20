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

}
