package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleModel;
import org.junit.Before;
import org.junit.Test;

public class TreeTableCellsTest {

	private MapFake mapFake;
	private NodeModel table;
	private NodeModel header;
	private NodeModel row;

	@Before
	public void setUp() {
		mapFake = new MapFake();
		table = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(table);
		header = styledChildOf(table, "Colonnes", TreeTableHeader.style());
		childOf(header, "Prix");
		row = childOf(table, "Projet A");
	}

	private NodeModel childOf(final NodeModel parent, final String text) {
		final NodeModel child = mapFake.createNode(text);
		parent.insert(child);
		return child;
	}

	private NodeModel styledChildOf(final NodeModel parent, final String text, final IStyle style) {
		final NodeModel child = childOf(parent, text);
		LogicalStyleModel.createExtension(child).setStyle(style);
		return child;
	}

	private NodeModel cellOf(final NodeModel parent, final String key) {
		return styledChildOf(parent, key, TreeTableCells.style());
	}

	@Test
	public void recognizesStyledChildOfRowAsCell() {
		final NodeModel price = cellOf(row, "Prix");

		assertThat(TreeTableCells.isCell(price)).isTrue();
		assertThat(TreeTableCells.keyOf(price)).isEqualTo("Prix");
		assertThat(TreeTableNodes.isRow(price)).isFalse();
	}

	@Test
	public void treatsCellTreeAsCellContentNotAsRows() {
		final NodeModel price = cellOf(row, "Prix");
		final NodeModel amount = childOf(price, "12");
		final NodeModel discount = childOf(price, "remise 5 %");
		final NodeModel condition = childOf(discount, "si > 100 u.");

		assertThat(TreeTableCells.cellAbove(condition)).isSameAs(price);
		assertThat(TreeTableCells.isCellContent(amount)).isTrue();
		assertThat(TreeTableCells.isCellContent(price)).isFalse();
		assertThat(TreeTableNodes.isRow(condition)).isFalse();
		assertThat(TreeTableNodes.rowsOf(table)).containsExactly(row);
	}

	@Test
	public void keepsSubRowsBesideCells() {
		final NodeModel price = cellOf(row, "Prix");
		final NodeModel subRow = childOf(row, "Tache 1");

		assertThat(TreeTableCells.cellsOf(row)).containsExactly(price);
		assertThat(TreeTableCells.subRowsOf(row)).containsExactly(subRow);
		assertThat(TreeTableNodes.rowsOf(table)).containsExactly(row, subRow);
	}

	@Test
	public void findsCellOfRowByColumnKey() {
		final NodeModel price = cellOf(row, "Prix");
		cellOf(row, "Delai");

		assertThat(TreeTableCells.cellOf(row, "Prix")).isSameAs(price);
		assertThat(TreeTableCells.cellOf(row, "Cout")).isNull();
	}

	@Test
	public void usesOutermostCellStyledNodeAsCell() {
		final NodeModel price = cellOf(row, "Prix");
		final NodeModel nested = cellOf(price, "Prix");

		assertThat(TreeTableCells.isCell(nested)).isFalse();
		assertThat(TreeTableCells.cellAbove(nested)).isSameAs(price);
	}

	@Test
	public void ignoresCellStyleOnTableRootChildAndOutsideTables() {
		final NodeModel directChild = cellOf(table, "Prix");
		final NodeModel outside = cellOf(mapFake.getRoot(), "Prix");

		assertThat(TreeTableCells.isCell(directChild)).isFalse();
		assertThat(TreeTableCells.isCell(outside)).isFalse();
	}

	@Test
	public void ignoresCellStyleInHeader() {
		final NodeModel inHeader = cellOf(header, "Prix");

		assertThat(TreeTableCells.isCell(inHeader)).isFalse();
	}
}
