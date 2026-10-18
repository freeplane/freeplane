package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class TreeTableGridTest {
	private static final TreeTableGeometry GEOMETRY = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

	private static TreeTableGrid gridWithLines(final int top, final int[] bottoms) {
		return new TreeTableGrid(1000, GEOMETRY, top, bottoms);
	}

	@Test
	public void framesTableOnHeaderTopAndLastRowBottom() {
		final TreeTableGrid grid = gridWithLines(20, new int[] { 40, 70, 100 });

		assertThat(grid.getLeft()).isEqualTo(1000);
		assertThat(grid.getRight()).isEqualTo(1230);
		assertThat(grid.getTop()).isEqualTo(20);
		assertThat(grid.getBottom()).isEqualTo(100);
	}

	@Test
	public void separatesHeaderOnItsBottomLine() {
		final TreeTableGrid grid = gridWithLines(20, new int[] { 40, 70, 100 });

		assertThat(grid.hasHeaderSeparator()).isTrue();
		assertThat(grid.getHeaderSeparatorY()).isEqualTo(40);
	}

	@Test
	public void separatesRowsOnTheirBottomLines() {
		final TreeTableGrid grid = gridWithLines(20, new int[] { 40, 70, 100 });

		assertThat(grid.getRowCount()).isEqualTo(2);
		assertThat(grid.getRowBottomY(0)).isEqualTo(70);
		assertThat(grid.getRowBottomY(1)).isEqualTo(100);
	}

	@Test
	public void hasNoSeparatorsForHeaderAlone() {
		final TreeTableGrid grid = gridWithLines(20, new int[] { 40 });

		assertThat(grid.hasHeaderSeparator()).isFalse();
		assertThat(grid.getRowCount()).isZero();
	}

	@Test
	public void placesColumnSeparatorsInTheMiddleOfTheGapBeforeEachColumn() {
		final TreeTableGrid grid = gridWithLines(20, new int[] { 40 });

		assertThat(grid.getColumnSeparatorCount()).isEqualTo(2);
		assertThat(grid.getColumnSeparatorX(0)).isEqualTo(1105);
		assertThat(grid.getColumnSeparatorX(1)).isEqualTo(1165);
	}

	@Test
	public void placesBorderLinesLikeRectangleNodeBorder() {
		assertThat(TreeTableGrid.borderLineStart(20, 3)).isEqualTo(21);
		assertThat(TreeTableGrid.borderLineEnd(20, 30, 3)).isEqualTo(48);
	}

	@Test
	public void placesThinBorderLinesOnOuterPixels() {
		assertThat(TreeTableGrid.borderLineStart(20, 1)).isEqualTo(20);
		assertThat(TreeTableGrid.borderLineEnd(20, 30, 1)).isEqualTo(49);
	}

	@Test
	public void placesUnderlineLikeForkNodeBorder() {
		assertThat(TreeTableGrid.underlineY(20, 30, 2)).isEqualTo(48);
		assertThat(TreeTableGrid.underlineY(20, 30, 1)).isEqualTo(49);
	}

	@Test
	public void rejectsMissingHeaderLine() {
		assertThatThrownBy(() -> gridWithLines(20, new int[] {}))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
