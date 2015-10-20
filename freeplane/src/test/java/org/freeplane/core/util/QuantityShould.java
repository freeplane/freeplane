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
		double normalized = quantity.inBaseUnits();
		assertThat(normalized, equalTo(0.01));
	}


	@Test
	public void returnNumericValue() throws Exception {
		Quantity<Metrics> quantity = new Quantity<Metrics>(1, Metrics.cm);
		double normalized = quantity.value;
		assertThat(normalized, equalTo(1d));
	}


	@Test
	public void returnUnit() throws Exception {
		Quantity<Metrics> quantity = new Quantity<Metrics>(1, Metrics.cm);
		assertThat(quantity.unit, equalTo(Metrics.cm));
	}
}
