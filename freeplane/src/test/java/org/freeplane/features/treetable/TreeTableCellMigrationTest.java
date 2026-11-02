package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.LogicalStyleModel;
import org.junit.Before;
import org.junit.Test;

public class TreeTableCellMigrationTest {

	private MapFake mapFake;
	private NodeModel table;
	private NodeModel header;
	private NodeModel row;

	@Before
	public void setUp() {
		mapFake = new MapFake();
		table = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(table);
		header = childOf(table, "Colonnes");
		LogicalStyleModel.createExtension(header).setStyle(TreeTableHeader.style());
		childOf(header, "Prix");
		childOf(header, "Delai");
		row = childOf(table, "Projet A");
	}

	private NodeModel childOf(final NodeModel parent, final String text) {
		final NodeModel child = mapFake.createNode(text);
		parent.insert(child);
		return child;
	}

	private static String contentOf(final NodeModel cell) {
		return cell.getChildAt(0).getText();
	}

	@Test
	public void turnsColumnValuesIntoCellsInColumnOrder() {
		TreeTableSheetExportTest.setValue(row, "Delai", "3 jours");
		TreeTableSheetExportTest.setValue(row, "Prix", "12");

		assertThat(TreeTableCellMigration.migrate(table)).isTrue();

		assertThat(TreeTableCells.cellsOf(row)).extracting(TreeTableCells::keyOf).containsExactly("Prix", "Delai");
		assertThat(contentOf(TreeTableCells.cellOf(row, "Prix"))).isEqualTo("12");
		assertThat(contentOf(TreeTableCells.cellOf(row, "Delai"))).isEqualTo("3 jours");
		assertThat(NodeAttributeTableModel.getModel(row).getRowCount()).isZero();
	}

	@Test
	public void keepsAttributesWhichAreNotColumnValues() {
		TreeTableSheetExportTest.setValue(row, "Prix", "12");
		TreeTableSheetExportTest.setValue(row, "Auteur", "Jean");

		TreeTableCellMigration.migrate(table);

		assertThat(NodeAttributeTableModel.getModel(row).getAttributeIndex("Auteur")).isZero();
		assertThat(NodeAttributeTableModel.getModel(row).getRowCount()).isEqualTo(1);
	}

	@Test
	public void migratesSubRowsAndPlacesCellsBeforeThem() {
		final NodeModel subRow = childOf(row, "Tache 1");
		TreeTableSheetExportTest.setValue(row, "Prix", "12");
		TreeTableSheetExportTest.setValue(subRow, "Prix", "5");

		TreeTableCellMigration.migrate(table);

		assertThat(row.getChildAt(0)).isSameAs(TreeTableCells.cellOf(row, "Prix"));
		assertThat(TreeTableCells.subRowsOf(row)).containsExactly(subRow);
		assertThat(contentOf(TreeTableCells.cellOf(subRow, "Prix"))).isEqualTo("5");
	}

	@Test
	public void leavesTableWithoutColumnValuesUntouched() {
		assertThat(TreeTableCellMigration.migrate(table)).isFalse();
		assertThat(TreeTableCells.cellsOf(row)).isEmpty();
	}

	@Test
	public void keepsExistingCellRatherThanDuplicatingIt() {
		final NodeModel price = childOf(row, "Prix");
		TreeTableCellModel.markAsCell(price);
		childOf(price, "10");
		TreeTableSheetExportTest.setValue(row, "Prix", "12");

		TreeTableCellMigration.migrate(table);

		assertThat(TreeTableCells.cellsOf(row)).containsExactly(price);
		assertThat(contentOf(price)).isEqualTo("10");
	}
}
