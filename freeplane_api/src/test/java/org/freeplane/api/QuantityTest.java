package org.freeplane.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.freeplane.api.LengthUnit.cm;
import static org.freeplane.api.LengthUnit.pt;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class QuantityTest {

    enum Metrics implements PhysicalUnit{
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
        assertThat(quantity.toBaseUnits(), equalTo(0.01));
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
        assertThat(quantity.toBaseUnitsRounded(), equalTo(0));
    }


    @Test
    public void calculateRoundedUpValueInBaseUnits() throws Exception {
        Quantity<Metrics> quantity = new Quantity<Metrics>(51, Metrics.cm);
        assertThat(quantity.toBaseUnitsRounded(), equalTo(1));
    }
    
    @Test
    public void convertToString() throws Exception {
        Quantity<Metrics> quantity = new Quantity<Metrics>(51, Metrics.cm);
        assertEquals("51 cm", quantity.toString());
    }
    
    @Test
    public void fromNullString() throws Exception {
        Quantity<Metrics> quantity = Quantity.fromString(null, Metrics.cm);
        assertNull(quantity);
    }
    
    @Test
    public void fromString_0_cm() throws Exception {
        Quantity<Metrics> quantity = Quantity.fromString("0 cm", Metrics.cm);
        assertEquals("0 cm", quantity.toString());
    }

    @Test
    public void fromString_1_cm() throws Exception {
        Quantity<Metrics> quantity = Quantity.fromString("1 cm", Metrics.cm);
        assertEquals("1 cm", quantity.toString());
    }

    @Test
    public void fromString_2_m() throws Exception {
        Quantity<Metrics> quantity = Quantity.fromString("1 m", Metrics.cm);
        assertEquals("1 m", quantity.toString());
    }
    
    @Test
    public void useDefaultUnit_fromString_1() throws Exception {
        Quantity<Metrics> quantity = Quantity.fromString("1", Metrics.cm);
        assertEquals("1 cm", quantity.toString());
    }
    
    @Test
    public void convertUnits() throws Exception {
        Quantity<Metrics> quantityInMeters = new Quantity<Metrics>(1, Metrics.m);
        Quantity<Metrics> quantityInCm = quantityInMeters.in(Metrics.cm);
        assertThat(quantityInCm, equalTo(new Quantity<Metrics>(100, Metrics.cm)));
    }
    
    @Test
    public void addQuantitiesInSameUnits() throws Exception {
        Quantity<Metrics> first = new Quantity<Metrics>(1, Metrics.m);
        Quantity<Metrics> second = new Quantity<Metrics>(2, Metrics.m);
        assertThat(first.add(second), equalTo(new Quantity<Metrics>(3, Metrics.m)));
    }
    
    @Test
    public void addQuantitiesInDifferentUnits() throws Exception {
        Quantity<Metrics> first = new Quantity<Metrics>(100, Metrics.cm);
        Quantity<Metrics> second = new Quantity<Metrics>(2, Metrics.m);
        assertThat(first.add(second), equalTo(new Quantity<Metrics>(300, Metrics.cm)));
    }

    
    @Test
    public void zoomQuantity() throws Exception {
        Quantity<Metrics> q = new Quantity<Metrics>(100, Metrics.cm);
        assertThat(q.zoomBy(0.5), equalTo(new Quantity<Metrics>(50, Metrics.cm)));
    }
    @Test
    public void addQuantitiesGivenAsPrimitives() throws Exception {
        Quantity<Metrics> first = new Quantity<Metrics>(100, Metrics.cm);
        assertThat(first.add(2, Metrics.m), equalTo(new Quantity<Metrics>(300, Metrics.cm)));
    }
    
    @Test(expected=IllegalStateException.class)
    public void negativeQuantity_throwsIllegalStateExceptionOnCheckForNonNegative() throws Exception {
        Quantity<Metrics> negative = new Quantity<Metrics>(-1, Metrics.cm);
        negative.assertNonNegative();
    }
    
    @Test()
    public void zeroQuantity_passesOnCheckForNonNegative() throws Exception {
        Quantity<Metrics> zero = new Quantity<Metrics>(0, Metrics.cm);
        zero.assertNonNegative();
    }
    
    @Test()
    public void nullQuantity_passesOnCheckForNonNegative() throws Exception {
        Quantity.assertNonNegativeOrNull(null);
    }
    
    @Test
    public void rounds4cm_WhenConvertsToString() throws Exception {
        assertThat(new Quantity<>(4, cm).toString()).isEqualTo("4 cm");
        assertThat(new Quantity<>(4, cm).in(pt).toString()).isEqualTo("113.38583 pt");
        assertThat(Quantity.fromString("113.38583 pt", cm).toString()).isEqualTo("113.38583 pt");
        assertThat(Quantity.fromString("113.38583 pt", cm).in(cm).toString()).isEqualTo("4 cm");
    }
    @Test
    public void rounds40cm_WhenConvertsToString() throws Exception {
        assertThat(new Quantity<>(40, cm).toString()).isEqualTo("40 cm");
        assertThat(new Quantity<>(40, cm).in(pt).toString()).isEqualTo("1133.85827 pt");
        assertThat(Quantity.fromString("1133.85827 pt", cm).toString()).isEqualTo("1133.85827 pt");
        assertThat(Quantity.fromString("1133.85827 pt", cm).in(cm).toString()).isEqualTo("40 cm");
    }
}
