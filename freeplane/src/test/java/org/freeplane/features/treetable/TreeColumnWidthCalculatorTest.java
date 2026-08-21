package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class TreeColumnWidthCalculatorTest {

	@Test
	public void reportsZeroWithoutRows() {
		assertThat(new TreeColumnWidthCalculator().getWidth()).isZero();
	}

	@Test
	public void usesIndentedWidthOfSingleRow() {
		final TreeColumnWidthCalculator calculator = new TreeColumnWidthCalculator();

		calculator.addRow(20, 80);

		assertThat(calculator.getWidth()).isEqualTo(100);
	}

	@Test
	public void usesWidestIndentedRow() {
		final TreeColumnWidthCalculator calculator = new TreeColumnWidthCalculator();

		calculator.addRow(0, 120);
		calculator.addRow(40, 90);
		calculator.addRow(20, 60);

		assertThat(calculator.getWidth()).isEqualTo(130);
	}

	@Test
	public void letsDeepNarrowRowWinOverShallowWideRow() {
		final TreeColumnWidthCalculator calculator = new TreeColumnWidthCalculator();

		calculator.addRow(0, 100);
		calculator.addRow(80, 50);

		assertThat(calculator.getWidth()).isEqualTo(130);
	}

	@Test
	public void ignoresNarrowerRowAddedLater() {
		final TreeColumnWidthCalculator calculator = new TreeColumnWidthCalculator();

		calculator.addRow(50, 100);
		calculator.addRow(0, 10);

		assertThat(calculator.getWidth()).isEqualTo(150);
	}

	@Test
	public void acceptsRowWithoutIndentationAndWidth() {
		final TreeColumnWidthCalculator calculator = new TreeColumnWidthCalculator();

		calculator.addRow(0, 0);

		assertThat(calculator.getWidth()).isZero();
	}

	@Test
	public void rejectsNegativeMeasures() {
		final TreeColumnWidthCalculator calculator = new TreeColumnWidthCalculator();

		assertThatThrownBy(() -> calculator.addRow(-1, 100)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> calculator.addRow(10, -1)).isInstanceOf(IllegalArgumentException.class);
	}
}
