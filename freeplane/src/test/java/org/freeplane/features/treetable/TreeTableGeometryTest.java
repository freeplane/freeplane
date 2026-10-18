package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class TreeTableGeometryTest {

	@Test
	public void placesFirstColumnAfterTreeColumnAndGap() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

		assertThat(geometry.getColumnPosition(0)).isEqualTo(110);
	}

	@Test
	public void placesFollowingColumnAfterPreviousColumnAndGap() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

		assertThat(geometry.getColumnPosition(1)).isEqualTo(170);
	}

	@Test
	public void alignsColumnsIdenticallyForEveryRow() {
		final int[] columnWidths = { 50, 60 };

		final TreeTableGeometry shallowRow = new TreeTableGeometry(100, columnWidths, 10);
		final TreeTableGeometry deepRow = new TreeTableGeometry(100, columnWidths, 10);

		assertThat(deepRow.getColumnPosition(0)).isEqualTo(shallowRow.getColumnPosition(0));
		assertThat(deepRow.getColumnPosition(1)).isEqualTo(shallowRow.getColumnPosition(1));
	}

	@Test
	public void reportsTotalWidthUpToLastColumn() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

		assertThat(geometry.getTotalWidth()).isEqualTo(230);
	}

	@Test
	public void reportsTreeColumnWidthAsTotalWidthWithoutColumns() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] {}, 10);

		assertThat(geometry.getTotalWidth()).isEqualTo(100);
		assertThat(geometry.getColumnCount()).isZero();
	}

	@Test
	public void handlesZeroGap() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 50, 60 }, 0);

		assertThat(geometry.getColumnPosition(0)).isEqualTo(100);
		assertThat(geometry.getColumnPosition(1)).isEqualTo(150);
		assertThat(geometry.getTotalWidth()).isEqualTo(210);
	}

	@Test
	public void keepsEmptyColumnAddressable() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 0, 60 }, 10);

		assertThat(geometry.getColumnPosition(0)).isEqualTo(110);
		assertThat(geometry.getColumnWidth(0)).isZero();
		assertThat(geometry.getColumnPosition(1)).isEqualTo(120);
	}

	@Test
	public void findsColumnIndexAtPosition() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

		assertThat(geometry.getColumnIndexAt(110)).isZero();
		assertThat(geometry.getColumnIndexAt(159)).isZero();
		assertThat(geometry.getColumnIndexAt(170)).isEqualTo(1);
	}

	@Test
	public void reportsNoColumnInTreeColumnArea() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

		assertThat(geometry.getColumnIndexAt(50)).isEqualTo(-1);
	}

	@Test
	public void reportsNoColumnInsideGap() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

		assertThat(geometry.getColumnIndexAt(165)).isEqualTo(-1);
	}

	@Test
	public void reportsNoColumnBeyondLastColumn() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

		assertThat(geometry.getColumnIndexAt(300)).isEqualTo(-1);
	}

	@Test
	public void isNotAffectedByLaterChangesOfGivenWidths() {
		final int[] columnWidths = { 50, 60 };
		final TreeTableGeometry geometry = new TreeTableGeometry(100, columnWidths, 10);

		columnWidths[0] = 999;

		assertThat(geometry.getColumnWidth(0)).isEqualTo(50);
	}

	@Test
	public void rejectsNegativeMeasures() {
		assertThatThrownBy(() -> new TreeTableGeometry(-1, new int[] { 50 }, 10))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new TreeTableGeometry(100, new int[] { -1 }, 10))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new TreeTableGeometry(100, new int[] { 50 }, -1))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void placesResizeEdgeInTheMiddleOfTheFollowingGap() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

		assertThat(geometry.getColumnResizeEdge(0)).isEqualTo(165);
	}

	@Test
	public void placesLastResizeEdgeAtTableEnd() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

		assertThat(geometry.getColumnResizeEdge(1)).isEqualTo(230);
	}

	@Test
	public void findsColumnWhoseResizeEdgeIsNear() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

		assertThat(geometry.getColumnIndexWithResizeEdgeNear(163, 3)).isEqualTo(0);
		assertThat(geometry.getColumnIndexWithResizeEdgeNear(232, 3)).isEqualTo(1);
	}

	@Test
	public void findsNoResizeEdgeFarFromColumnEdges() {
		final TreeTableGeometry geometry = new TreeTableGeometry(100, new int[] { 50, 60 }, 10);

		assertThat(geometry.getColumnIndexWithResizeEdgeNear(140, 3)).isEqualTo(-1);
		assertThat(geometry.getColumnIndexWithResizeEdgeNear(100, 3)).isEqualTo(-1);
	}
}
