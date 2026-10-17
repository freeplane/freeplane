package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class TreeTableGridTest {
	private static final TreeTableGeometry GEOMETRY = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

	private static TreeTableGrid gridWithLines(final int[] tops, final int[] bottoms) {
		return new TreeTableGrid(1000, GEOMETRY, tops, bottoms, 3);
	}

	@Test
	public void surroundsWholeTableWithPadding() {
		final TreeTableGrid grid = gridWithLines(new int[] { 20, 50, 80 }, new int[] { 40, 70, 100 });

		assertThat(grid.getLeft()).isEqualTo(997);
		assertThat(grid.getRight()).isEqualTo(1233);
		assertThat(grid.getTop()).isEqualTo(17);
		assertThat(grid.getBottom()).isEqualTo(103);
	}

	@Test
	public void separatesHeaderFromFirstRowInTheMiddleOfTheirGap() {
		final TreeTableGrid grid = gridWithLines(new int[] { 20, 50, 80 }, new int[] { 40, 70, 100 });

		assertThat(grid.hasHeaderSeparator()).isTrue();
		assertThat(grid.getHeaderSeparatorY()).isEqualTo(45);
	}

	@Test
	public void separatesConsecutiveRowsInTheMiddleOfTheirGap() {
		final TreeTableGrid grid = gridWithLines(new int[] { 20, 50, 80, 110 }, new int[] { 40, 70, 100, 130 });

		assertThat(grid.getRowSeparatorCount()).isEqualTo(2);
		assertThat(grid.getRowSeparatorY(0)).isEqualTo(75);
		assertThat(grid.getRowSeparatorY(1)).isEqualTo(105);
	}

	@Test
	public void hasNoSeparatorsForHeaderAlone() {
		final TreeTableGrid grid = gridWithLines(new int[] { 20 }, new int[] { 40 });

		assertThat(grid.hasHeaderSeparator()).isFalse();
		assertThat(grid.getRowSeparatorCount()).isZero();
	}

	@Test
	public void hasNoRowSeparatorForSingleRow() {
		final TreeTableGrid grid = gridWithLines(new int[] { 20, 50 }, new int[] { 40, 70 });

		assertThat(grid.hasHeaderSeparator()).isTrue();
		assertThat(grid.getRowSeparatorCount()).isZero();
	}

	@Test
	public void placesColumnSeparatorsInTheMiddleOfTheGapBeforeEachColumn() {
		final TreeTableGrid grid = gridWithLines(new int[] { 20 }, new int[] { 40 });

		assertThat(grid.getColumnSeparatorCount()).isEqualTo(2);
		assertThat(grid.getColumnSeparatorX(0)).isEqualTo(1105);
		assertThat(grid.getColumnSeparatorX(1)).isEqualTo(1165);
	}

	@Test
	public void rejectsInconsistentLines() {
		assertThatThrownBy(() -> gridWithLines(new int[] {}, new int[] {}))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> gridWithLines(new int[] { 20, 50 }, new int[] { 40 }))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void rejectsNegativePadding() {
		assertThatThrownBy(() -> new TreeTableGrid(0, GEOMETRY, new int[] { 20 }, new int[] { 40 }, -1))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
