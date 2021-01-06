package org.freeplane.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.freeplane.api.LengthUnit.cm;
import static org.freeplane.api.LengthUnit.pt;

import org.junit.Test;

public class QuantityTest {
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
