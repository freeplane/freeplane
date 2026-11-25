package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.LogicalStyleModel;
import org.junit.Before;
import org.junit.Test;

public class TreeTableGridCompletionTest {

	private MapFake mapFake;
	private NodeModel table;
	private NodeModel row;
	private NodeModel subRow;

	@Before
	public void setUp() {
		mapFake = new MapFake();
		table = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(table);
		final NodeModel header = childOf(table, "Colonnes");
		LogicalStyleModel.createExtension(header).setStyle(TreeTableHeader.style());
		childOf(header, "Prix");
		childOf(header, "Delai");
		row = childOf(table, "A");
		subRow = childOf(row, "A.1");
	}

	private NodeModel childOf(final NodeModel parent, final String text) {
		final NodeModel child = mapFake.createNode(text);
		parent.insert(child);
		return child;
	}

	@Test
	public void findsEveryRowColumnPairWithoutContent() {
		final NodeModel price = TreeTableCells.newCell(table.getMap(), "Prix", "12");
		row.insert(price, 0);
		final NodeModel emptyDelay = childOf(row, "Delai");
		TreeTableCellModel.markAsCell(emptyDelay);

		assertThat(TreeTableGridCompletion.gapsOf(table)).extracting(gap -> gap.getRow().getText() + "/"
				+ gap.getColumnKey() + "/" + (gap.getEmptyCell() == emptyDelay))
			.containsExactly("A/Delai/true", "A.1/Prix/false", "A.1/Delai/false");
	}

	@Test
	public void completesGridWithEmptyContentsPlacedBeforeSubRows() {
		assertThat(TreeTableGridCompletion.completeWithoutUndo(table)).isTrue();

		assertThat(TreeTableCells.cellsOf(row)).extracting(TreeTableCells::keyOf).containsExactly("Prix", "Delai");
		assertThat(TreeTableCells.leadingCellCount(row)).isEqualTo(2);
		assertThat(TreeTableCells.subRowsOf(row)).containsExactly(subRow);
		assertThat(TreeTableCells.cellOf(subRow, "Delai").getChildAt(0).getText()).isEmpty();
		assertThat(TreeTableGridCompletion.gapsOf(table)).isEmpty();
		assertThat(TreeTableGridCompletion.completeWithoutUndo(table)).isFalse();
	}

	@Test
	public void findsEmptyCellsOfRemovedColumnsButKeepsTheirData() {
		final NodeModel emptyOrphan = TreeTableCells.newCell(table.getMap(), "Planning", "");
		row.insert(emptyOrphan, 0);
		row.insert(TreeTableCells.newCell(table.getMap(), "Financier", "10"), 0);
		row.insert(TreeTableCells.newCell(table.getMap(), "Prix", ""), 0);

		assertThat(TreeTableGridCompletion.emptyCellsOutsideColumnsOf(table)).containsExactly(emptyOrphan);
	}
}
