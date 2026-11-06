package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class TreeTableGridTest {
	private static final int[] COLUMN_BOUNDS = { 200, 260, 330, 400 };

	private static TreeTableGrid grid(final int[] rowBottoms) {
		return new TreeTableGrid(90, 20, COLUMN_BOUNDS, 80, rowBottoms, new int[] { 50, 35 });
	}

	@Test
	public void placesColumnBoundsBetweenNeighbourColumns() {
		final int[] bounds = TreeTableGrid.columnBounds(new int[] { 210, 270 }, new int[] { 250, 330 }, 3);

		assertThat(bounds).containsExactly(207, 260, 333);
	}

	@Test
	public void rejectsMissingColumns() {
		assertThatThrownBy(() -> TreeTableGrid.columnBounds(new int[] {}, new int[] {}, 3))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void framesTableFromRootTopToLastRow() {
		final TreeTableGrid grid = grid(new int[] { 110, 140 });

		assertThat(grid.getLeft()).isEqualTo(90);
		assertThat(grid.getTop()).isEqualTo(20);
		assertThat(grid.getRight()).isEqualTo(400);
		assertThat(grid.getBottom()).isEqualTo(140);
	}

	@Test
	public void endsTableUnderHeaderWithoutRows() {
		assertThat(grid(new int[] {}).getBottom()).isEqualTo(80);
	}

	@Test
	public void findsColumnContainingPosition() {
		final TreeTableGrid grid = grid(new int[] { 110 });

		assertThat(grid.getColumnIndexAt(200)).isEqualTo(0);
		assertThat(grid.getColumnIndexAt(329)).isEqualTo(1);
		assertThat(grid.getColumnIndexAt(199)).isEqualTo(-1);
		assertThat(grid.getColumnIndexAt(400)).isEqualTo(-1);
	}

	@Test
	public void findsResizeEdgeOnColumnRightBound() {
		final TreeTableGrid grid = grid(new int[] { 110 });

		assertThat(grid.getColumnIndexWithResizeEdgeNear(262, 3)).isEqualTo(0);
		assertThat(grid.getColumnIndexWithResizeEdgeNear(398, 3)).isEqualTo(2);
		assertThat(grid.getColumnIndexWithResizeEdgeNear(230, 3)).isEqualTo(-1);
	}

	@Test
	public void startsFirstSeparatorAtTableTopAndOthersUnderCommonAncestor() {
		final TreeTableGrid grid = grid(new int[] { 110 });

		assertThat(grid.getColumnSeparatorCount()).isEqualTo(3);
		assertThat(grid.getColumnSeparatorX(1)).isEqualTo(260);
		assertThat(grid.getColumnSeparatorTop(0)).isEqualTo(20);
		assertThat(grid.getColumnSeparatorTop(1)).isEqualTo(50);
		assertThat(grid.getColumnSeparatorTop(2)).isEqualTo(35);
	}

	@Test
	public void rejectsInconsistentSeparators() {
		assertThatThrownBy(() -> new TreeTableGrid(0, 0, COLUMN_BOUNDS, 80, new int[] {}, new int[] { 50 }))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void placesBorderLinesLikeRectangleNodeBorder() {
		assertThat(TreeTableGrid.borderLineStart(20, 3)).isEqualTo(21);
		assertThat(TreeTableGrid.borderLineEnd(20, 30, 3)).isEqualTo(48);
	}

	@Test
	public void placesUnderlineLikeForkNodeBorder() {
		assertThat(TreeTableGrid.underlineY(20, 30, 2)).isEqualTo(48);
		assertThat(TreeTableGrid.underlineY(20, 30, 1)).isEqualTo(49);
	}

	@Test
	public void leavesSameSpaceBeforeContentAsBeforeColumnText() {
		assertThat(TreeTableGrid.leftOfContent(1000, 3, 4)).isEqualTo(993);
	}
}
